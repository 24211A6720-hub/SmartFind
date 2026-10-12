package com.smartfind.controller;

import com.smartfind.dto.response.ApiResponse;
import com.smartfind.dto.response.MatchResponse;
import com.smartfind.entity.ItemType;
import com.smartfind.entity.User;
import com.smartfind.service.MatchingService;
import com.smartfind.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchingService matchingService;
    private final UserService userService;

    public MatchController(MatchingService matchingService, UserService userService) {
        this.matchingService = matchingService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MatchResponse>>> list(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(matchingService.list(userService.fromAuth(auth))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MatchResponse>> get(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(matchingService.get(id, userService.fromAuth(auth))));
    }

    /** Re-run matching for one report. Example: POST /api/matches/run/5?type=LOST */
    @PostMapping("/run/{reportId}")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> run(@PathVariable Long reportId,
                                                                @RequestParam ItemType type,
                                                                Authentication auth) {
        User me = userService.fromAuth(auth);
        return ResponseEntity.ok(ApiResponse.ok("Matching completed", matchingService.run(reportId, type, me)));
    }
}
