package com.auditrail;

import com.auditrail.dto.ingest.EventIngestRequest;
import com.auditrail.dto.verification.VerificationRunResponse;
import com.auditrail.entity.AuditEvent;
import com.auditrail.entity.Organization;
import com.auditrail.repository.AuditEventRepository;
import com.auditrail.repository.OrganizationRepository;
import com.auditrail.service.AuditEventIngestionService;
import com.auditrail.service.IntegrityVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class IntegrityVerificationServiceTest {

    @Autowired
    private AuditEventIngestionService ingestionService;

    @Autowired
    private IntegrityVerificationService verificationService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private String orgId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID().toString();
        Organization org = new Organization(orgId, "Verification Org", "verif-" + orgId.substring(0, 5), "ACTIVE");
        organizationRepository.save(org);
    }

    @Test
    void testCleanChainVerificationSucceeds() {
        ingestionService.ingestEvent(orgId, new EventIngestRequest("actor1", "LOGIN", "session1", Instant.now(), "{}"));
        ingestionService.ingestEvent(orgId, new EventIngestRequest("actor1", "TRANSFER", "acc1", Instant.now(), "{\"amount\":100}"));
        ingestionService.ingestEvent(orgId, new EventIngestRequest("actor1", "LOGOUT", "session1", Instant.now(), "{}"));

        VerificationRunResponse run = verificationService.triggerVerification(orgId, "MANUAL", "admin-1");
        assertEquals("VERIFIED", run.getResult());
        assertEquals(3L, run.getTotalEventsChecked());
        assertNull(run.getFailureSequenceNumber());
    }

    @Test
    void testTamperedEventFailsVerification() {
        ingestionService.ingestEvent(orgId, new EventIngestRequest("actor1", "LOGIN", "session1", Instant.now(), "{}"));
        ingestionService.ingestEvent(orgId, new EventIngestRequest("actor1", "TRANSFER", "acc1", Instant.now(), "{\"amount\":100}"));
        ingestionService.ingestEvent(orgId, new EventIngestRequest("actor1", "LOGOUT", "session1", Instant.now(), "{}"));

        // Tamper directly with event sequence 2
        AuditEvent event2 = auditEventRepository.findByOrganizationIdAndSequenceNumber(orgId, 2L).orElseThrow();
        event2.setActorReference("malicious_attacker");
        auditEventRepository.save(event2);

        VerificationRunResponse run = verificationService.triggerVerification(orgId, "MANUAL", "auditor-1");
        assertEquals("FAILED", run.getResult());
        assertEquals(2L, run.getFailureSequenceNumber());
        assertTrue(run.getFailureReason().contains("tamper detected"));
    }

    @Test
    void testDeletedEventSequenceGapFailsVerification() {
        ingestionService.ingestEvent(orgId, new EventIngestRequest("actor1", "LOGIN", "session1", Instant.now(), "{}"));
        ingestionService.ingestEvent(orgId, new EventIngestRequest("actor1", "TRANSFER", "acc1", Instant.now(), "{\"amount\":100}"));
        ingestionService.ingestEvent(orgId, new EventIngestRequest("actor1", "LOGOUT", "session1", Instant.now(), "{}"));

        // Delete sequence 2 to create a gap
        AuditEvent event2 = auditEventRepository.findByOrganizationIdAndSequenceNumber(orgId, 2L).orElseThrow();
        auditEventRepository.delete(event2);

        VerificationRunResponse run = verificationService.triggerVerification(orgId, "MANUAL", "auditor-1");
        assertEquals("FAILED", run.getResult());
        assertEquals(3L, run.getFailureSequenceNumber()); // Gap detected when sequence 3 is encountered instead of 2
    }

    @Test
    void testCheckpointAnchorVerification() {
        ingestionService.ingestEvent(orgId, new EventIngestRequest("actor1", "LOGIN", "session1", Instant.now(), "{}"));
        ingestionService.ingestEvent(orgId, new EventIngestRequest("actor1", "TRANSFER", "acc1", Instant.now(), "{\"amount\":100}"));

        // Create external checkpoint
        verificationService.createCheckpoint(orgId);

        // Run verification against checkpoint
        VerificationRunResponse run = verificationService.triggerVerification(orgId, "MANUAL", "auditor-1");
        assertEquals("VERIFIED", run.getResult());
        assertNotNull(run.getCheckpointId());
    }
}
