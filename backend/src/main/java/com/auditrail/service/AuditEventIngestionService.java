package com.auditrail.service;

import com.auditrail.dto.ingest.EventIngestRequest;
import com.auditrail.dto.ingest.EventIngestResponse;

public interface AuditEventIngestionService {

    EventIngestResponse ingestEvent(String organizationId, EventIngestRequest request);
}
