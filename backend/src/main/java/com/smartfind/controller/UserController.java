package com.smartfind.controller;

import com.smartfind.dto.request.UpdateProfileRequest;
import com.smartfind.dto.response.ApiResponse;
import com.smartfind.dto.response.UserResponse;
import com.smartfind.entity.User;
import com.smartfind.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Admin only (enforced in SecurityConfig). */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> list(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(ApiResponse.ok(userService.list(q)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> get(@PathVariable Long id, Authentication auth) {
        User me = userService.fromAuth(auth);
        return ResponseEntity.ok(ApiResponse.ok(userService.getById(id, me)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(@PathVariable Long id,
                                                            @Valid @RequestBody UpdateProfileRequest req,
                                                            Authentication auth) {
        User me = userService.fromAuth(auth);
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", userService.update(id, req, me)));
    }
}
