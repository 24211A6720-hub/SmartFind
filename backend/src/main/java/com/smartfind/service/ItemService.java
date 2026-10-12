package com.smartfind.service;

import com.smartfind.dto.request.ItemRequest;
import com.smartfind.dto.response.ItemResponse;
import com.smartfind.entity.*;
import com.smartfind.exception.BadRequestException;
import com.smartfind.exception.ForbiddenException;
import com.smartfind.exception.ResourceNotFoundException;
import com.smartfind.mapper.EntityMapper;
import com.smartfind.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/** Lost and found reports share the same logic, so one service handles both (ItemType tells which). */
@Service
@Transactional
public class ItemService {

    private final LostItemRepository lostRepo;
    private final FoundItemRepository foundRepo;
    private final MatchResultRepository matchRepo;
    private final ClaimRepository claimRepo;
    private final NotificationRepository notificationRepo;
    private final MatchingService matchingService;

    public ItemService(LostItemRepository lostRepo, FoundItemRepository foundRepo,
                       MatchResultRepository matchRepo, ClaimRepository claimRepo,
                       NotificationRepository notificationRepo, MatchingService matchingService) {
        this.lostRepo = lostRepo;
        this.foundRepo = foundRepo;
        this.matchRepo = matchRepo;
        this.claimRepo = claimRepo;
        this.notificationRepo = notificationRepo;
        this.matchingService = matchingService;
    }

    // ------------------------------------------------------------------ create / read

    public ItemResponse create(ItemType type, ItemRequest req, User actor) {
        BaseItem item = type == ItemType.LOST ? new LostItem() : new FoundItem();
        item.setUser(actor);
        item.setStatus(type == ItemType.LOST ? ItemStatus.LOST : ItemStatus.FOUND);
        apply(item, req);
        item = save(item);
        matchingService.runFor(item);          // automatic smart matching
        return toResponse(item, actor);
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> list(ItemType type, String name, String category, String color,
                                   String location, LocalDate date, String status, User viewer) {
        ItemStatus wanted = parseStatus(status, false);
        List<? extends BaseItem> all = type == ItemType.LOST
                ? lostRepo.findAllByOrderByCreatedAtDesc() : foundRepo.findAllByOrderByCreatedAtDesc();
        return all.stream()
                .filter(i -> contains(i.getItemName(), name))
                .filter(i -> contains(i.getCategory(), category))
                .filter(i -> contains(i.getColor(), color))
                .filter(i -> contains(i.getLocation(), location))
                .filter(i -> date == null || date.equals(i.getItemDate()))
                .filter(i -> wanted == null || wanted == i.getStatus())
                .map(i -> toResponse(i, viewer))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> mine(ItemType type, User actor) {
        List<? extends BaseItem> all = type == ItemType.LOST
                ? lostRepo.findByUserIdOrderByCreatedAtDesc(actor.getId())
                : foundRepo.findByUserIdOrderByCreatedAtDesc(actor.getId());
        return all.stream().map(i -> toResponse(i, actor)).toList();
    }

    @Transactional(readOnly = true)
    public ItemResponse get(ItemType type, Long id, User viewer) {
        return toResponse(find(type, id), viewer);
    }

    // ------------------------------------------------------------------ update / delete

    public ItemResponse update(ItemType type, Long id, ItemRequest req, User actor) {
        BaseItem item = find(type, id);
        if (!item.getUser().getId().equals(actor.getId())) {
            throw new ForbiddenException("You can only edit your own reports");
        }
        if (item.getStatus() == ItemStatus.CLAIMED || item.getStatus() == ItemStatus.CLOSED) {
            throw new BadRequestException("A claimed or closed report cannot be edited");
        }
        apply(item, req);
        item = save(item);
        matchingService.runFor(item);          // scores may change after an edit
        return toResponse(item, actor);
    }

    public void delete(ItemType type, Long id, User actor) {
        BaseItem item = find(type, id);
        boolean owner = item.getUser().getId().equals(actor.getId());
        if (!owner && actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You can only delete your own reports");
        }
        List<MatchResult> matches = type == ItemType.LOST
                ? matchRepo.findByLostItemId(id) : matchRepo.findByFoundItemId(id);
        for (MatchResult m : matches) {            // remove everything that points to this report
            claimRepo.deleteByMatchId(m.getId());
            notificationRepo.deleteByMatchId(m.getId());
            matchRepo.delete(m);
        }
        matchRepo.flush();
        if (type == ItemType.LOST) lostRepo.delete((LostItem) item); else foundRepo.delete((FoundItem) item);
    }

    /** Admin: change report status (validated against the type). */
    public ItemResponse adminSetStatus(ItemType type, Long id, String status, User admin) {
        ItemStatus newStatus = parseStatus(status, true);
        ItemStatus own = type == ItemType.LOST ? ItemStatus.LOST : ItemStatus.FOUND;
        ItemStatus other = type == ItemType.LOST ? ItemStatus.FOUND : ItemStatus.LOST;
        if (newStatus == other) {
            throw new BadRequestException("Status " + newStatus + " is not valid for a " + type + " report");
        }
        BaseItem item = find(type, id);
        item.setStatus(newStatus == own ? own : newStatus);
        return toResponse(save(item), admin);
    }

    // ------------------------------------------------------------------ helpers

    private BaseItem find(ItemType type, Long id) {
        if (type == ItemType.LOST) {
            return lostRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Lost report not found"));
        }
        return foundRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Found report not found"));
    }

    private BaseItem save(BaseItem item) {
        return item instanceof LostItem l ? lostRepo.save(l) : foundRepo.save((FoundItem) item);
    }

    private void apply(BaseItem item, ItemRequest r) {
        item.setItemName(r.itemName().trim());
        item.setCategory(r.category().trim());
        item.setColor(r.color().trim());
        item.setLocation(r.location().trim());
        item.setItemDate(r.date());
        item.setDescription(r.description().trim());
        item.setIdentifyingDetails(r.identifyingDetails() == null ? null : r.identifyingDetails().trim());
        item.setImageUrl(r.imageUrl() == null || r.imageUrl().isBlank() ? null : r.imageUrl().trim());
        item.setContactInfo(r.contactInfo().trim());
    }

    private ItemResponse toResponse(BaseItem item, User viewer) {
        boolean full = viewer.getRole() == Role.ADMIN || item.getUser().getId().equals(viewer.getId());
        return EntityMapper.toItem(item, full);
    }

    private boolean contains(String field, String query) {
        if (query == null || query.isBlank()) return true;
        return field != null && field.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT));
    }

    private ItemStatus parseStatus(String status, boolean required) {
        if (status == null || status.isBlank()) {
            if (required) throw new BadRequestException("Status is required");
            return null;
        }
        try {
            return ItemStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid status. Allowed: LOST, FOUND, MATCHED, CLAIMED, CLOSED");
        }
    }
}
