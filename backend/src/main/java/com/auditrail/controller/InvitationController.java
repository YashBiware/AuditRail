package com.auditrail.controller;

import com.auditrail.dto.user.CreateInvitationRequest;
import com.auditrail.dto.user.InvitationResponse;
import com.auditrail.dto.user.RedeemInvitationRequest;
import com.auditrail.security.TenantContext;
import com.auditrail.security.UserPrincipal;
import com.auditrail.service.InvitationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invitations")
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping
    public ResponseEntity<InvitationResponse> createInvitation(@Valid @RequestBody CreateInvitationRequest request) {
        String organizationId = TenantContext.getCurrentOrganizationId();
        UserPrincipal user = TenantContext.getCurrentUser();
        String creatorId = user != null ? user.getId() : organizationId;

        InvitationResponse response = invitationService.createInvitation(organizationId, request, creatorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<InvitationResponse>> listInvitations() {
        String organizationId = TenantContext.getCurrentOrganizationId();
        List<InvitationResponse> list = invitationService.listInvitations(organizationId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/redeem")
    public ResponseEntity<Void> redeemInvitation(@Valid @RequestBody RedeemInvitationRequest request) {
        invitationService.redeemInvitation(request);
        return ResponseEntity.noContent().build();
    }
}
