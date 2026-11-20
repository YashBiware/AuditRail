package com.auditrail.dto.audit;

import java.time.Instant;

public class AuditSearchCriteria {

    private String actor;
    private String action;
    private String resource;
    private Instant startDate;
    private Instant endDate;
    private int page = 0;
    private int size = 20;

    public AuditSearchCriteria() {}

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getResource() { return resource; }
    public void setResource(String resource) { this.resource = resource; }

    public Instant getStartDate() { return startDate; }
    public void setStartDate(Instant startDate) { this.startDate = startDate; }

    public Instant getEndDate() { return endDate; }
    public void setEndDate(Instant endDate) { this.endDate = endDate; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(0, page); }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = (size > 0 && size <= 100) ? size : 20; }
}
