package com.smartfind.repository;

import com.smartfind.entity.FoundItem;
import com.smartfind.entity.ItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FoundItemRepository extends JpaRepository<FoundItem, Long> {
    List<FoundItem> findAllByOrderByCreatedAtDesc();
    List<FoundItem> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<FoundItem> findByStatusIn(Collection<ItemStatus> statuses);
    long countByUserId(Long userId);
}
