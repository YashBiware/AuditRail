package com.auditrail.dto.apikey;

import java.time.Instant;

public class ApiKeyResponse {

    private String id;
    private String label;
    private String keyPrefix;
    private String status;
    private Instant createdAt;
    private Instant revokedAt;
    private Instant lastUsedAt;

    public ApiKeyResponse() {}

    public ApiKeyResponse(
        String id,
        String label,
        String keyPrefix,
        String status,
        Instant createdAt,
        Instant revokedAt,
        Instant lastUsedAt
    ) {
        this.id = id;
        this.label = label;
        this.keyPrefix = keyPrefix;
        this.status = status;
        this.createdAt = createdAt;
        this.revokedAt = revokedAt;
        this.lastUsedAt = lastUsedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }
}
