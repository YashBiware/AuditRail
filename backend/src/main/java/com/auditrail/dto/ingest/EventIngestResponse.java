package com.auditrail.dto.ingest;

import java.time.Instant;

public class EventIngestResponse {

    private String id;
    private String organizationId;
    private long sequenceNumber;
    private String currentHash;
    private String previousHash;
    private Instant ingestedAt;

    public EventIngestResponse() {}

    public EventIngestResponse(
        String id,
        String organizationId,
        long sequenceNumber,
        String currentHash,
        String previousHash,
        Instant ingestedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.sequenceNumber = sequenceNumber;
        this.currentHash = currentHash;
        this.previousHash = previousHash;
        this.ingestedAt = ingestedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public long getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(long sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public String getCurrentHash() { return currentHash; }
    public void setCurrentHash(String currentHash) { this.currentHash = currentHash; }

    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }

    public Instant getIngestedAt() { return ingestedAt; }
    public void setIngestedAt(Instant ingestedAt) { this.ingestedAt = ingestedAt; }
}
