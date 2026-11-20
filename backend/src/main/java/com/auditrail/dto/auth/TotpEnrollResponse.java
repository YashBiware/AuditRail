package com.auditrail.dto.auth;

public class TotpEnrollResponse {

    private String secret;
    private String qrUri;

    public TotpEnrollResponse() {}

    public TotpEnrollResponse(String secret, String qrUri) {
        this.secret = secret;
        this.qrUri = qrUri;
    }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public String getQrUri() { return qrUri; }
    public void setQrUri(String qrUri) { this.qrUri = qrUri; }
}
