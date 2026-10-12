package com.smartfind.matching;

import com.smartfind.entity.MatchLevel;
import java.util.List;

/** Output of the matching engine for one lost/found pair. All scores are 0-100. */
public record MatchResultDetail(double nameScore, double categoryScore, double colorScore,
                                double locationScore, double dateScore, double descriptionScore,
                                double total, MatchLevel level, List<String> reasons) {}
