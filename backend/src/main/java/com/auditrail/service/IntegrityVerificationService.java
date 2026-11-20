package com.auditrail.service;

import com.auditrail.dto.checkpoint.CheckpointResponse;
import com.auditrail.dto.verification.VerificationRunResponse;

import java.util.List;

public interface IntegrityVerificationService {

    VerificationRunResponse triggerVerification(String organizationId, String triggerType, String initiatedByUserId);

    CheckpointResponse createCheckpoint(String organizationId);

    List<VerificationRunResponse> getVerificationHistory(String organizationId);

    List<CheckpointResponse> getCheckpointHistory(String organizationId);

    void runScheduledVerification();
}
