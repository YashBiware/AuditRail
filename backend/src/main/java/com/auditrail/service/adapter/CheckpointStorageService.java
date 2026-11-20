package com.auditrail.service.adapter;

public interface CheckpointStorageService {

    record CheckpointData(
        String organizationId,
        long sequenceNumber,
        String checkpointHash,
        String hashAlgorithm,
        String createdAtIsoUtc,
        String storageProvider,
        String bucket,
        String objectKey
    ) {}

    record StoredCheckpointResult(
        String storageProvider,
        String bucket,
        String objectKey,
        String objectVersionReference,
        String objectSha256
    ) {}

    StoredCheckpointResult storeCheckpoint(CheckpointData data);

    String retrieveCheckpointContent(String bucket, String objectKey);

    boolean isAvailable();
}
