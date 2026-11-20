package com.auditrail.service.impl;

import com.auditrail.dto.apikey.ApiKeyResponse;
import com.auditrail.dto.apikey.CreateApiKeyRequest;
import com.auditrail.dto.apikey.CreateApiKeyResponse;
import com.auditrail.entity.ApiKey;
import com.auditrail.exception.ResourceNotFoundException;
import com.auditrail.repository.ApiKeyRepository;
import com.auditrail.service.ApiKeyService;
import com.auditrail.util.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class ApiKeyServiceImpl implements ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyServiceImpl(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    @Transactional
    public CreateApiKeyResponse generateApiKey(String organizationId, CreateApiKeyRequest request, String createdByUserId) {
        byte[] prefixBytes = new byte[4];
        byte[] secretBytes = new byte[16];
        secureRandom.nextBytes(prefixBytes);
        secureRandom.nextBytes(secretBytes);

        String prefixHex = HexFormat.of().formatHex(prefixBytes);
        String secretHex = HexFormat.of().formatHex(secretBytes);

        String keyPrefix = "ar_live_" + prefixHex;
        String rawApiKey = keyPrefix + "_" + secretHex;
        String keyHash = HashUtil.sha256Hex(rawApiKey);

        ApiKey apiKey = new ApiKey();
        apiKey.setId(UUID.randomUUID().toString());
        apiKey.setOrganizationId(organizationId);
        apiKey.setKeyPrefix(keyPrefix);
        apiKey.setKeyHash(keyHash);
        apiKey.setLabel(request.getLabel().trim());
        apiKey.setStatus("ACTIVE");
        apiKey.setCreatedByUserId(createdByUserId);
        apiKey.setCreatedAt(Instant.now());

        apiKeyRepository.save(apiKey);

        return new CreateApiKeyResponse(
            apiKey.getId(),
            apiKey.getLabel(),
            apiKey.getKeyPrefix(),
            rawApiKey,
            apiKey.getCreatedAt()
        );
    }

    @Override
    @Transactional
    public void revokeApiKey(String organizationId, String apiKeyId) {
        ApiKey apiKey = apiKeyRepository.findById(apiKeyId)
            .filter(k -> k.getOrganizationId().equals(organizationId))
            .orElseThrow(() -> new ResourceNotFoundException("API key not found"));

        apiKey.setStatus("REVOKED");
        apiKey.setRevokedAt(Instant.now());
        apiKeyRepository.save(apiKey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiKeyResponse> listApiKeys(String organizationId) {
        return apiKeyRepository.findAllByOrganizationId(organizationId).stream()
            .map(k -> new ApiKeyResponse(
                k.getId(),
                k.getLabel(),
                k.getKeyPrefix(),
                k.getStatus(),
                k.getCreatedAt(),
                k.getRevokedAt(),
                k.getLastUsedAt()
            ))
            .toList();
    }
}
