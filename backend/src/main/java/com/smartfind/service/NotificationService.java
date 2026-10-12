package com.smartfind.service;

import com.smartfind.dto.response.NotificationResponse;
import com.smartfind.entity.MatchResult;
import com.smartfind.entity.Notification;
import com.smartfind.entity.User;
import com.smartfind.exception.ForbiddenException;
import com.smartfind.exception.ResourceNotFoundException;
import com.smartfind.mapper.EntityMapper;
import com.smartfind.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /** Creates an in-app notification. match may be null. */
    public void notify(User user, String message, MatchResult match) {
        Notification n = new Notification();
        n.setUser(user);
        n.setMessage(message);
        n.setMatch(match);
        notificationRepository.save(n);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(User user) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(EntityMapper::toNotification).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(User user) {
        return notificationRepository.countByUserIdAndSeenFalse(user.getId());
    }

    public NotificationResponse markRead(Long id, User user) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!n.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("This notification belongs to another user");
        }
        n.setSeen(true);
        return EntityMapper.toNotification(notificationRepository.save(n));
    }

    public void markAllRead(User user) {
        List<Notification> unread = notificationRepository.findByUserIdAndSeenFalse(user.getId());
        unread.forEach(n -> n.setSeen(true));
        notificationRepository.saveAll(unread);
    }
}
