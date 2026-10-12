package com.smartfind.dto.response;

public record StatsResponse(long totalUsers, long totalLostReports, long totalFoundReports,
                            long totalMatches, long strongMatches, long pendingClaims,
                            long approvedClaims, long rejectedClaims, long resolvedCases) {}
