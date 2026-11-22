package com.auditrail.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 128, message = "Organization name must be between 2 and 128 characters")
    private String organizationName;

    @NotBlank(message = "Organization slug is required")
    @Size(min = 2, max = 64, message = "Slug must be between 2 and 64 characters")
    private String organizationSlug;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Valid corporate email is required")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    @Size(min = 8, max = 64, message = "Password must be at least 8 characters")
    private String adminPassword;

    public RegisterOrganizationRequest() {}

    public RegisterOrganizationRequest(String organizationName, String organizationSlug, String adminEmail, String adminPassword) {
        this.organizationName = organizationName;
        this.organizationSlug = organizationSlug;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getOrganizationSlug() { return organizationSlug; }
    public void setOrganizationSlug(String organizationSlug) { this.organizationSlug = organizationSlug; }

    public String getAdminEmail() { return adminEmail; }
    public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }

    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
}
