package com.smartfind.service;

import com.smartfind.dto.request.ClaimRequest;
import com.smartfind.dto.response.ClaimResponse;
import com.smartfind.entity.*;
import com.smartfind.exception.BadRequestException;
import com.smartfind.exception.ConflictException;
import com.smartfind.exception.ForbiddenException;
import com.smartfind.exception.ResourceNotFoundException;
import com.smartfind.mapper.EntityMapper;
import com.smartfind.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class ClaimService {

    private final ClaimRepository claimRepo;
    private final MatchResultRepository matchRepo;
    private final LostItemRepository lostRepo;
    private final FoundItemRepository foundRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    public ClaimService(ClaimRepository claimRepo, MatchResultRepository matchRepo, LostItemRepository lostRepo,
                        FoundItemRepository foundRepo, UserRepository userRepo,
                        NotificationService notificationService) {
        this.claimRepo = claimRepo;
        this.matchRepo = matchRepo;
        this.lostRepo = lostRepo;
        this.foundRepo = foundRepo;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
    }

    /** A user claims a match. Only the owner of the LOST report may claim, and an admin must verify. */
    public ClaimResponse create(ClaimRequest req, User actor) {
        MatchResult match = matchRepo.findById(req.matchId())
                .orElseThrow(() -> new ResourceNotFoundException("Match not found"));
        if (!match.getLostItem().getUser().getId().equals(actor.getId())) {
            throw new ForbiddenException("Only the owner of the lost report can claim this match");
        }
        if (match.getMatchLevel() == MatchLevel.WEAK) {
            throw new BadRequestException("Weak matches cannot be claimed");
        }
        if (match.getStatus() != MatchStatus.ACTIVE) {
            throw new BadRequestException("This match is no longer open for claims");
        }
        if (claimRepo.existsByMatchIdAndClaimantIdAndStatus(match.getId(), actor.getId(), ClaimStatus.PENDING)) {
            throw new ConflictException("You already have a pending claim for this match");
        }

        Claim claim = new Claim();
        claim.setMatch(match);
        claim.setClaimant(actor);
        claim.setMessage(req.message().trim());
        claim = claimRepo.save(claim);

        for (User admin : userRepo.findByRole(Role.ADMIN)) {
            notificationService.notify(admin,
                    "New claim #" + claim.getId() + " by " + actor.getName() + " is waiting for review.", match);
        }
        return EntityMapper.toClaim(claim);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> list(String status, User actor) {
        ClaimStatus wanted = parseStatus(status);
        List<Claim> claims;
        if (actor.getRole() == Role.ADMIN) {
            claims = wanted == null ? claimRepo.findAllByOrderByCreatedAtDesc()
                    : claimRepo.findByStatusOrderByCreatedAtDesc(wanted);
        } else {
            claims = wanted == null ? claimRepo.findByClaimantIdOrderByCreatedAtDesc(actor.getId())
                    : claimRepo.findByClaimantIdAndStatusOrderByCreatedAtDesc(actor.getId(), wanted);
        }
        return claims.stream().map(EntityMapper::toClaim).toList();
    }

    /** Admin approves: claim APPROVED, match CLAIMED, both reports CLAIMED, competing claims rejected. */
    public ClaimResponse approve(Long id, String note, User admin) {
        Claim claim = findPending(id);
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setReviewedBy(admin);
        claim.setReviewedAt(LocalDateTime.now());
        claim.setAdminNote(note);
        claimRepo.save(claim);

        MatchResult match = claim.getMatch();
        match.setStatus(MatchStatus.CLAIMED);
        matchRepo.save(match);

        LostItem lost = match.getLostItem();
        FoundItem found = match.getFoundItem();
        lost.setStatus(ItemStatus.CLAIMED);
        found.setStatus(ItemStatus.CLAIMED);
        lostRepo.save(lost);
        foundRepo.save(found);

        // other pending claims on the same match lose automatically
        for (Claim other : claimRepo.findByMatchId(match.getId())) {
            if (!other.getId().equals(claim.getId()) && other.getStatus() == ClaimStatus.PENDING) {
                other.setStatus(ClaimStatus.REJECTED);
                other.setReviewedBy(admin);
                other.setReviewedAt(LocalDateTime.now());
                other.setAdminNote("Another claim for this match was approved");
                claimRepo.save(other);
            }
        }
        // other matches that involve these two reports are no longer relevant
        dismissOthers(matchRepo.findByLostItemId(lost.getId()), match);
        dismissOthers(matchRepo.findByFoundItemId(found.getId()), match);

        notificationService.notify(claim.getClaimant(),
                "Your claim for \"" + lost.getItemName() + "\" was approved. You can now see the finder's contact details.",
                match);
        notificationService.notify(found.getUser(),
                "The owner's claim for the item you found (\"" + found.getItemName() + "\") was approved by the admin.",
                match);
        return EntityMapper.toClaim(claim);
    }

    public ClaimResponse reject(Long id, String note, User admin) {
        Claim claim = findPending(id);
        claim.setStatus(ClaimStatus.REJECTED);
        claim.setReviewedBy(admin);
        claim.setReviewedAt(LocalDateTime.now());
        claim.setAdminNote(note == null || note.isBlank() ? "Rejected by admin" : note.trim());
        claimRepo.save(claim);
        notificationService.notify(claim.getClaimant(),
                "Your claim for \"" + claim.getMatch().getLostItem().getItemName() + "\" was rejected: "
                        + claim.getAdminNote(), claim.getMatch());
        return EntityMapper.toClaim(claim);
    }

    private void dismissOthers(List<MatchResult> matches, MatchResult keep) {
        for (MatchResult m : matches) {
            if (!m.getId().equals(keep.getId()) && m.getStatus() == MatchStatus.ACTIVE) {
                m.setStatus(MatchStatus.DISMISSED);
                matchRepo.save(m);
            }
        }
    }

    private Claim findPending(Long id) {
        Claim claim = claimRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Claim not found"));
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("This claim has already been " + claim.getStatus().name().toLowerCase());
        }
        return claim;
    }

    private ClaimStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return null;
        try {
            return ClaimStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status. Allowed: PENDING, APPROVED, REJECTED");
        }
    }
}
