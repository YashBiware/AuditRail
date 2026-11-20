package com.auditrail.dto.auth;

import jakarta.validation.constraints.NotBlank;

public class TotpVerifyRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "TOTP code is required")
    private String code;

    public TotpVerifyRequest() {}

    public TotpVerifyRequest(String userId, String code) {
        this.userId = userId;
        this.code = code;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
