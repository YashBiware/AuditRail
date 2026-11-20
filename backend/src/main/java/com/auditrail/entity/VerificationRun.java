package com.auditrail.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "verification_runs")
public class VerificationRun {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "trigger_type", length = 32, nullable = false)
    private String triggerType; // MANUAL, SCHEDULED_HOURLY

    @Column(name = "initiated_by_user_id", length = 36)
    private String initiatedByUserId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "start_sequence_number", nullable = false)
    private long startSequenceNumber;

    @Column(name = "end_sequence_number", nullable = false)
    private long endSequenceNumber;

    @Column(name = "total_events_checked", nullable = false)
    private long totalEventsChecked = 0L;

    @Column(name = "result", length = 32, nullable = false)
    private String result = "PENDING"; // PENDING, VERIFIED, FAILED, INCOMPLETE, CHECKPOINT_UNAVAILABLE

    @Column(name = "checkpoint_id", length = 36)
    private String checkpointId;

    @Column(name = "failure_sequence_number")
    private Long failureSequenceNumber;

    @Column(name = "failure_reason", length = 512)
    private String failureReason;

    @Lob
    @Column(name = "failure_details", columnDefinition = "JSON")
    private String failureDetails;

    public VerificationRun() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }

    public String getInitiatedByUserId() { return initiatedByUserId; }
    public void setInitiatedByUserId(String initiatedByUserId) { this.initiatedByUserId = initiatedByUserId; }

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
