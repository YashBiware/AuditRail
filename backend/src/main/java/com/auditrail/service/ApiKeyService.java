package com.auditrail.service;

import com.auditrail.dto.apikey.ApiKeyResponse;
import com.auditrail.dto.apikey.CreateApiKeyRequest;
import com.auditrail.dto.apikey.CreateApiKeyResponse;

import java.util.List;

public interface ApiKeyService {

    CreateApiKeyResponse generateApiKey(String organizationId, CreateApiKeyRequest request, String createdByUserId);

    void revokeApiKey(String organizationId, String apiKeyId);

    List<ApiKeyResponse> listApiKeys(String organizationId);
}
