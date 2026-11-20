package com.auditrail.service.impl;

import com.auditrail.entity.Organization;
import com.auditrail.exception.ResourceNotFoundException;
import com.auditrail.repository.OrganizationRepository;
import com.auditrail.service.OrganizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Organization getOrganization(String organizationId) {
        return organizationRepository.findById(organizationId)
            .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }
}
