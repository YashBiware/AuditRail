package com.auditrail.service.impl;

import com.auditrail.dto.user.CreateInvitationRequest;
import com.auditrail.dto.user.InvitationResponse;
import com.auditrail.dto.user.RedeemInvitationRequest;
import com.auditrail.entity.Invitation;
import com.auditrail.entity.User;
import com.auditrail.exception.ResourceNotFoundException;
import com.auditrail.repository.InvitationRepository;
import com.auditrail.repository.UserRepository;
import com.auditrail.service.InvitationService;
import com.auditrail.util.HashUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class InvitationServiceImpl implements InvitationService {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public InvitationServiceImpl(
        InvitationRepository invitationRepository,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public InvitationResponse createInvitation(String organizationId, CreateInvitationRequest request, String createdByUserId) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByOrganizationIdAndEmail(organizationId, cleanEmail)) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        byte[] tokenBytes = new byte[24];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = HexFormat.of().formatHex(tokenBytes);
        String tokenHash = HashUtil.sha256Hex(rawToken);

        Invitation invitation = new Invitation();
        invitation.setId(UUID.randomUUID().toString());
        invitation.setOrganizationId(organizationId);
        invitation.setEmail(cleanEmail);
        invitation.setRole(request.getRole().trim().toUpperCase());
        invitation.setTokenHash(tokenHash);
        invitation.setStatus("PENDING");
        invitation.setExpiresAt(Instant.now().plus(48, ChronoUnit.HOURS));
        invitation.setCreatedByUserId(createdByUserId);
        invitation.setCreatedAt(Instant.now());

        invitationRepository.save(invitation);

        String inviteUrl = "/accept-invitation?token=" + rawToken;

        return new InvitationResponse(
            invitation.getId(),
            invitation.getEmail(),
            invitation.getRole(),
            rawToken,
            inviteUrl,
            invitation.getStatus(),
            invitation.getExpiresAt()
        );
    }

    @Override
    @Transactional
    public void redeemInvitation(RedeemInvitationRequest request) {
        String tokenHash = HashUtil.sha256Hex(request.getToken().trim());
        Invitation invitation = invitationRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invitation token"));

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new IllegalArgumentException("Invitation has already been " + invitation.getStatus().toLowerCase());
        }

        if (invitation.getExpiresAt().isBefore(Instant.now())) {
            invitation.setStatus("EXPIRED");
            invitationRepository.save(invitation);
            throw new IllegalArgumentException("Invitation token has expired");
        }

        if (userRepository.existsByOrganizationIdAndEmail(invitation.getOrganizationId(), invitation.getEmail())) {
            throw new IllegalArgumentException("User account already exists for this email");
        }

        // Create new user
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setOrganizationId(invitation.getOrganizationId());
        user.setEmail(invitation.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(invitation.getRole());
        user.setAccountStatus("ACTIVE");
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        // Mark invitation as accepted
        invitation.setStatus("ACCEPTED");
        invitation.setUsedAt(Instant.now());
        invitationRepository.save(invitation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> listInvitations(String organizationId) {
        return invitationRepository.findAllByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
            .map(i -> new InvitationResponse(
                i.getId(),
                i.getEmail(),
                i.getRole(),
                null,
                null,
                i.getStatus(),
                i.getExpiresAt()
            ))
            .toList();
    }
}
