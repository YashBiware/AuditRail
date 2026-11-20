package com.auditrail.controller;

import com.auditrail.dto.apikey.ApiKeyResponse;
import com.auditrail.dto.apikey.CreateApiKeyRequest;
import com.auditrail.dto.apikey.CreateApiKeyResponse;
import com.auditrail.security.TenantContext;
import com.auditrail.security.UserPrincipal;
import com.auditrail.service.ApiKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<CreateApiKeyResponse> generateApiKey(@Valid @RequestBody CreateApiKeyRequest request) {
        String organizationId = TenantContext.getCurrentOrganizationId();
        UserPrincipal user = TenantContext.getCurrentUser();
        String creatorId = user != null ? user.getId() : null;

        CreateApiKeyResponse response = apiKeyService.generateApiKey(organizationId, request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> listApiKeys() {
        String organizationId = TenantContext.getCurrentOrganizationId();
        List<ApiKeyResponse> list = apiKeyService.listApiKeys(organizationId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeApiKey(@PathVariable String id) {
        String organizationId = TenantContext.getCurrentOrganizationId();
        apiKeyService.revokeApiKey(organizationId, id);
        return ResponseEntity.noContent().build();
    }
}
