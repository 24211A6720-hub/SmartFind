package com.smartfind.repository;

import com.smartfind.entity.MatchResult;
import com.smartfind.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    Optional<MatchResult> findByLostItemIdAndFoundItemId(Long lostItemId, Long foundItemId);

    List<MatchResult> findByLostItemId(Long lostItemId);

    List<MatchResult> findByFoundItemId(Long foundItemId);

    List<MatchResult> findAllByOrderByScoreDesc();

    long countByStatus(MatchStatus status);

    /** All matches where the user owns either the lost or the found report. */
    @Query("select m from MatchResult m where m.lostItem.user.id = :uid or m.foundItem.user.id = :uid order by m.score desc")
    List<MatchResult> findForUser(@Param("uid") Long userId);
}
