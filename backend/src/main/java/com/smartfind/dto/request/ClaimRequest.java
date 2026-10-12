package com.smartfind.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ClaimRequest(
        @NotNull(message = "Match id is required") Long matchId,
        @NotBlank(message = "Please explain why this item is yours")
        @Size(min = 10, max = 1000, message = "Message must be 10-1000 characters") String message) {}
