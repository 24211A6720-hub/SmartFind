package com.smartfind.dto.response;

import com.smartfind.entity.ItemStatus;
import com.smartfind.entity.ItemType;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** identifyingDetails and contactInfo are null unless the viewer is allowed to see them. */
public record ItemResponse(Long id, ItemType type, String itemName, String category, String color,
                           String location, LocalDate date, String description, String identifyingDetails,
                           String imageUrl, String contactInfo, ItemStatus status,
                           Long ownerId, String ownerName, LocalDateTime createdAt) {}
