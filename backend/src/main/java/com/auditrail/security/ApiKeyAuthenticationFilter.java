package com.auditrail.security;

import com.auditrail.entity.ApiKey;
import com.auditrail.repository.ApiKeyRepository;
import com.auditrail.util.HashUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    public static final String API_KEY_HEADER = "X-AuditRail-API-Key";
    private final ApiKeyRepository apiKeyRepository;

    public ApiKeyAuthenticationFilter(ApiKeyRepository apiKeyRepository) {
        this.apiKeyRepository = apiKeyRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String rawApiKey = request.getHeader(API_KEY_HEADER);

        if (rawApiKey != null && !rawApiKey.trim().isEmpty()) {
            String keyHash = HashUtil.sha256Hex(rawApiKey.trim());
            Optional<ApiKey> apiKeyOpt = apiKeyRepository.findByKeyHashAndStatus(keyHash, "ACTIVE");

            if (apiKeyOpt.isPresent()) {
                ApiKey apiKey = apiKeyOpt.get();
                apiKey.setLastUsedAt(Instant.now());
                apiKeyRepository.save(apiKey);

                ApiKeyAuthenticationToken auth = new ApiKeyAuthenticationToken(apiKey.getId(), apiKey.getOrganizationId());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
