package com.smartfind.controller;

import com.smartfind.dto.request.LoginRequest;
import com.smartfind.dto.request.RegisterRequest;
import com.smartfind.dto.response.ApiResponse;
import com.smartfind.dto.response.AuthResponse;
import com.smartfind.dto.response.UserResponse;
import com.smartfind.mapper.EntityMapper;
import com.smartfind.service.AuthService;
import com.smartfind.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful", authService.register(req)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authService.login(req)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication auth) {
        return ResponseEntity.ok(ApiResponse.ok(EntityMapper.toUser(userService.fromAuth(auth))));
    }
}
