package com.smartfind.service;

import com.smartfind.dto.response.MatchResponse;
import com.smartfind.entity.*;
import com.smartfind.exception.ForbiddenException;
import com.smartfind.exception.ResourceNotFoundException;
import com.smartfind.mapper.EntityMapper;
import com.smartfind.matching.MatchConfig;
import com.smartfind.matching.MatchResultDetail;
import com.smartfind.matching.MatchStrategy;
import com.smartfind.repository.FoundItemRepository;
import com.smartfind.repository.LostItemRepository;
import com.smartfind.repository.MatchResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Orchestrates matching: finds candidate reports, asks the MatchStrategy for a score,
 * saves the result, sends notifications and exposes matches to controllers.
 */
@Service
@Transactional
public class MatchingService {

    private static final List<ItemStatus> OPEN_LOST = List.of(ItemStatus.LOST, ItemStatus.MATCHED);
    private static final List<ItemStatus> OPEN_FOUND = List.of(ItemStatus.FOUND, ItemStatus.MATCHED);

    private final LostItemRepository lostRepo;
    private final FoundItemRepository foundRepo;
    private final MatchResultRepository matchRepo;
    private final NotificationService notificationService;
    private final MatchStrategy strategy;
    private final MatchConfig config;

    public MatchingService(LostItemRepository lostRepo, FoundItemRepository foundRepo,
                           MatchResultRepository matchRepo, NotificationService notificationService,
                           MatchStrategy strategy, MatchConfig config) {
        this.lostRepo = lostRepo;
        this.foundRepo = foundRepo;
        this.matchRepo = matchRepo;
        this.notificationService = notificationService;
        this.strategy = strategy;
        this.config = config;
    }

    // ---------------------------------------------------------------- running the engine

    /** Compares one report against all open reports of the opposite type. */
    public List<MatchResult> runFor(BaseItem item) {
        List<MatchResult> results = new ArrayList<>();
        if (item.getStatus() == ItemStatus.CLAIMED || item.getStatus() == ItemStatus.CLOSED) return results;

        if (item instanceof LostItem lost) {
            for (FoundItem found : foundRepo.findByStatusIn(OPEN_FOUND)) {
                if (!eligible(lost, found)) continue;
                MatchResult r = evaluateAndStore(lost, found);
                if (r != null) results.add(r);
            }
        } else if (item instanceof FoundItem found) {
            for (LostItem lost : lostRepo.findByStatusIn(OPEN_LOST)) {
                if (!eligible(lost, found)) continue;
                MatchResult r = evaluateAndStore(lost, found);
                if (r != null) results.add(r);
            }
        }
        results.sort(Comparator.comparingDouble(MatchResult::getScore).reversed());
        return results;
    }

    /** Candidate filter: different owners, and the item cannot be found before it was lost (1 day tolerance). */
    private boolean eligible(LostItem lost, FoundItem found) {
        if (lost.getUser().getId().equals(found.getUser().getId())) return false;
        return !found.getItemDate().isBefore(lost.getItemDate().minusDays(1));
    }

