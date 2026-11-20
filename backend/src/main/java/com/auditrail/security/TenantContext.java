package com.auditrail.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class TenantContext {

    private TenantContext() {}

    public static String getCurrentOrganizationId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated security context found");
        }

        if (auth instanceof ApiKeyAuthenticationToken apiKeyAuth) {
            return apiKeyAuth.getOrganizationId();
        }

        if (auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getOrganizationId();
        }

        throw new IllegalStateException("Authentication principal does not contain organization context");
    }

    public static UserPrincipal getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }
        return null;
    }
}
