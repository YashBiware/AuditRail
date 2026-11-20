package com.auditrail.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "checkpoints", uniqueConstraints = {
    @UniqueConstraint(name = "uq_checkpoints_org_seq", columnNames = {"organization_id", "sequence_number"})
})
public class Checkpoint {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "organization_id", length = 36, nullable = false)
    private String organizationId;

    @Column(name = "sequence_number", nullable = false)
    private long sequenceNumber;

    @Column(name = "checkpoint_hash", length = 64, nullable = false)
    private String checkpointHash;

    @Column(name = "hash_algorithm", length = 32, nullable = false)
    private String hashAlgorithm = "SHA-256";

    @Column(name = "storage_provider", length = 32, nullable = false)
    private String storageProvider; // MINIO, AMAZON_S3, LOCAL

    @Column(name = "storage_bucket", length = 128, nullable = false)
    private String storageBucket;

    @Column(name = "storage_object_key", length = 512, nullable = false)
    private String storageObjectKey;

    @Column(name = "object_version_reference", length = 128)
    private String objectVersionReference;

    @Column(name = "object_sha256", length = 64, nullable = false)
    private String objectSha256;

    @Column(name = "verification_status", length = 32, nullable = false)
    private String verificationStatus = "PENDING"; // PENDING, VERIFIED, MISMATCH, UNAVAILABLE

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Checkpoint() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public long getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(long sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public String getCheckpointHash() { return checkpointHash; }
    public void setCheckpointHash(String checkpointHash) { this.checkpointHash = checkpointHash; }

    public String getHashAlgorithm() { return hashAlgorithm; }
    public void setHashAlgorithm(String hashAlgorithm) { this.hashAlgorithm = hashAlgorithm; }

    public String getStorageProvider() { return storageProvider; }
    public void setStorageProvider(String storageProvider) { this.storageProvider = storageProvider; }

    public String getStorageBucket() { return storageBucket; }
    public void setStorageBucket(String storageBucket) { this.storageBucket = storageBucket; }

    public String getStorageObjectKey() { return storageObjectKey; }
    public void setStorageObjectKey(String storageObjectKey) { this.storageObjectKey = storageObjectKey; }

    public String getObjectVersionReference() { return objectVersionReference; }
    public void setObjectVersionReference(String objectVersionReference) { this.objectVersionReference = objectVersionReference; }

    public String getObjectSha256() { return objectSha256; }
    public void setObjectSha256(String objectSha256) { this.objectSha256 = objectSha256; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
