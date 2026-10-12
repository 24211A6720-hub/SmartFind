package com.smartfind.dto.request;

import jakarta.validation.constraints.*;

/** newPassword is optional; leave it empty to keep the current password. */
public record UpdateProfileRequest(
        @NotBlank(message = "Name is required") @Size(max = 100) String name,
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Enter a valid phone number") String phone,
        @Pattern(regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "Password must be at least 8 characters with a letter and a number") String newPassword) {}
