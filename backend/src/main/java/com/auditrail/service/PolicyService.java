package com.auditrail.service;

import com.auditrail.dto.policy.DpdpSafeguardsResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PolicyService {

    public DpdpSafeguardsResponse getDpdpSafeguards() {
        List<DpdpSafeguardsResponse.SafeguardItem> technical = List.of(
            new DpdpSafeguardsResponse.SafeguardItem(
                "Sec 8(5) - Reasonable Security Safeguards",
                "Cryptographic Hash-Chain Integrity",
                "Every ingested audit event is chained with SHA-256 digests referencing its predecessor, making unauthorized alteration or deletion immediately detectable."
            ),
            new DpdpSafeguardsResponse.SafeguardItem(
                "Sec 8(5) - Independent Checkpointing",
                "Out-of-Band WORM Checkpoint Anchors",
                "Periodic chain-head snapshots are exported to separate write-once-read-many (WORM) storage (MinIO / S3 Object Lock) preventing database-level tampering."
            ),
            new DpdpSafeguardsResponse.SafeguardItem(
                "Sec 9 - Personal Data Integrity",
                "Canonical Serialization (RFC 8785)",
                "Contextual event metadata is canonicalized and hashed deterministically, preventing serialization discrepancies across client platforms."
            ),
            new DpdpSafeguardsResponse.SafeguardItem(
                "Sec 8(4) - Access Control & Authentication",
                "Role-Based Access Control & Mandatory TOTP",
                "All platform human operators are protected by BCrypt password hashing and time-based one-time password (TOTP) multi-factor authentication."
            )
        );

        List<DpdpSafeguardsResponse.SafeguardItem> organizational = List.of(
            new DpdpSafeguardsResponse.SafeguardItem(
                "Dual-Role Segregation",
                "Strict Administrator and Auditor Role Separation",
                "Auditors possess strictly read-only access to audit logs and verification reports; administrative configuration is isolated to Administrators."
            ),
            new DpdpSafeguardsResponse.SafeguardItem(
                "Automated Verification Protocol",
                "Scheduled Hourly Integrity Audits",
                "Automated hourly verification runs walk the entire chain and compare against external checkpoints, logging any variance without exposing internal database errors."
            ),
            new DpdpSafeguardsResponse.SafeguardItem(
                "Tenant Boundary Enforcement",
                "Cryptographic API Key Organization Resolution",
                "External event callers authenticate via hashed API keys; callers are never trusted to specify their own organization identifier."
            )
        );

        return new DpdpSafeguardsResponse(
            "Digital Personal Data Protection (DPDP) Act, 2023",
            "1.0-ENTERPRISE",
            "AuditRail platform technical and organizational safeguards designed to protect personal and business audit trail integrity.",
            technical,
            organizational
        );
    }
}
