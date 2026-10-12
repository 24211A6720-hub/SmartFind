package com.smartfind.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(Long id, String message, Long matchId, boolean read, LocalDateTime createdAt) {}
