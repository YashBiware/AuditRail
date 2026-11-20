package com.auditrail.service;

import com.auditrail.dto.audit.AuditEventResponse;
import com.auditrail.dto.audit.AuditSearchCriteria;
import com.auditrail.dto.audit.PageResponse;

public interface AuditQueryService {

    PageResponse<AuditEventResponse> searchEvents(String organizationId, AuditSearchCriteria criteria);

    AuditEventResponse getEventDetail(String organizationId, String eventId);

    byte[] exportEventsToCsv(String organizationId, AuditSearchCriteria criteria);
}
