package com.auditrail.dto.user;

import jakarta.validation.constraints.NotBlank;

public class RedeemInvitationRequest {

    @NotBlank(message = "Invitation token is required")
    private String token;

    @NotBlank(message = "Password is required")
    private String password;

    public RedeemInvitationRequest() {}

    public RedeemInvitationRequest(String token, String password) {
        this.token = token;
        this.password = password;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
