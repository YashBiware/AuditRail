package com.auditrail.controller;

import com.auditrail.dto.audit.AuditEventResponse;
import com.auditrail.dto.audit.AuditSearchCriteria;
import com.auditrail.dto.audit.PageResponse;
import com.auditrail.security.TenantContext;
import com.auditrail.service.AuditQueryService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditExplorerController {

    private final AuditQueryService auditQueryService;

    public AuditExplorerController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping("/events")
    public ResponseEntity<PageResponse<AuditEventResponse>> listEvents(
        @RequestParam(required = false) String actor,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String resource,
        @RequestParam(required = false) Instant startDate,
        @RequestParam(required = false) Instant endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        String organizationId = TenantContext.getCurrentOrganizationId();

        AuditSearchCriteria criteria = new AuditSearchCriteria();
        criteria.setActor(actor);
        criteria.setAction(action);
        criteria.setResource(resource);
        criteria.setStartDate(startDate);
        criteria.setEndDate(endDate);
        criteria.setPage(page);
        criteria.setSize(size);

        PageResponse<AuditEventResponse> response = auditQueryService.searchEvents(organizationId, criteria);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<AuditEventResponse> getEventDetail(@PathVariable String id) {
        String organizationId = TenantContext.getCurrentOrganizationId();
        AuditEventResponse response = auditQueryService.getEventDetail(organizationId, id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv(
        @RequestParam(required = false) String actor,
        @RequestParam(required = false) String action,
        @RequestParam(required = false) String resource,
        @RequestParam(required = false) Instant startDate,
        @RequestParam(required = false) Instant endDate
    ) {
        String organizationId = TenantContext.getCurrentOrganizationId();

        AuditSearchCriteria criteria = new AuditSearchCriteria();
        criteria.setActor(actor);
        criteria.setAction(action);
        criteria.setResource(resource);
        criteria.setStartDate(startDate);
        criteria.setEndDate(endDate);

        byte[] csvBytes = auditQueryService.exportEventsToCsv(organizationId, criteria);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"auditrail_export_" + organizationId + ".csv\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csvBytes);
    }
}
