package com.smartfind.repository;

import com.smartfind.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByUserIdAndSeenFalse(Long userId);
    long countByUserIdAndSeenFalse(Long userId);
    void deleteByMatchId(Long matchId);
}
