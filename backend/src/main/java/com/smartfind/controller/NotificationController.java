package com.smartfind.controller;

import com.smartfind.dto.response.ApiResponse;
import com.smartfind.dto.response.NotificationResponse;
import com.smartfind.entity.User;
import com.smartfind.service.NotificationService;
import com.smartfind.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> list(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(notificationService.list(userService.fromAuth(auth))));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> unreadCount(Authentication auth) {
        long count = notificationService.unreadCount(userService.fromAuth(auth));
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(@PathVariable Long id, Authentication auth) {
        User me = userService.fromAuth(auth);
        return ResponseEntity.ok(ApiResponse.ok(notificationService.markRead(id, me)));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(Authentication auth) {
        notificationService.markAllRead(userService.fromAuth(auth));
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }
}
