package com.auditrail;

import com.auditrail.dto.ingest.EventIngestRequest;
import com.auditrail.dto.ingest.EventIngestResponse;
import com.auditrail.entity.ChainHead;
import com.auditrail.entity.Organization;
import com.auditrail.repository.AuditEventRepository;
import com.auditrail.repository.ChainHeadRepository;
import com.auditrail.repository.OrganizationRepository;
import com.auditrail.service.AuditEventIngestionService;
import com.auditrail.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuditEventIngestionServiceTest {

    @Autowired
    private AuditEventIngestionService ingestionService;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private ChainHeadRepository chainHeadRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    private String orgId;

    @BeforeEach
    void setUp() {
        orgId = UUID.randomUUID().toString();
        Organization org = new Organization(orgId, "Test Org " + orgId.substring(0, 5), "slug-" + orgId.substring(0, 5), "ACTIVE");
        organizationRepository.save(org);
    }

    @Test
    void testSequentialEventIngestionAndHashChaining() {
        // Event 1 (Genesis event)
        EventIngestRequest req1 = new EventIngestRequest(
            "user_alice",
            "USER_LOGIN",
            "auth:session_1",
            Instant.now(),
            "{\"ip\":\"10.0.0.1\"}"
        );

        EventIngestResponse res1 = ingestionService.ingestEvent(orgId, req1);
        assertNotNull(res1);
        assertEquals(1L, res1.getSequenceNumber());
        assertEquals(HashUtil.GENESIS_HASH, res1.getPreviousHash());
        assertNotNull(res1.getCurrentHash());

        // Event 2 (Chained to Event 1)
        EventIngestRequest req2 = new EventIngestRequest(
            "user_alice",
            "DOCUMENT_UPDATE",
            "doc:doc_101",
            Instant.now(),
            "{\"diff\":\"title changed\"}"
        );

        EventIngestResponse res2 = ingestionService.ingestEvent(orgId, req2);
        assertNotNull(res2);
        assertEquals(2L, res2.getSequenceNumber());
        assertEquals(res1.getCurrentHash(), res2.getPreviousHash());

        // Verify Chain Head matches Event 2
        ChainHead head = chainHeadRepository.findByOrganizationId(orgId).orElseThrow();
        assertEquals(2L, head.getLatestSequenceNumber());
        assertEquals(res2.getCurrentHash(), head.getLatestEventHash());
    }
}
