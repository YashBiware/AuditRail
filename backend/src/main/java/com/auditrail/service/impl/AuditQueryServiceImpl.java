package com.auditrail.service.impl;

import com.auditrail.dto.audit.AuditEventResponse;
import com.auditrail.dto.audit.AuditSearchCriteria;
import com.auditrail.dto.audit.PageResponse;
import com.auditrail.entity.AuditEvent;
import com.auditrail.exception.ResourceNotFoundException;
import com.auditrail.repository.AuditEventRepository;
import com.auditrail.service.AuditQueryService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditQueryServiceImpl implements AuditQueryService {

    private final AuditEventRepository auditEventRepository;

    public AuditQueryServiceImpl(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditEventResponse> searchEvents(String organizationId, AuditSearchCriteria criteria) {
        Specification<AuditEvent> spec = buildSpecification(organizationId, criteria);
        Pageable pageable = PageRequest.of(
            criteria.getPage(),
            criteria.getSize(),
            Sort.by(Sort.Direction.DESC, "sequenceNumber")
        );

        Page<AuditEvent> page = auditEventRepository.findAll(spec, pageable);
        List<AuditEventResponse> dtos = page.getContent().stream().map(this::mapToResponse).toList();

        return new PageResponse<>(
            dtos,
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber(),
            page.getSize()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuditEventResponse getEventDetail(String organizationId, String eventId) {
        AuditEvent event = auditEventRepository.findByOrganizationIdAndId(organizationId, eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Audit event not found for id: " + eventId));
        return mapToResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportEventsToCsv(String organizationId, AuditSearchCriteria criteria) {
        Specification<AuditEvent> spec = buildSpecification(organizationId, criteria);
        Sort sort = Sort.by(Sort.Direction.ASC, "sequenceNumber");
        List<AuditEvent> events = auditEventRepository.findAll(spec, sort);

        StringBuilder sb = new StringBuilder();
        // RFC 4180 CSV Header
        sb.append("SequenceNumber,EventTimestamp,Actor,Action,Resource,PreviousHash,CurrentHash,MetadataHash,MetadataJson\n");

        for (AuditEvent e : events) {
            sb.append(e.getSequenceNumber()).append(",")
              .append(escapeCsv(e.getEventTimestamp().toString())).append(",")
              .append(escapeCsv(e.getActorReference())).append(",")
              .append(escapeCsv(e.getAction())).append(",")
              .append(escapeCsv(e.getResourceReference())).append(",")
              .append(escapeCsv(e.getPreviousHash())).append(",")
              .append(escapeCsv(e.getCurrentHash())).append(",")
              .append(escapeCsv(e.getMetadataHash())).append(",")
              .append(escapeCsv(e.getMetadataJson())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Specification<AuditEvent> buildSpecification(String organizationId, AuditSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Mandatory tenant boundary
            predicates.add(cb.equal(root.get("organizationId"), organizationId));

            if (criteria.getActor() != null && !criteria.getActor().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("actorReference")), "%" + criteria.getActor().trim().toLowerCase() + "%"));
            }
            if (criteria.getAction() != null && !criteria.getAction().trim().isEmpty()) {
                predicates.add(cb.equal(cb.upper(root.get("action")), criteria.getAction().trim().toUpperCase()));
            }
            if (criteria.getResource() != null && !criteria.getResource().trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("resourceReference")), "%" + criteria.getResource().trim().toLowerCase() + "%"));
            }
            if (criteria.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventTimestamp"), criteria.getStartDate()));
            }
            if (criteria.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventTimestamp"), criteria.getEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private AuditEventResponse mapToResponse(AuditEvent event) {
        AuditEventResponse resp = new AuditEventResponse();
        resp.setId(event.getId());
        resp.setOrganizationId(event.getOrganizationId());
        resp.setSequenceNumber(event.getSequenceNumber());
        resp.setActorReference(event.getActorReference());
        resp.setAction(event.getAction());
        resp.setResourceReference(event.getResourceReference());
        resp.setEventTimestamp(event.getEventTimestamp());
        resp.setMetadataJson(event.getMetadataJson());
        resp.setMetadataHash(event.getMetadataHash());
        resp.setPreviousHash(event.getPreviousHash());
        resp.setCurrentHash(event.getCurrentHash());
        resp.setIngestedAt(event.getIngestedAt());
        return resp;
    }
}