    private MatchResult evaluateAndStore(LostItem lost, FoundItem found) {
        MatchResultDetail d = strategy.evaluate(lost, found);
        Optional<MatchResult> existing = matchRepo.findByLostItemIdAndFoundItemId(lost.getId(), found.getId());

        if (existing.isEmpty() && d.total() < config.getMinStore()) return null;

        MatchResult m = existing.orElseGet(MatchResult::new);
        boolean isNew = m.getId() == null;
        if (!isNew && m.getStatus() != MatchStatus.ACTIVE) return m;   // resolved matches are frozen
        double previousScore = isNew ? 0 : m.getScore();

        m.setLostItem(lost);
        m.setFoundItem(found);
        m.setScore(d.total());
        m.setMatchLevel(d.level());
        m.setReasons(String.join("\n", d.reasons()));
        m.setNameScore(d.nameScore());
        m.setCategoryScore(d.categoryScore());
        m.setColorScore(d.colorScore());
        m.setLocationScore(d.locationScore());
        m.setDateScore(d.dateScore());
        m.setDescriptionScore(d.descriptionScore());
        m.setStatus(MatchStatus.ACTIVE);
        m = matchRepo.save(m);

        // Notify only when the score first crosses the notify threshold (avoids duplicate notifications).
        if (d.total() >= config.getNotifyThreshold() && previousScore < config.getNotifyThreshold()) {
            String pct = String.format("%.0f%%", d.total());
            notificationService.notify(lost.getUser(),
                    "Possible match found for your lost item \"" + lost.getItemName() + "\" (" + pct + " match).", m);
            notificationService.notify(found.getUser(),
                    "Your found item \"" + found.getItemName() + "\" may belong to someone who reported it lost ("
                            + pct + " match).", m);
            if (lost.getStatus() == ItemStatus.LOST) lost.setStatus(ItemStatus.MATCHED);
            if (found.getStatus() == ItemStatus.FOUND) found.setStatus(ItemStatus.MATCHED);
            lostRepo.save(lost);
            foundRepo.save(found);
        }
        return m;
    }

    // ---------------------------------------------------------------- API-facing methods

    /** POST /api/matches/run/{reportId}: owner or admin can re-run matching for a report. */
    public List<MatchResponse> run(Long reportId, ItemType type, User actor) {
        BaseItem item = type == ItemType.LOST
                ? lostRepo.findById(reportId).orElseThrow(() -> new ResourceNotFoundException("Report not found"))
                : foundRepo.findById(reportId).orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        boolean owner = item.getUser().getId().equals(actor.getId());
        if (!owner && actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You can only run matching for your own reports");
        }
        return runFor(item).stream().map(m -> toResponse(m, actor)).toList();
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> list(User viewer) {
        List<MatchResult> matches = viewer.getRole() == Role.ADMIN
                ? matchRepo.findAllByOrderByScoreDesc()
                : matchRepo.findForUser(viewer.getId()).stream()
                        .filter(m -> m.getMatchLevel() != MatchLevel.WEAK && m.getStatus() != MatchStatus.DISMISSED)
                        .toList();
        return matches.stream().map(m -> toResponse(m, viewer)).toList();
    }

    @Transactional(readOnly = true)
    public MatchResponse get(Long id, User viewer) {
        MatchResult m = matchRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        boolean party = m.getLostItem().getUser().getId().equals(viewer.getId())
                || m.getFoundItem().getUser().getId().equals(viewer.getId());
        if (!party && viewer.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You are not part of this match");
        }
        return toResponse(m, viewer);
    }

    /**
     * Privacy rule: contact info and identifying details of a report are shown only to its owner and admins,
     * and to the other party once the claim has been approved.
     */
    public MatchResponse toResponse(MatchResult m, User viewer) {
        boolean admin = viewer.getRole() == Role.ADMIN;
        boolean lostOwner = m.getLostItem().getUser().getId().equals(viewer.getId());
        boolean foundOwner = m.getFoundItem().getUser().getId().equals(viewer.getId());
        boolean resolved = m.getStatus() == MatchStatus.CLAIMED || m.getStatus() == MatchStatus.CLOSED;

        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("name", m.getNameScore());
        scores.put("category", m.getCategoryScore());
        scores.put("color", m.getColorScore());
        scores.put("location", m.getLocationScore());
        scores.put("date", m.getDateScore());
        scores.put("description", m.getDescriptionScore());

        List<String> reasons = m.getReasons() == null || m.getReasons().isBlank()
                ? List.of() : Arrays.asList(m.getReasons().split("\n"));

        return new MatchResponse(m.getId(),
                EntityMapper.toItem(m.getLostItem(), admin || lostOwner || (foundOwner && resolved)),
                EntityMapper.toItem(m.getFoundItem(), admin || foundOwner || (lostOwner && resolved)),
                m.getScore(), m.getMatchLevel(), reasons, scores, m.getStatus(), m.getCreatedAt());
    }
}
