package com.auditrail.controller;

import com.auditrail.dto.policy.DpdpSafeguardsResponse;
import com.auditrail.service.PolicyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policy")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping("/dpdp")
    public ResponseEntity<DpdpSafeguardsResponse> getDpdpSafeguards() {
        return ResponseEntity.ok(policyService.getDpdpSafeguards());
    }
}
