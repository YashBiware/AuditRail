package com.auditrail.dto.auth;

public class LoginResponse {

    private String token;
    private boolean requiresTotp;
    private String userId;
    private String email;
    private String role;
    private String organizationId;
    private String organizationName;

    public LoginResponse() {}

    public static LoginResponse requiresTotp(String userId, String organizationId) {
        LoginResponse resp = new LoginResponse();
        resp.setRequiresTotp(true);
        resp.setUserId(userId);
        resp.setOrganizationId(organizationId);
        return resp;
    }

    public static LoginResponse authenticated(
        String token,
        String userId,
        String email,
        String role,
        String organizationId,
        String organizationName
    ) {
        LoginResponse resp = new LoginResponse();
        resp.setRequiresTotp(false);
        resp.setToken(token);
        resp.setUserId(userId);
        resp.setEmail(email);
        resp.setRole(role);
        resp.setOrganizationId(organizationId);
        resp.setOrganizationName(organizationName);
        return resp;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public boolean isRequiresTotp() { return requiresTotp; }
    public void setRequiresTotp(boolean requiresTotp) { this.requiresTotp = requiresTotp; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
}
