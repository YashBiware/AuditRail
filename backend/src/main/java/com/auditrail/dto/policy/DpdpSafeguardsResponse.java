package com.auditrail.dto.policy;

import java.util.List;

public class DpdpSafeguardsResponse {

    private String standard;
    private String version;
    private String description;
    private List<SafeguardItem> technicalSafeguards;
    private List<SafeguardItem> organizationalSafeguards;

    public DpdpSafeguardsResponse() {}

    public DpdpSafeguardsResponse(
        String standard,
        String version,
        String description,
        List<SafeguardItem> technicalSafeguards,
        List<SafeguardItem> organizationalSafeguards
    ) {
        this.standard = standard;
        this.version = version;
        this.description = description;
        this.technicalSafeguards = technicalSafeguards;
        this.organizationalSafeguards = organizationalSafeguards;
    }

    public static class SafeguardItem {
        private String clause;
        private String title;
        private String implementation;

        public SafeguardItem() {}

        public SafeguardItem(String clause, String title, String implementation) {
            this.clause = clause;
            this.title = title;
            this.implementation = implementation;
        }

        public String getClause() { return clause; }
        public void setClause(String clause) { this.clause = clause; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getImplementation() { return implementation; }
        public void setImplementation(String implementation) { this.implementation = implementation; }
    }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<SafeguardItem> getTechnicalSafeguards() { return technicalSafeguards; }
    public void setTechnicalSafeguards(List<SafeguardItem> technicalSafeguards) { this.technicalSafeguards = technicalSafeguards; }

    public List<SafeguardItem> getOrganizationalSafeguards() { return organizationalSafeguards; }
    public void setOrganizationalSafeguards(List<SafeguardItem> organizationalSafeguards) { this.organizationalSafeguards = organizationalSafeguards; }
}
