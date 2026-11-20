package com.auditrail.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events", uniqueConstraints = {
    @UniqueConstraint(name = "uq_audit_events_org_seq", columnNames = {"organization_id", "sequence_number"}),
    @UniqueConstraint(name = "uq_audit_events_org_hash", columnNames = {"organization_id", "current_hash"})
}, indexes = {
    @Index(name = "idx_audit_events_org_time", columnList = "organization_id, event_timestamp DESC"),
    @Index(name = "idx_audit_events_org_actor", columnList = "organization_id, actor_reference, event_timestamp DESC"),
    @Index(name = "idx_audit_events_org_action", columnList = "organization_id, action, event_timestamp DESC"),
    @Index(name = "idx_audit_events_org_resource", columnList = "organization_id, resource_reference, event_timestamp DESC")
})
public class AuditEvent {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "sequence_number", nullable = false)
    private long sequenceNumber;

    @Column(name = "actor_reference", length = 255, nullable = false)
    private String actorReference;

    @Column(name = "action", length = 128, nullable = false)
    private String action;

    @Column(name = "resource_reference", length = 255, nullable = false)
    private String resourceReference;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "metadata_json", columnDefinition = "TEXT", nullable = false)
    private String metadataJson;

    @Column(name = "metadata_hash", length = 64, nullable = false)
    private String metadataHash;

    @Column(name = "previous_hash", length = 64, nullable = false)
    private String previousHash;

    @Column(name = "current_hash", length = 64, nullable = false)
    private String currentHash;

    @Column(name = "ingested_at", nullable = false, updatable = false)
    private Instant ingestedAt = Instant.now();

    public AuditEvent() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public long getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(long sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public String getActorReference() { return actorReference; }
    public void setActorReference(String actorReference) { this.actorReference = actorReference; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResourceReference() { return resourceReference; }
    public void setResourceReference(String resourceReference) { this.resourceReference = resourceReference; }

    public Instant getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Instant eventTimestamp) { this.eventTimestamp = eventTimestamp; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }

    public String getMetadataHash() { return metadataHash; }
    public void setMetadataHash(String metadataHash) { this.metadataHash = metadataHash; }

    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }

    public String getCurrentHash() { return currentHash; }
    public void setCurrentHash(String currentHash) { this.currentHash = currentHash; }

    public Instant getIngestedAt() { return ingestedAt; }
    public void setIngestedAt(Instant ingestedAt) { this.ingestedAt = ingestedAt; }
}
