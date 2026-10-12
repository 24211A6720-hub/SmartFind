package com.smartfind.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank(message = "Name is required") @Size(max = 100) String name,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") @Size(max = 150) String email,
        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Enter a valid phone number") String phone,
        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$",
                message = "Password must be at least 8 characters with a letter and a number") String password) {}
