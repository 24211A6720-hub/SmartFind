package com.smartfind.controller;

import com.smartfind.dto.request.EnabledRequest;
import com.smartfind.dto.request.StatusRequest;
import com.smartfind.dto.response.*;
import com.smartfind.entity.ItemType;
import com.smartfind.entity.User;
import com.smartfind.service.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Every endpoint here requires the ADMIN role (rule in SecurityConfig: /api/admin/**). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final ItemService itemService;
    private final ClaimService claimService;
    private final MatchingService matchingService;

    public AdminController(AdminService adminService, UserService userService, ItemService itemService,
                           ClaimService claimService, MatchingService matchingService) {
        this.adminService = adminService;
        this.userService = userService;
        this.itemService = itemService;
        this.claimService = claimService;
        this.matchingService = matchingService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.stats()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> users(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(userService.list(q)));
    }

    @GetMapping("/users/{id}/activity")
    public ResponseEntity<ApiResponse<ActivityResponse>> activity(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.activity(id, userService.fromAuth(auth))));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> setEnabled(@PathVariable Long id,
                                                                @Valid @RequestBody EnabledRequest req,
                                                                Authentication auth) {
        User admin = userService.fromAuth(auth);
        return ResponseEntity.ok(ApiResponse.ok("User updated", adminService.setUserEnabled(id, req.enabled(), admin)));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id, Authentication auth) {
        adminService.deleteUser(id, userService.fromAuth(auth));
        return ResponseEntity.ok(ApiResponse.ok("User deleted", null));
    }

    @PutMapping("/lost-items/{id}/status")
    public ResponseEntity<ApiResponse<ItemResponse>> lostStatus(@PathVariable Long id,
                                                                @Valid @RequestBody StatusRequest req,
                                                                Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                itemService.adminSetStatus(ItemType.LOST, id, req.status(), userService.fromAuth(auth))));
    }

    @PutMapping("/found-items/{id}/status")
    public ResponseEntity<ApiResponse<ItemResponse>> foundStatus(@PathVariable Long id,
                                                                 @Valid @RequestBody StatusRequest req,
                                                                 Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                itemService.adminSetStatus(ItemType.FOUND, id, req.status(), userService.fromAuth(auth))));
    }

    @GetMapping("/claims")
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> claims(@RequestParam(required = false) String status,
                                                                   Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(claimService.list(status, userService.fromAuth(auth))));
    }

    @GetMapping("/matches")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> matches(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(matchingService.list(userService.fromAuth(auth))));
    }

    @PutMapping("/cases/{matchId}/close")
    public ResponseEntity<ApiResponse<MatchResponse>> closeCase(@PathVariable Long matchId, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok("Case closed",
                adminService.closeCase(matchId, userService.fromAuth(auth))));
    }
}
