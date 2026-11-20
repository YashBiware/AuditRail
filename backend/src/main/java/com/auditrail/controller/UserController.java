package com.auditrail.controller;

import com.auditrail.dto.auth.UserSummaryResponse;
import com.auditrail.dto.user.CreateUserRequest;
import com.auditrail.security.TenantContext;
import com.auditrail.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> listUsers() {
        String organizationId = TenantContext.getCurrentOrganizationId();
        List<UserSummaryResponse> users = userService.listUsers(organizationId);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<UserSummaryResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        String organizationId = TenantContext.getCurrentOrganizationId();
        UserSummaryResponse response = userService.createUser(organizationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateUserStatus(
        @PathVariable String id,
        @RequestParam String status
    ) {
        String organizationId = TenantContext.getCurrentOrganizationId();
        userService.setUserStatus(organizationId, id, status);
        return ResponseEntity.noContent().build();
    }
}
