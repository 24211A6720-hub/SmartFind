package com.smartfind.service;

import com.smartfind.dto.request.UpdateProfileRequest;
import com.smartfind.dto.response.UserResponse;
import com.smartfind.entity.Role;
import com.smartfind.entity.User;
import com.smartfind.exception.ForbiddenException;
import com.smartfind.exception.ResourceNotFoundException;
import com.smartfind.exception.UnauthorizedException;
import com.smartfind.mapper.EntityMapper;
import com.smartfind.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Loads the logged-in user from the Authentication that Spring Security filled from the JWT. */
    @Transactional(readOnly = true)
    public User fromAuth(Authentication auth) {
        if (auth == null) throw new UnauthorizedException("Authentication required");
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id, User actor) {
        checkSelfOrAdmin(id, actor);
        return EntityMapper.toUser(find(id));
    }

    public UserResponse update(Long id, UpdateProfileRequest req, User actor) {
        checkSelfOrAdmin(id, actor);
        User user = find(id);
        user.setName(req.name().trim());
        user.setPhone(req.phone().trim());
        if (req.newPassword() != null && !req.newPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        }
        return EntityMapper.toUser(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(String query) {
        List<User> users = (query == null || query.isBlank())
                ? userRepository.findAllByOrderByCreatedAtDesc()
                : userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByCreatedAtDesc(query.trim(), query.trim());
        return users.stream().map(EntityMapper::toUser).toList();
    }

    public User find(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void checkSelfOrAdmin(Long id, User actor) {
        if (!actor.getId().equals(id) && actor.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You can only access your own profile");
        }
    }
}
