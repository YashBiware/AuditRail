package com.auditrail.service.impl;

import com.auditrail.dto.auth.UserSummaryResponse;
import com.auditrail.dto.user.CreateUserRequest;
import com.auditrail.entity.User;
import com.auditrail.exception.ResourceNotFoundException;
import com.auditrail.repository.UserRepository;
import com.auditrail.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UserSummaryResponse createUser(String organizationId, CreateUserRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByOrganizationIdAndEmail(organizationId, cleanEmail)) {
            throw new IllegalArgumentException("User with this email already exists in organization");
        }

        String role = request.getRole().trim().toUpperCase();
        if (!"ADMIN".equals(role) && !"AUDITOR".equals(role)) {
            throw new IllegalArgumentException("Invalid role: must be ADMIN or AUDITOR");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setOrganizationId(organizationId);
        user.setEmail(cleanEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setAccountStatus("ACTIVE");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        userRepository.save(user);

        return new UserSummaryResponse(
            user.getId(),
            user.getOrganizationId(),
            user.getEmail(),
            user.getRole(),
            user.getAccountStatus(),
            user.isTotpEnabled(),
            user.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public void setUserStatus(String organizationId, String userId, String status) {
        User user = userRepository.findById(userId)
            .filter(u -> u.getOrganizationId().equals(organizationId))
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setAccountStatus(status.toUpperCase());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> listUsers(String organizationId) {
        return userRepository.findAllByOrganizationId(organizationId).stream()
            .map(u -> new UserSummaryResponse(
                u.getId(),
                u.getOrganizationId(),
                u.getEmail(),
                u.getRole(),
                u.getAccountStatus(),
                u.isTotpEnabled(),
                u.getCreatedAt()
            ))
            .toList();
    }
}
