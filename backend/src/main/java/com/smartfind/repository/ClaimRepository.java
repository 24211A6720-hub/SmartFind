package com.smartfind.repository;

import com.smartfind.entity.Claim;
import com.smartfind.entity.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {
    List<Claim> findAllByOrderByCreatedAtDesc();
    List<Claim> findByStatusOrderByCreatedAtDesc(ClaimStatus status);
    List<Claim> findByClaimantIdOrderByCreatedAtDesc(Long claimantId);
    List<Claim> findByClaimantIdAndStatusOrderByCreatedAtDesc(Long claimantId, ClaimStatus status);
    List<Claim> findByMatchId(Long matchId);
    boolean existsByMatchIdAndClaimantIdAndStatus(Long matchId, Long claimantId, ClaimStatus status);
    long countByStatus(ClaimStatus status);
    void deleteByMatchId(Long matchId);
}
