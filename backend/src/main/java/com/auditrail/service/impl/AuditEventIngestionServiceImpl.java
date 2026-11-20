package com.auditrail.service.impl;

import com.auditrail.dto.ingest.EventIngestRequest;
import com.auditrail.dto.ingest.EventIngestResponse;
import com.auditrail.entity.AuditEvent;
import com.auditrail.entity.ChainHead;
import com.auditrail.repository.AuditEventRepository;
import com.auditrail.repository.ChainHeadRepository;
import com.auditrail.service.AuditEventIngestionService;
import com.auditrail.util.CanonicalJsonUtil;
import com.auditrail.util.CanonicalPayloadBuilder;
import com.auditrail.util.HashUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuditEventIngestionServiceImpl implements AuditEventIngestionService {

    private final AuditEventRepository auditEventRepository;
    private final ChainHeadRepository chainHeadRepository;

    public AuditEventIngestionServiceImpl(
        AuditEventRepository auditEventRepository,
        ChainHeadRepository chainHeadRepository
    ) {
        this.auditEventRepository = auditEventRepository;
        this.chainHeadRepository = chainHeadRepository;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public EventIngestResponse ingestEvent(String organizationId, EventIngestRequest request) {
        if (organizationId == null || organizationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Organization ID cannot be null or empty");
        }

        // 1. Acquire pessimistic write lock on the organization's chain head row
        ChainHead chainHead = chainHeadRepository.findByOrganizationIdForUpdate(organizationId)
            .orElseGet(() -> {
                ChainHead newHead = new ChainHead(organizationId, 0L, HashUtil.GENESIS_HASH);
                return chainHeadRepository.save(newHead);
            });

        // 2. Determine sequence and previous hash
        long nextSequence = chainHead.getLatestSequenceNumber() + 1;
        String previousHash = chainHead.getLatestSequenceNumber() == 0L
            ? HashUtil.GENESIS_HASH
            : chainHead.getLatestEventHash();

        // 3. Canonicalize metadata JSON and compute metadata hash
        String canonicalMetadata = CanonicalJsonUtil.canonicalize(request.getMetadataJson());
        String metadataHash = HashUtil.sha256Hex(canonicalMetadata);

        // 4. Calculate deterministic event hash
        Instant rawEventTime = request.getEventTimestamp() != null ? request.getEventTimestamp() : Instant.now();
        Instant eventTime = rawEventTime.truncatedTo(java.time.temporal.ChronoUnit.MICROS);
        String currentHash = CanonicalPayloadBuilder.calculateEventHash(
            organizationId,
            nextSequence,
            request.getActorReference(),
            request.getAction(),
            request.getResourceReference(),
            eventTime,
            metadataHash,
            previousHash
        );

        // 5. Build and persist immutable audit event
        AuditEvent event = new AuditEvent();
        event.setId(UUID.randomUUID().toString());
        event.setOrganizationId(organizationId);
        event.setSequenceNumber(nextSequence);
        event.setActorReference(request.getActorReference());
        event.setAction(request.getAction().toUpperCase());
        event.setResourceReference(request.getResourceReference());
        event.setEventTimestamp(eventTime);
        event.setMetadataJson(canonicalMetadata);
        event.setMetadataHash(metadataHash);
        event.setPreviousHash(previousHash);
        event.setCurrentHash(currentHash);
        event.setIngestedAt(Instant.now());

        auditEventRepository.save(event);

        // 6. Update organization chain head
        chainHead.setLatestSequenceNumber(nextSequence);
        chainHead.setLatestEventHash(currentHash);
        chainHead.setUpdatedAt(Instant.now());
        chainHeadRepository.save(chainHead);

        return new EventIngestResponse(
            event.getId(),
            event.getOrganizationId(),
            event.getSequenceNumber(),
            event.getCurrentHash(),
            event.getPreviousHash(),
            event.getIngestedAt()
        );
    }
}
