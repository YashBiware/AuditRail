package com.auditrail.service.impl;

import com.auditrail.dto.checkpoint.CheckpointResponse;
import com.auditrail.dto.verification.VerificationRunResponse;
import com.auditrail.entity.*;
import com.auditrail.exception.ResourceNotFoundException;
import com.auditrail.repository.*;
import com.auditrail.service.IntegrityVerificationService;
import com.auditrail.service.adapter.CheckpointStorageService;
import com.auditrail.util.CanonicalJsonUtil;
import com.auditrail.util.CanonicalPayloadBuilder;
import com.auditrail.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class IntegrityVerificationServiceImpl implements IntegrityVerificationService {

    private static final Logger log = LoggerFactory.getLogger(IntegrityVerificationServiceImpl.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AuditEventRepository auditEventRepository;
    private final ChainHeadRepository chainHeadRepository;
    private final CheckpointRepository checkpointRepository;
    private final VerificationRunRepository verificationRunRepository;
    private final OrganizationRepository organizationRepository;
    private final CheckpointStorageService checkpointStorageService;

    public IntegrityVerificationServiceImpl(
        AuditEventRepository auditEventRepository,
        ChainHeadRepository chainHeadRepository,
        CheckpointRepository checkpointRepository,
        VerificationRunRepository verificationRunRepository,
        OrganizationRepository organizationRepository,
        CheckpointStorageService checkpointStorageService
    ) {
        this.auditEventRepository = auditEventRepository;
        this.chainHeadRepository = chainHeadRepository;
        this.checkpointRepository = checkpointRepository;
        this.verificationRunRepository = verificationRunRepository;
        this.organizationRepository = organizationRepository;
        this.checkpointStorageService = checkpointStorageService;
    }

    @Override
    @Transactional
    public VerificationRunResponse triggerVerification(String organizationId, String triggerType, String initiatedByUserId) {
        VerificationRun run = new VerificationRun();
        run.setId(UUID.randomUUID().toString());
        run.setOrganizationId(organizationId);
        run.setTriggerType(triggerType != null ? triggerType : "MANUAL");
        run.setInitiatedByUserId(initiatedByUserId);
        run.setStartedAt(Instant.now());
        run.setResult("PENDING");

        List<AuditEvent> events = auditEventRepository.findAllByOrganizationIdOrderBySequenceNumberAsc(organizationId);

        if (events.isEmpty()) {
            run.setStartSequenceNumber(0L);
            run.setEndSequenceNumber(0L);
            run.setTotalEventsChecked(0L);
            run.setResult("VERIFIED");
            run.setCompletedAt(Instant.now());
            verificationRunRepository.save(run);
            return mapToRunResponse(run);
        }

        run.setStartSequenceNumber(events.get(0).getSequenceNumber());
        run.setEndSequenceNumber(events.get(events.size() - 1).getSequenceNumber());
        run.setTotalEventsChecked(events.size());

        // Level 1: Internal Hash-Chain Continuity Walk
        String priorHash = HashUtil.GENESIS_HASH;
        long expectedSequence = 1L;

        for (AuditEvent event : events) {
            // Check for missing sequence (gap detection)
            if (event.getSequenceNumber() != expectedSequence) {
                return recordFailure(
                    run,
                    event.getSequenceNumber(),
                    "Sequence gap detected: expected sequence " + expectedSequence + " but found " + event.getSequenceNumber(),
                    "SEQUENCE_GAP"
                );
            }

            // Verify previous hash link
            if (!HashUtil.constantTimeEquals(event.getPreviousHash(), priorHash)) {
                return recordFailure(
                    run,
                    event.getSequenceNumber(),
                    "Hash continuity broken at sequence " + event.getSequenceNumber() + ". Stored previous hash does not match prior event hash.",
                    "CHAIN_LINK_MISMATCH"
                );
            }

            // Recompute metadata hash
            String canonicalMeta = CanonicalJsonUtil.canonicalize(event.getMetadataJson());
            String recomputedMetaHash = HashUtil.sha256Hex(canonicalMeta);
            if (!HashUtil.constantTimeEquals(event.getMetadataHash(), recomputedMetaHash)) {
                return recordFailure(
                    run,
                    event.getSequenceNumber(),
                    "Metadata tamper detected at sequence " + event.getSequenceNumber() + ". Metadata hash does not match canonical payload.",
                    "METADATA_TAMPER"
                );
            }

            // Recompute current event hash
            String recomputedHash = CanonicalPayloadBuilder.calculateEventHash(
                organizationId,
                event.getSequenceNumber(),
                event.getActorReference(),
                event.getAction(),
                event.getResourceReference(),
                event.getEventTimestamp(),
                event.getMetadataHash(),
                event.getPreviousHash()
            );

            if (!HashUtil.constantTimeEquals(event.getCurrentHash(), recomputedHash)) {
                return recordFailure(
                    run,
                    event.getSequenceNumber(),
                    "Content tamper detected at sequence " + event.getSequenceNumber() + ". Event hash does not match recalculated digest.",
                    "CONTENT_TAMPER"
                );
            }

            priorHash = event.getCurrentHash();
            expectedSequence++;
        }

        // Level 2: External Checkpoint Anchor Reconciliation
        Optional<Checkpoint> latestCheckpointOpt = checkpointRepository.findTopByOrganizationIdOrderBySequenceNumberDesc(organizationId);
        if (latestCheckpointOpt.isPresent()) {
            Checkpoint cp = latestCheckpointOpt.get();
            run.setCheckpointId(cp.getId());

            if (!checkpointStorageService.isAvailable()) {
                run.setResult("CHECKPOINT_UNAVAILABLE");
                run.setFailureReason("External checkpoint storage service is currently unreachable");
                run.setCompletedAt(Instant.now());
                verificationRunRepository.save(run);
                return mapToRunResponse(run);
            }

            try {
                // Fetch external object and verify SHA-256
                String content = checkpointStorageService.retrieveCheckpointContent(cp.getStorageBucket(), cp.getStorageObjectKey());
                String objectHash = HashUtil.sha256Hex(content);

                if (!HashUtil.constantTimeEquals(cp.getObjectSha256(), objectHash)) {
                    return recordFailure(
                        run,
                        cp.getSequenceNumber(),
                        "External checkpoint object hash mismatch. File may have been altered in storage.",
                        "CHECKPOINT_OBJECT_ALTERED"
                    );
                }

                // Verify checkpoint hash against database event hash at that sequence
                Optional<AuditEvent> cpEventOpt = auditEventRepository.findByOrganizationIdAndSequenceNumber(organizationId, cp.getSequenceNumber());
                if (cpEventOpt.isEmpty() || !HashUtil.constantTimeEquals(cp.getCheckpointHash(), cpEventOpt.get().getCurrentHash())) {
                    return recordFailure(
                        run,
                        cp.getSequenceNumber(),
                        "Database state does not match external checkpoint. Database was rewritten after checkpoint snapshot!",
                        "DATABASE_REWRITTEN"
                    );
                }
            } catch (Exception ex) {
                log.warn("Checkpoint retrieval failed during verification", ex);
                run.setResult("CHECKPOINT_UNAVAILABLE");
                run.setFailureReason("Failed to read external checkpoint: " + ex.getMessage());
                run.setCompletedAt(Instant.now());
                verificationRunRepository.save(run);
                return mapToRunResponse(run);
            }
        }

        run.setResult("VERIFIED");
        run.setCompletedAt(Instant.now());
        verificationRunRepository.save(run);
        return mapToRunResponse(run);
    }

    @Override
    @Transactional
    public CheckpointResponse createCheckpoint(String organizationId) {
        ChainHead chainHead = chainHeadRepository.findByOrganizationId(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("No chain head found for organization"));

        if (chainHead.getLatestSequenceNumber() == 0L) {
            throw new IllegalArgumentException("Cannot create checkpoint for empty chain");
        }

        String objectKey = String.format("checkpoints/%s/cp_seq_%d.json", organizationId, chainHead.getLatestSequenceNumber());

        CheckpointStorageService.CheckpointData data = new CheckpointStorageService.CheckpointData(
            organizationId,
            chainHead.getLatestSequenceNumber(),
            chainHead.getLatestEventHash(),
            "SHA-256",
            Instant.now().toString(),
            "LOCAL",
            "auditrail-checkpoints",
            objectKey
        );

        CheckpointStorageService.StoredCheckpointResult stored = checkpointStorageService.storeCheckpoint(data);

        Checkpoint cp = new Checkpoint();
        cp.setId(UUID.randomUUID().toString());
        cp.setOrganizationId(organizationId);
        cp.setSequenceNumber(chainHead.getLatestSequenceNumber());
        cp.setCheckpointHash(chainHead.getLatestEventHash());
        cp.setHashAlgorithm("SHA-256");
        cp.setStorageProvider(stored.storageProvider());
        cp.setStorageBucket(stored.bucket());
        cp.setStorageObjectKey(stored.objectKey());
        cp.setObjectVersionReference(stored.objectVersionReference());
        cp.setObjectSha256(stored.objectSha256());
        cp.setVerificationStatus("VERIFIED");
        cp.setCreatedAt(Instant.now());

        checkpointRepository.save(cp);
        return mapToCheckpointResponse(cp);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerificationRunResponse> getVerificationHistory(String organizationId) {
        return verificationRunRepository.findAllByOrganizationIdOrderByStartedAtDesc(organizationId)
            .stream().map(this::mapToRunResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckpointResponse> getCheckpointHistory(String organizationId) {
        return checkpointRepository.findAllByOrganizationIdOrderBySequenceNumberDesc(organizationId)
            .stream().map(this::mapToCheckpointResponse).toList();
    }

    @Override
    @Scheduled(cron = "${auditrail.scheduling.verification-cron:0 0 * * * *}")
    public void runScheduledVerification() {
        log.info("Starting scheduled hourly integrity verification across all organizations...");
        List<Organization> orgs = organizationRepository.findAll();
        for (Organization org : orgs) {
            try {
                triggerVerification(org.getId(), "SCHEDULED_HOURLY", null);
            } catch (Exception e) {
                log.error("Scheduled verification failed for organization: {}", org.getId(), e);
            }
        }
    }

    private VerificationRunResponse recordFailure(
        VerificationRun run,
        long failureSeq,
        String reason,
        String failureType
    ) {
        run.setResult("FAILED");
        run.setFailureSequenceNumber(failureSeq);
        run.setFailureReason(reason);
        run.setCompletedAt(Instant.now());

        Map<String, Object> details = new HashMap<>();
        details.put("failureType", failureType);
        details.put("sequenceNumber", failureSeq);
        details.put("timestamp", Instant.now().toString());
        try {
            run.setFailureDetails(MAPPER.writeValueAsString(details));
        } catch (Exception ignored) {}

        verificationRunRepository.save(run);
        return mapToRunResponse(run);
    }

    private VerificationRunResponse mapToRunResponse(VerificationRun run) {
        VerificationRunResponse resp = new VerificationRunResponse();
        resp.setId(run.getId());
        resp.setOrganizationId(run.getOrganizationId());
        resp.setTriggerType(run.getTriggerType());
        resp.setStartedAt(run.getStartedAt());
        resp.setCompletedAt(run.getCompletedAt());
        resp.setStartSequenceNumber(run.getStartSequenceNumber());
        resp.setEndSequenceNumber(run.getEndSequenceNumber());
        resp.setTotalEventsChecked(run.getTotalEventsChecked());
        resp.setResult(run.getResult());
        resp.setCheckpointId(run.getCheckpointId());
        resp.setFailureSequenceNumber(run.getFailureSequenceNumber());
        resp.setFailureReason(run.getFailureReason());
        resp.setFailureDetails(run.getFailureDetails());
        return resp;
    }

    private CheckpointResponse mapToCheckpointResponse(Checkpoint cp) {
        CheckpointResponse resp = new CheckpointResponse();
        resp.setId(cp.getId());
        resp.setOrganizationId(cp.getOrganizationId());
        resp.setSequenceNumber(cp.getSequenceNumber());
        resp.setCheckpointHash(cp.getCheckpointHash());
        resp.setHashAlgorithm(cp.getHashAlgorithm());
        resp.setStorageProvider(cp.getStorageProvider());
        resp.setStorageBucket(cp.getStorageBucket());
        resp.setStorageObjectKey(cp.getStorageObjectKey());
        resp.setObjectVersionReference(cp.getObjectVersionReference());
        resp.setVerificationStatus(cp.getVerificationStatus());
        resp.setCreatedAt(cp.getCreatedAt());
        return resp;
    }
}
