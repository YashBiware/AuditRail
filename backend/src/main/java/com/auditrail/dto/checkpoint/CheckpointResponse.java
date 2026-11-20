package com.auditrail.dto.checkpoint;

import java.time.Instant;

public class CheckpointResponse {

    private String id;
    private String organizationId;
    private long sequenceNumber;
    private String checkpointHash;
    private String hashAlgorithm;
    private String storageProvider;
    private String storageBucket;
    private String storageObjectKey;
    private String objectVersionReference;
    private String verificationStatus;
    private Instant createdAt;

    public CheckpointResponse() {}

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

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
