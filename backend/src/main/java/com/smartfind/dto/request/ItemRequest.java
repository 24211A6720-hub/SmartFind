package com.smartfind.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/** Used for both lost and found reports. */
public record ItemRequest(
        @NotBlank(message = "Item name is required") @Size(max = 150) String itemName,
        @NotBlank(message = "Category is required") @Size(max = 60) String category,
        @NotBlank(message = "Color is required") @Size(max = 60) String color,
        @NotBlank(message = "Location is required") @Size(max = 200) String location,
        @NotNull(message = "Date is required") @PastOrPresent(message = "Date cannot be in the future") LocalDate date,
        @NotBlank(message = "Description is required")
        @Size(min = 5, max = 1000, message = "Description must be 5-1000 characters") String description,
        @Size(max = 1000) String identifyingDetails,
        @Size(max = 500) String imageUrl,
        @NotBlank(message = "Contact information is required") @Size(max = 150) String contactInfo) {}
