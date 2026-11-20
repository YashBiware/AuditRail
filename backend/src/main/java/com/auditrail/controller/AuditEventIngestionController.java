package com.auditrail.controller;

import com.auditrail.dto.ingest.EventIngestRequest;
import com.auditrail.dto.ingest.EventIngestResponse;
import com.auditrail.security.TenantContext;
import com.auditrail.service.AuditEventIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
public class AuditEventIngestionController {

    private final AuditEventIngestionService ingestionService;

    public AuditEventIngestionController(AuditEventIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<EventIngestResponse> ingestEvent(@Valid @RequestBody EventIngestRequest request) {
        // Organization ID is securely derived from the verified API Key
        String organizationId = TenantContext.getCurrentOrganizationId();
        EventIngestResponse response = ingestionService.ingestEvent(organizationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
