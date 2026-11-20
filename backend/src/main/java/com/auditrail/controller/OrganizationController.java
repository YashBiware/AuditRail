package com.auditrail.controller;

import com.auditrail.entity.Organization;
import com.auditrail.security.TenantContext;
import com.auditrail.service.OrganizationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/current")
    public ResponseEntity<Organization> getCurrentOrganization() {
        String organizationId = TenantContext.getCurrentOrganizationId();
        Organization org = organizationService.getOrganization(organizationId);
        return ResponseEntity.ok(org);
    }
}
