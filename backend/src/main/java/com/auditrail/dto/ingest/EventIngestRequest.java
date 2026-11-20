package com.auditrail.dto.ingest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class EventIngestRequest {

    @NotBlank(message = "Actor reference is required")
    private String actorReference;

    @NotBlank(message = "Action is required")
    private String action;

    @NotBlank(message = "Resource reference is required")
    private String resourceReference;

    @NotNull(message = "Event timestamp is required")
    private Instant eventTimestamp;

    private String metadataJson = "{}";

    public EventIngestRequest() {}

    public EventIngestRequest(
        String actorReference,
        String action,
        String resourceReference,
        Instant eventTimestamp,
        String metadataJson
    ) {
        this.actorReference = actorReference;
        this.action = action;
        this.resourceReference = resourceReference;
        this.eventTimestamp = eventTimestamp;
        this.metadataJson = metadataJson != null ? metadataJson : "{}";
    }

    public String getActorReference() { return actorReference; }
    public void setActorReference(String actorReference) { this.actorReference = actorReference; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResourceReference() { return resourceReference; }
    public void setResourceReference(String resourceReference) { this.resourceReference = resourceReference; }

    public Instant getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Instant eventTimestamp) { this.eventTimestamp = eventTimestamp; }

    public String getMetadataJson() { return metadataJson; }
    public void setMetadataJson(String metadataJson) { this.metadataJson = metadataJson; }
}
