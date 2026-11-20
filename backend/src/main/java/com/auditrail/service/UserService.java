package com.auditrail.service;

import com.auditrail.dto.auth.UserSummaryResponse;
import com.auditrail.dto.user.CreateUserRequest;

import java.util.List;

public interface UserService {

    UserSummaryResponse createUser(String organizationId, CreateUserRequest request);

    void setUserStatus(String organizationId, String userId, String status);

    List<UserSummaryResponse> listUsers(String organizationId);
}
