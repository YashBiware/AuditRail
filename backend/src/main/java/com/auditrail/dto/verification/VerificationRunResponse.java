package com.auditrail.dto.verification;

import java.time.Instant;

public class VerificationRunResponse {

    private String id;
    private String organizationId;
    private String triggerType;
    private Instant startedAt;
    private Instant completedAt;
    private long startSequenceNumber;
    private long endSequenceNumber;
    private long totalEventsChecked;
    private String result; // PENDING, VERIFIED, FAILED, INCOMPLETE, CHECKPOINT_UNAVAILABLE
    private String checkpointId;
    private Long failureSequenceNumber;
    private String failureReason;
    private String failureDetails;

    public VerificationRunResponse() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public long getStartSequenceNumber() { return startSequenceNumber; }
    public void setStartSequenceNumber(long startSequenceNumber) { this.startSequenceNumber = startSequenceNumber; }

    public long getEndSequenceNumber() { return endSequenceNumber; }
    public void setEndSequenceNumber(long endSequenceNumber) { this.endSequenceNumber = endSequenceNumber; }

    public long getTotalEventsChecked() { return totalEventsChecked; }
    public void setTotalEventsChecked(long totalEventsChecked) { this.totalEventsChecked = totalEventsChecked; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getCheckpointId() { return checkpointId; }
    public void setCheckpointId(String checkpointId) { this.checkpointId = checkpointId; }

    public Long getFailureSequenceNumber() { return failureSequenceNumber; }
    public void setFailureSequenceNumber(Long failureSequenceNumber) { this.failureSequenceNumber = failureSequenceNumber; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public String getFailureDetails() { return failureDetails; }
    public void setFailureDetails(String failureDetails) { this.failureDetails = failureDetails; }
}
