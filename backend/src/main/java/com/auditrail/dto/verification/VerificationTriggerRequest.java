package com.auditrail.dto.verification;

public class VerificationTriggerRequest {

    private String triggerType = "MANUAL";

    public VerificationTriggerRequest() {}

    public VerificationTriggerRequest(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
}
