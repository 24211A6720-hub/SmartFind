package com.smartfind.dto.response;

import java.util.List;

public record ActivityResponse(UserResponse user, List<ItemResponse> lostItems,
                               List<ItemResponse> foundItems, List<ClaimResponse> claims) {}
