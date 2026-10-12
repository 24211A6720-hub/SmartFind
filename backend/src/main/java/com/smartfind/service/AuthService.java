package com.smartfind.service;

import com.smartfind.dto.request.LoginRequest;
import com.smartfind.dto.request.RegisterRequest;
import com.smartfind.dto.response.AuthResponse;
import com.smartfind.entity.Role;
import com.smartfind.entity.User;
import com.smartfind.exception.ConflictException;
import com.smartfind.exception.ForbiddenException;
import com.smartfind.exception.UnauthorizedException;
import com.smartfind.mapper.EntityMapper;
import com.smartfind.repository.UserRepository;
import com.smartfind.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(email);
        user.setPhone(req.phone().trim());
        user.setPasswordHash(passwordEncoder.encode(req.password()));   // BCrypt hash, never plain text
        user.setRole(Role.USER);                                         // public sign-up can never create an admin
        user = userRepository.save(user);
        return new AuthResponse(jwtUtil.generateToken(user.getEmail(), user.getRole().name()), EntityMapper.toUser(user));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new ForbiddenException("This account has been disabled by an administrator");
        }
        return new AuthResponse(jwtUtil.generateToken(user.getEmail(), user.getRole().name()), EntityMapper.toUser(user));
    }
}
