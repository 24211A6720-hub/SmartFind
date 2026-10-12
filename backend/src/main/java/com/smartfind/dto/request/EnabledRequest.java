package com.smartfind.dto.request;

import jakarta.validation.constraints.NotNull;

public record EnabledRequest(@NotNull(message = "enabled is required") Boolean enabled) {}
