package com.auditrail.dto.auth;

public class RegisterOrganizationResponse {

    private String organizationId;
    private String organizationName;
    private String organizationSlug;
    private String adminUserId;
    private String adminEmail;
    private String token;
    private String rawApiKey; // Displayed ONCE to the user upon registration
    private String keyPrefix;
    private String message;

    public RegisterOrganizationResponse() {}

    public RegisterOrganizationResponse(
            String organizationId,
            String organizationName,
            String organizationSlug,
            String adminUserId,
            String adminEmail,
            String token,
            String rawApiKey,
            String keyPrefix,
            String message
    ) {
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.organizationSlug = organizationSlug;
        this.adminUserId = adminUserId;
        this.adminEmail = adminEmail;
        this.token = token;
        this.rawApiKey = rawApiKey;
        this.keyPrefix = keyPrefix;
        this.message = message;
    }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getOrganizationSlug() { return organizationSlug; }
    public void setOrganizationSlug(String organizationSlug) { this.organizationSlug = organizationSlug; }

    public String getAdminUserId() { return adminUserId; }
    public void setAdminUserId(String adminUserId) { this.adminUserId = adminUserId; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRawApiKey() { return rawApiKey; }
    public void setRawApiKey(String rawApiKey) { this.rawApiKey = rawApiKey; }

    public String getKeyPrefix() { return keyPrefix; }
    public void setKeyPrefix(String keyPrefix) { this.keyPrefix = keyPrefix; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
