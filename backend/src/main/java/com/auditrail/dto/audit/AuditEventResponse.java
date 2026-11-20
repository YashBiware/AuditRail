package com.auditrail.dto.audit;

import java.time.Instant;

public class AuditEventResponse {

    private String id;
    private String organizationId;
    private long sequenceNumber;
    private String actorReference;
    private String action;
    private String resourceReference;
    private Instant eventTimestamp;
    private String metadataJson;
    private String metadataHash;
    private String previousHash;
    private String currentHash;
    private Instant ingestedAt;

    public AuditEventResponse() {}

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
