package com.auditrail.service.impl;

import com.auditrail.dto.auth.*;
import com.auditrail.entity.ApiKey;
import com.auditrail.entity.ChainHead;
import com.auditrail.entity.Organization;
import com.auditrail.entity.User;
import com.auditrail.exception.BadRequestException;
import com.auditrail.exception.ResourceNotFoundException;
import com.auditrail.exception.UnauthorizedException;
import com.auditrail.repository.ApiKeyRepository;
import com.auditrail.repository.ChainHeadRepository;
import com.auditrail.repository.OrganizationRepository;
import com.auditrail.repository.UserRepository;
import com.auditrail.security.JwtTokenProvider;
import com.auditrail.security.TenantContext;
import com.auditrail.security.UserPrincipal;
import com.auditrail.service.AuthService;
import com.auditrail.util.HashUtil;
import com.auditrail.util.TotpUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final ChainHeadRepository chainHeadRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthServiceImpl(
        UserRepository userRepository,
        OrganizationRepository organizationRepository,
        ChainHeadRepository chainHeadRepository,
        ApiKeyRepository apiKeyRepository,
        PasswordEncoder passwordEncoder,
        JwtTokenProvider jwtTokenProvider
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.chainHeadRepository = chainHeadRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
            .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!"ACTIVE".equals(user.getAccountStatus())) {
            throw new UnauthorizedException("Account is " + user.getAccountStatus().toLowerCase());
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new UnauthorizedException("Account is temporarily locked. Try again later.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int failed = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failed);
            if (failed >= 5) {
                user.setLockedUntil(Instant.now().plusSeconds(900)); // 15 min lockout
            }
            userRepository.save(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Reset failed login counter on password match
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        if (user.isTotpEnabled()) {
            return LoginResponse.requiresTotp(user.getId(), user.getOrganizationId());
        }

        Organization org = organizationRepository.findById(user.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        String token = jwtTokenProvider.generateToken(
            user.getId(),
            user.getOrganizationId(),
            user.getEmail(),
            user.getRole()
        );

        return LoginResponse.authenticated(
            token,
            user.getId(),
            user.getEmail(),
            user.getRole(),
            org.getId(),
            org.getName()
        );
    }

    @Override
    @Transactional
    public LoginResponse verifyTotp(TotpVerifyRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (!user.isTotpEnabled() || user.getTotpSecretEncrypted() == null) {
            throw new UnauthorizedException("TOTP is not enrolled for this account");
        }

        boolean valid = TotpUtil.verifyCode(user.getTotpSecretEncrypted(), request.getCode());
        if (!valid) {
            throw new UnauthorizedException("Invalid TOTP verification code");
        }

        user.setTotpVerifiedAt(Instant.now());
        userRepository.save(user);

        Organization org = organizationRepository.findById(user.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        String token = jwtTokenProvider.generateToken(
            user.getId(),
            user.getOrganizationId(),
            user.getEmail(),
            user.getRole()
        );

        return LoginResponse.authenticated(
            token,
            user.getId(),
            user.getEmail(),
            user.getRole(),
            org.getId(),
            org.getName()
        );
    }

    @Override
    @Transactional
    public TotpEnrollResponse setupTotp(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Organization org = organizationRepository.findById(user.getOrganizationId())
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        String secret = TotpUtil.generateSecretKey();
        user.setTotpSecretEncrypted(secret);
        user.setTotpEnabled(true);
        userRepository.save(user);

        String qrUri = TotpUtil.getOtpAuthUrl(user.getEmail(), org.getName(), secret);
        return new TotpEnrollResponse(secret, qrUri);
    }

    @Override
    public UserSummaryResponse getCurrentUser() {
        UserPrincipal principal = TenantContext.getCurrentUser();
        if (principal == null) {
            throw new UnauthorizedException("No authenticated user");
        }
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
    public RegisterOrganizationResponse registerOrganization(RegisterOrganizationRequest request) {
        String cleanSlug = request.getOrganizationSlug().trim().toLowerCase().replaceAll("[^a-z0-9-]", "-");
        if (organizationRepository.findBySlug(cleanSlug).isPresent()) {
            throw new BadRequestException("Organization slug already exists: " + cleanSlug);
        }

        String cleanEmail = request.getAdminEmail().trim().toLowerCase();
        if (userRepository.findByEmail(cleanEmail).isPresent()) {
            throw new BadRequestException("Admin email already registered: " + cleanEmail);
        }

        // 1. Create Organization
        Organization org = new Organization();
        org.setId(UUID.randomUUID().toString());
        org.setName(request.getOrganizationName().trim());
        org.setSlug(cleanSlug);
        org.setStatus("ACTIVE");
        org = organizationRepository.save(org);

        // 2. Initialize Genesis Chain Head (Seq 0, 64-char zeros hash)
        ChainHead chainHead = new ChainHead();
        chainHead.setOrganizationId(org.getId());
        chainHead.setLatestSequenceNumber(0L);
        chainHead.setLatestEventHash("0000000000000000000000000000000000000000000000000000000000000000");
        chainHeadRepository.save(chainHead);

        // 3. Create Admin User
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID().toString());
        adminUser.setOrganizationId(org.getId());
        adminUser.setEmail(cleanEmail);
        adminUser.setPasswordHash(passwordEncoder.encode(request.getAdminPassword()));
        adminUser.setRole("ADMIN");
        adminUser.setAccountStatus("ACTIVE");
        adminUser.setTotpEnabled(false);
        adminUser = userRepository.save(adminUser);

        // 4. Generate Initial Secret Ingestion API Key (Shown ONCE to user)
        byte[] randomBytes = new byte[24];
        new SecureRandom().nextBytes(randomBytes);
        String rawSecret = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String prefix = "ar_live_" + cleanSlug.substring(0, Math.min(6, cleanSlug.length()));
        String rawApiKey = prefix + "_" + rawSecret;
        String hashedKey = HashUtil.sha256Hex(rawApiKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setId(UUID.randomUUID().toString());
        apiKey.setOrganizationId(org.getId());
        apiKey.setKeyPrefix(prefix);
        apiKey.setKeyHash(hashedKey);
        apiKey.setLabel("Primary Default Ingestion Key");
        apiKey.setStatus("ACTIVE");
        apiKey.setCreatedByUserId(adminUser.getId());
        apiKeyRepository.save(apiKey);

        // 5. Generate Initial JWT Token
        String token = jwtTokenProvider.generateToken(
            adminUser.getId(),
            org.getId(),
            adminUser.getEmail(),
            adminUser.getRole()
        );

        return new RegisterOrganizationResponse(
            org.getId(),
            org.getName(),
            org.getSlug(),
            adminUser.getId(),
            adminUser.getEmail(),
            token,
            rawApiKey,
            prefix,
            "Organization successfully registered! Save your API key securely in your client server environment."
        );
    }
}
