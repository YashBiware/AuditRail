package com.auditrail.dto.user;

import java.time.Instant;

public class InvitationResponse {

    private String id;
    private String email;
    private String role;
    private String rawToken;
    private String inviteUrl;
    private String status;
    private Instant expiresAt;

    public InvitationResponse() {}

    public InvitationResponse(
        String id,
        String email,
        String role,
        String rawToken,
        String inviteUrl,
        String status,
        Instant expiresAt
    ) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.rawToken = rawToken;
        this.inviteUrl = inviteUrl;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getRawToken() { return rawToken; }
    public void setRawToken(String rawToken) { this.rawToken = rawToken; }

    public String getInviteUrl() { return inviteUrl; }
    public void setInviteUrl(String inviteUrl) { this.inviteUrl = inviteUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
