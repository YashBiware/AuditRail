package com.auditrail.controller;

import com.auditrail.dto.auth.*;
import com.auditrail.security.TenantContext;
import com.auditrail.security.UserPrincipal;
import com.auditrail.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/totp/verify")
    public ResponseEntity<LoginResponse> verifyTotp(@Valid @RequestBody TotpVerifyRequest request) {
        LoginResponse response = authService.verifyTotp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/totp/setup")
    public ResponseEntity<TotpEnrollResponse> setupTotp() {
        UserPrincipal user = TenantContext.getCurrentUser();
        TotpEnrollResponse response = authService.setupTotp(user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryResponse> getCurrentUser() {
        UserSummaryResponse response = authService.getCurrentUser();
        return ResponseEntity.ok(response);
    }
}
