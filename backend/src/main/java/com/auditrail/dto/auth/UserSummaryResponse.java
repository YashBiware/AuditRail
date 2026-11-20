package com.auditrail.dto.auth;

import java.time.Instant;

public class UserSummaryResponse {

    private String id;
    private String organizationId;
    private String email;
    private String role;
    private String accountStatus;
    private boolean totpEnabled;
    private Instant createdAt;

    public UserSummaryResponse() {}

    public UserSummaryResponse(
        String id,
        String organizationId,
        String email,
        String role,
        String accountStatus,
        boolean totpEnabled,
        Instant createdAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.email = email;
        this.role = role;
        this.accountStatus = accountStatus;
        this.totpEnabled = totpEnabled;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAccountStatus() { return accountStatus; }
    public void setAccountStatus(String accountStatus) { this.accountStatus = accountStatus; }

    public boolean isTotpEnabled() { return totpEnabled; }
    public void setTotpEnabled(boolean totpEnabled) { this.totpEnabled = totpEnabled; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
