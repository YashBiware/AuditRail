package com.auditrail.dto.apikey;

import java.time.Instant;

public class CreateApiKeyResponse {

    private String id;
    private String label;
    private String keyPrefix;
    private String rawApiKey; // ONLY RETURNED ONCE!
    private Instant createdAt;

    public CreateApiKeyResponse() {}

    public CreateApiKeyResponse(String id, String label, String keyPrefix, String rawApiKey, Instant createdAt) {
        this.id = id;
        this.label = label;
        this.keyPrefix = keyPrefix;
        this.rawApiKey = rawApiKey;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }

    public String getRawApiKey() { return rawApiKey; }
    public void setRawApiKey(String rawApiKey) { this.rawApiKey = rawApiKey; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
