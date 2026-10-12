package com.smartfind.dto.response;

import com.smartfind.entity.Role;
import java.time.LocalDateTime;

public record UserResponse(Long id, String name, String email, String phone, Role role,
                           boolean enabled, LocalDateTime createdAt) {}
