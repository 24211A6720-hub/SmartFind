package com.smartfind.dto.response;

import com.smartfind.entity.MatchLevel;
import com.smartfind.entity.MatchStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record MatchResponse(Long id, ItemResponse lostItem, ItemResponse foundItem, double score,
                            MatchLevel matchLevel, List<String> reasons, Map<String, Double> componentScores,
                            MatchStatus status, LocalDateTime createdAt) {}
