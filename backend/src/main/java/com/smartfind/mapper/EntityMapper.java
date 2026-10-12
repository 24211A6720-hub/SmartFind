package com.smartfind.mapper;

import com.smartfind.dto.response.*;
import com.smartfind.entity.*;

/** Converts JPA entities to response DTOs so entities are never exposed directly. */
public final class EntityMapper {

    private EntityMapper() {}

    public static UserResponse toUser(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole(),
                u.isEnabled(), u.getCreatedAt());
    }

    /** full=false hides identifying details and contact info (used when someone else views the report). */
    public static ItemResponse toItem(BaseItem i, boolean full) {
        return new ItemResponse(i.getId(), i.getType(), i.getItemName(), i.getCategory(), i.getColor(),
                i.getLocation(), i.getItemDate(), i.getDescription(),
                full ? i.getIdentifyingDetails() : null, i.getImageUrl(),
                full ? i.getContactInfo() : null, i.getStatus(),
                i.getUser().getId(), i.getUser().getName(), i.getCreatedAt());
    }

    public static NotificationResponse toNotification(Notification n) {
        return new NotificationResponse(n.getId(), n.getMessage(),
                n.getMatch() == null ? null : n.getMatch().getId(), n.isSeen(), n.getCreatedAt());
    }

    public static ClaimResponse toClaim(Claim c) {
        MatchResult m = c.getMatch();
        return new ClaimResponse(c.getId(), m.getId(), c.getClaimant().getId(), c.getClaimant().getName(),
                m.getLostItem().getItemName(), m.getFoundItem().getItemName(), m.getScore(),
                c.getMessage(), c.getStatus(), c.getAdminNote(),
                c.getReviewedBy() == null ? null : c.getReviewedBy().getName(),
                c.getReviewedAt(), c.getCreatedAt());
    }
}
