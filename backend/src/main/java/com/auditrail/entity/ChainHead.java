package com.auditrail.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chain_heads")
public class ChainHead {

    @Id
    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "latest_sequence_number", nullable = false)
    private long latestSequenceNumber = 0L;

    @Column(name = "latest_event_hash", length = 64, nullable = false)
    private String latestEventHash = "0000000000000000000000000000000000000000000000000000000000000000";

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ChainHead() {}

    public ChainHead(String organizationId, long latestSequenceNumber, String latestEventHash) {
        this.organizationId = organizationId;
        this.latestSequenceNumber = latestSequenceNumber;
        this.latestEventHash = latestEventHash;
        this.updatedAt = Instant.now();
    }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public long getLatestSequenceNumber() { return latestSequenceNumber; }
    public void setLatestSequenceNumber(long latestSequenceNumber) { this.latestSequenceNumber = latestSequenceNumber; }

    public String getLatestEventHash() { return latestEventHash; }
    public void setLatestEventHash(String latestEventHash) { this.latestEventHash = latestEventHash; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
