package com.auditrail.service;

import com.auditrail.dto.auth.LoginRequest;
import com.auditrail.dto.auth.LoginResponse;
import com.auditrail.dto.auth.TotpEnrollResponse;
import com.auditrail.dto.auth.TotpVerifyRequest;
import com.auditrail.dto.auth.UserSummaryResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse verifyTotp(TotpVerifyRequest request);

    TotpEnrollResponse setupTotp(String userId);

    UserSummaryResponse getCurrentUser();

    com.auditrail.dto.auth.RegisterOrganizationResponse registerOrganization(com.auditrail.dto.auth.RegisterOrganizationRequest request);
}
