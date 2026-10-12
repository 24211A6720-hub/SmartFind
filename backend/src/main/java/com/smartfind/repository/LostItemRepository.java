package com.smartfind.repository;

import com.smartfind.entity.ItemStatus;
import com.smartfind.entity.LostItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LostItemRepository extends JpaRepository<LostItem, Long> {
    List<LostItem> findAllByOrderByCreatedAtDesc();
    List<LostItem> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<LostItem> findByStatusIn(Collection<ItemStatus> statuses);
    long countByUserId(Long userId);
}
