package com.smartfind.controller;

import com.smartfind.dto.request.ClaimRequest;
import com.smartfind.dto.request.NoteRequest;
import com.smartfind.dto.response.ApiResponse;
import com.smartfind.dto.response.ClaimResponse;
import com.smartfind.entity.User;
import com.smartfind.service.ClaimService;
import com.smartfind.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;
    private final UserService userService;

    public ClaimController(ClaimService claimService, UserService userService) {
        this.claimService = claimService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClaimResponse>> create(@Valid @RequestBody ClaimRequest req, Authentication auth) {
        User me = userService.fromAuth(auth);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Claim submitted. An admin will review it.", claimService.create(req, me)));
    }

    /** Users see their own claims; admins see all claims. Optional ?status=PENDING */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClaimResponse>>> list(@RequestParam(required = false) String status,
                                                                 Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(claimService.list(status, userService.fromAuth(auth))));
    }

    /** Admin only (enforced in SecurityConfig). */
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ClaimResponse>> approve(@PathVariable Long id,
                                                              @Valid @RequestBody(required = false) NoteRequest body,
                                                              Authentication auth) {
        User admin = userService.fromAuth(auth);
        return ResponseEntity.ok(ApiResponse.ok("Claim approved",
                claimService.approve(id, body == null ? null : body.note(), admin)));
    }

    /** Admin only (enforced in SecurityConfig). */
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ClaimResponse>> reject(@PathVariable Long id,
                                                             @Valid @RequestBody(required = false) NoteRequest body,
                                                             Authentication auth) {
        User admin = userService.fromAuth(auth);
        return ResponseEntity.ok(ApiResponse.ok("Claim rejected",
                claimService.reject(id, body == null ? null : body.note(), admin)));
    }
}
