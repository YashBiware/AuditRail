package com.auditrail.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String apiKeyId;
    private final String organizationId;

    public ApiKeyAuthenticationToken(String apiKeyId, String organizationId) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_INGEST")));
        this.apiKeyId = apiKeyId;
        this.organizationId = organizationId;
        setAuthenticated(true);
    }

    public String getApiKeyId() { return apiKeyId; }
    public String getOrganizationId() { return organizationId; }

    @Override
    public Object getCredentials() { return null; }

    @Override
    public Object getPrincipal() { return organizationId; }
}
