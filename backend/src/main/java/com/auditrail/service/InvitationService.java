package com.auditrail.service;

import com.auditrail.dto.user.CreateInvitationRequest;
import com.auditrail.dto.user.InvitationResponse;
import com.auditrail.dto.user.RedeemInvitationRequest;

import java.util.List;

public interface InvitationService {

    InvitationResponse createInvitation(String organizationId, CreateInvitationRequest request, String createdByUserId);

    void redeemInvitation(RedeemInvitationRequest request);

    List<InvitationResponse> listInvitations(String organizationId);
}
