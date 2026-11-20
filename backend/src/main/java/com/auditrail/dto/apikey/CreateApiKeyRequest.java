package com.auditrail.dto.apikey;

import jakarta.validation.constraints.NotBlank;

public class CreateApiKeyRequest {

    @NotBlank(message = "Label is required")
    private String label;

    public CreateApiKeyRequest() {}

    public CreateApiKeyRequest(String label) {
        this.label = label;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
}
