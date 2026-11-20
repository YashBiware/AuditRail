package com.auditrail.service.adapter;

import com.auditrail.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
public class LocalFilesystemCheckpointStorageAdapter implements CheckpointStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFilesystemCheckpointStorageAdapter.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String localDir;
    private final String bucketName;

    public LocalFilesystemCheckpointStorageAdapter(
        @Value("${auditrail.storage.local-dir:./checkpoints_storage}") String localDir,
        @Value("${auditrail.storage.minio.bucket:auditrail-checkpoints}") String bucketName
    ) {
        this.localDir = localDir;
        this.bucketName = bucketName;
    }

    @Override
    public StoredCheckpointResult storeCheckpoint(CheckpointData data) {
        try {
            String jsonContent = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            byte[] bytes = jsonContent.getBytes(StandardCharsets.UTF_8);
            String objectSha256 = HashUtil.sha256Hex(bytes);

            Path targetPath = Paths.get(localDir, bucketName, data.objectKey());
            Files.createDirectories(targetPath.getParent());

            // Write to external storage
            Files.write(targetPath, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            String versionRef = "local-v-" + UUID.randomUUID().toString().substring(0, 8);
            log.info("Stored external checkpoint object at: {}", targetPath.toAbsolutePath());

            return new StoredCheckpointResult(
                "LOCAL",
                bucketName,
                data.objectKey(),
                versionRef,
                objectSha256
            );
        } catch (IOException e) {
            log.error("Failed to store checkpoint object externally", e);
            throw new IllegalStateException("Failed to persist external checkpoint object", e);
        }
    }

    @Override
    public String retrieveCheckpointContent(String bucket, String objectKey) {
        try {
            Path targetPath = Paths.get(localDir, bucket != null ? bucket : bucketName, objectKey);
            if (!Files.exists(targetPath)) {
                throw new IllegalArgumentException("Checkpoint object not found: " + objectKey);
            }
            return Files.readString(targetPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to retrieve external checkpoint object: {}", objectKey, e);
            throw new IllegalStateException("External checkpoint read failure", e);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            Path targetDir = Paths.get(localDir);
            Files.createDirectories(targetDir);
            return Files.isWritable(targetDir);
        } catch (Exception e) {
            return false;
        }
    }
}
