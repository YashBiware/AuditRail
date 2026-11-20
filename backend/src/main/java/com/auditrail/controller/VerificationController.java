package com.auditrail.controller;

import com.auditrail.dto.verification.VerificationRunResponse;
import com.auditrail.dto.verification.VerificationTriggerRequest;
import com.auditrail.security.TenantContext;
import com.auditrail.security.UserPrincipal;
import com.auditrail.service.IntegrityVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/verification")
public class VerificationController {

    private final IntegrityVerificationService verificationService;

    public VerificationController(IntegrityVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/run")
    public ResponseEntity<VerificationRunResponse> triggerVerification(
        @RequestBody(required = false) VerificationTriggerRequest request
    ) {
        String organizationId = TenantContext.getCurrentOrganizationId();
        UserPrincipal user = TenantContext.getCurrentUser();
        String userId = user != null ? user.getId() : null;
        String triggerType = (request != null && request.getTriggerType() != null) ? request.getTriggerType() : "MANUAL";

        VerificationRunResponse response = verificationService.triggerVerification(organizationId, triggerType, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    public ResponseEntity<List<VerificationRunResponse>> getHistory() {
        String organizationId = TenantContext.getCurrentOrganizationId();
        List<VerificationRunResponse> history = verificationService.getVerificationHistory(organizationId);
        return ResponseEntity.ok(history);
    }
}
