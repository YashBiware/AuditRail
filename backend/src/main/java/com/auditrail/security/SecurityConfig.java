package com.auditrail.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ApiKeyAuthenticationFilter apiKeyAuthFilter;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthFilter,
        ApiKeyAuthenticationFilter apiKeyAuthFilter
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.apiKeyAuthFilter = apiKeyAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public Endpoints
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/totp/verify").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/invitations/redeem").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/policy/dpdp").permitAll()
                
                // Ingestion Gateway (Restricted to Valid API Key with ROLE_INGEST)
                .requestMatchers(HttpMethod.POST, "/api/v1/events").hasRole("INGEST")
                
                // Administrative Operations
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/api-keys/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/v1/invitations").hasRole("ADMIN")
                
                // Read & Verification Operations (Admin, Auditor, or Ingest key for Explorer)
                .requestMatchers("/api/v1/audit/**").hasAnyRole("ADMIN", "AUDITOR", "INGEST")
                .requestMatchers("/api/v1/verification/**").hasAnyRole("ADMIN", "AUDITOR", "INGEST")
                .requestMatchers("/api/v1/checkpoints/**").hasAnyRole("ADMIN", "AUDITOR", "INGEST")
                
                // Common Authenticated User & Org Info
                .requestMatchers("/api/v1/auth/**").authenticated()
                .requestMatchers("/api/v1/organizations/**").authenticated()
                
                .anyRequest().authenticated()
            )
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
