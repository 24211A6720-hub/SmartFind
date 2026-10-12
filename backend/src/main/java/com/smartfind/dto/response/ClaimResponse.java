package com.smartfind.dto.response;

import com.smartfind.entity.ClaimStatus;
import java.time.LocalDateTime;

public record ClaimResponse(Long id, Long matchId, Long claimantId, String claimantName,
                            String lostItemName, String foundItemName, double matchScore,
                            String message, ClaimStatus status, String adminNote,
                            String reviewedByName, LocalDateTime reviewedAt, LocalDateTime createdAt) {}
