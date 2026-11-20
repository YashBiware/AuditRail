package com.auditrail.controller;

import com.auditrail.dto.checkpoint.CheckpointResponse;
import com.auditrail.security.TenantContext;
import com.auditrail.service.IntegrityVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/checkpoints")
public class CheckpointController {

    private final IntegrityVerificationService verificationService;

    public CheckpointController(IntegrityVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping
    public ResponseEntity<CheckpointResponse> createCheckpoint() {
        String organizationId = TenantContext.getCurrentOrganizationId();
        CheckpointResponse response = verificationService.createCheckpoint(organizationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CheckpointResponse>> listCheckpoints() {
        String organizationId = TenantContext.getCurrentOrganizationId();
        List<CheckpointResponse> checkpoints = verificationService.getCheckpointHistory(organizationId);
        return ResponseEntity.ok(checkpoints);
    }
}
