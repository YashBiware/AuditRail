-- =============================================================================
-- AuditRail — Enterprise Audit Integrity Platform
-- Database Schema Definition (MySQL 8.0+)
-- Architecture: Multi-Tenant Isolated, Append-Only Tamper-Evident Hash Chaining
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- 1. Organizations Table
-- -----------------------------------------------------------------------------
-- Core tenant entity. Every audit event, user, API key, chain head, checkpoint,
-- and verification run belongs strictly to one organization.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS organizations (
    id VARCHAR(36) NOT NULL COMMENT 'UUID v4 unique tenant identifier',
    name VARCHAR(128) NOT NULL COMMENT 'Organization legal / business name',
    slug VARCHAR(64) NOT NULL COMMENT 'URL-safe unique organization identifier',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Tenant status: ACTIVE, SUSPENDED, DEACTIVATED',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Tenant creation timestamp in UTC',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp in UTC',
    
    PRIMARY KEY (id),
    UNIQUE KEY uq_organizations_slug (slug),
    INDEX idx_organizations_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Multi-tenant organization boundary';

-- -----------------------------------------------------------------------------
-- 2. Users Table
-- -----------------------------------------------------------------------------
-- Stores authenticated enterprise operators (ADMIN or AUDITOR).
-- Email uniqueness is scoped per organization to support organization-level identity.
-- Passwords are store strictly as BCrypt hashes. MFA uses TOTP secrets.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) NOT NULL COMMENT 'UUID v4 user identifier',
    organization_id VARCHAR(36) NOT NULL COMMENT 'Tenant boundary foreign key',
    email VARCHAR(255) NOT NULL COMMENT 'User corporate email address',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password (cost factor >= 12)',
    role VARCHAR(32) NOT NULL COMMENT 'Role-based access: ADMIN, AUDITOR',
    account_status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Status: ACTIVE, DISABLED, PENDING_INVITE',
    totp_secret_encrypted VARCHAR(512) NULL COMMENT 'AES-256-GCM encrypted TOTP secret key',
    totp_enabled BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether TOTP MFA is enforced for this user',
    totp_verified_at DATETIME(6) NULL COMMENT 'Timestamp when TOTP was successfully enrolled',
    failed_login_attempts INT NOT NULL DEFAULT 0 COMMENT 'Counter for brute-force lockouts',
    locked_until DATETIME(6) NULL COMMENT 'Account lockout expiration timestamp in UTC',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Account creation timestamp in UTC',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Last update timestamp in UTC',
    
    PRIMARY KEY (id),
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id) 
        REFERENCES organizations (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    UNIQUE KEY uq_users_org_email (organization_id, email),
    INDEX idx_users_org_role (organization_id, role),
    INDEX idx_users_org_status (organization_id, account_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Enterprise operators with RBAC and TOTP';

-- -----------------------------------------------------------------------------
-- 3. API Keys Table
-- -----------------------------------------------------------------------------
-- Ingestion credentials for external client applications (SAP, Core Banking, etc.).
-- Raw API key is shown exactly ONCE to the admin upon generation.
-- The database stores only the SHA-256 hash of the key secret.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS api_keys (
    id VARCHAR(36) NOT NULL COMMENT 'UUID v4 API key record identifier',
    organization_id VARCHAR(36) NOT NULL COMMENT 'Tenant boundary foreign key',
    key_prefix VARCHAR(16) NOT NULL COMMENT 'Public prefix for identification (e.g. ar_live_a1b2c3d4)',
    key_hash CHAR(64) NOT NULL COMMENT 'SHA-256 hex digest of the raw API secret',
    label VARCHAR(128) NOT NULL COMMENT 'Human-readable descriptor (e.g. SAP Gateway, ERP Ingestion)',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Key state: ACTIVE, REVOKED',
    created_by_user_id VARCHAR(36) NULL COMMENT 'Admin user who generated this key',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Key generation timestamp in UTC',
    revoked_at DATETIME(6) NULL COMMENT 'Key revocation timestamp in UTC',
    last_used_at DATETIME(6) NULL COMMENT 'Last successful event ingestion timestamp in UTC',
    
    PRIMARY KEY (id),
    CONSTRAINT fk_api_keys_organization FOREIGN KEY (organization_id) 
        REFERENCES organizations (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_api_keys_creator FOREIGN KEY (created_by_user_id) 
        REFERENCES users (id) ON DELETE SET NULL ON UPDATE RESTRICT,
    UNIQUE KEY uq_api_keys_hash (key_hash),
    INDEX idx_api_keys_prefix (key_prefix),
    INDEX idx_api_keys_org_status (organization_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Hashed ingestion credentials for external systems';

-- -----------------------------------------------------------------------------
-- 4. Chain Heads Table
-- -----------------------------------------------------------------------------
-- Represents the current cryptographic tail of each organization''s hash chain.
-- High-concurrency anchor: updated using pessimistic row locking (SELECT ... FOR UPDATE)
-- to serialize event appending strictly per organization without cross-tenant delays.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS chain_heads (
    organization_id VARCHAR(36) NOT NULL COMMENT 'Tenant identifier and PRIMARY KEY (1:1 with organizations)',
    latest_sequence_number BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Monotonically increasing sequence number',
    latest_event_hash CHAR(64) NOT NULL COMMENT 'SHA-256 hex digest of the latest appended event',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT 'Timestamp of last chain head update',
    
    PRIMARY KEY (organization_id),
    CONSTRAINT fk_chain_heads_organization FOREIGN KEY (organization_id) 
        REFERENCES organizations (id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Current cryptographic tail of each organization hash chain';

-- -----------------------------------------------------------------------------
-- 5. Audit Events Table (Append-Only)
-- -----------------------------------------------------------------------------
-- Primary immutable audit log store. Records are append-only.
-- Every record contains its monotonic sequence number, actor, action, resource,
-- canonical metadata hash, previous SHA-256 event hash, and current event hash.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_events (
    id VARCHAR(36) NOT NULL COMMENT 'UUID v4 event record identifier',
    organization_id VARCHAR(36) NOT NULL COMMENT 'Tenant boundary foreign key',
    sequence_number BIGINT UNSIGNED NOT NULL COMMENT 'Monotonically increasing sequence number per organization',
    actor_reference VARCHAR(255) NOT NULL COMMENT 'Identity that triggered the event (user, service, cron)',
    action VARCHAR(128) NOT NULL COMMENT 'Action code (e.g. USER_LOGIN, FINANCIAL_RECORD_UPDATE)',
    resource_reference VARCHAR(255) NOT NULL COMMENT 'Entity affected (e.g. account:acc_9812, doc:doc_45)',
    event_timestamp DATETIME(6) NOT NULL COMMENT 'Event generation timestamp from source application in UTC',
    metadata_json JSON NOT NULL COMMENT 'Arbitrary structured contextual attributes',
    metadata_hash CHAR(64) NOT NULL COMMENT 'SHA-256 hex digest of RFC 8785 canonical metadata JSON',
    previous_hash CHAR(64) NOT NULL COMMENT 'SHA-256 hash of event (sequence_number - 1); 64 zeros for seq 1',
    current_hash CHAR(64) NOT NULL COMMENT 'SHA-256 hash of canonical event payload + previous_hash',
    ingested_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Platform ingestion timestamp in UTC',
    
    PRIMARY KEY (id),
    CONSTRAINT fk_audit_events_organization FOREIGN KEY (organization_id) 
        REFERENCES organizations (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    UNIQUE KEY uq_audit_events_org_seq (organization_id, sequence_number),
    UNIQUE KEY uq_audit_events_org_hash (organization_id, current_hash),
    INDEX idx_audit_events_org_time (organization_id, event_timestamp DESC),
    INDEX idx_audit_events_org_actor (organization_id, actor_reference, event_timestamp DESC),
    INDEX idx_audit_events_org_action (organization_id, action, event_timestamp DESC),
    INDEX idx_audit_events_org_resource (organization_id, resource_reference, event_timestamp DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Immutable, cryptographically chained audit events';

-- -----------------------------------------------------------------------------
-- 6. Checkpoints Table
-- -----------------------------------------------------------------------------
-- Stores METADATA about external, independent checkpoint objects stored in MinIO/S3.
-- Note: Storing metadata here does not substitute the external WORM/Object Lock store.
-- The external object holds the immutable proof; this table allows indexing and fast lookup.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS checkpoints (
    id VARCHAR(36) NOT NULL COMMENT 'UUID v4 checkpoint record identifier',
    organization_id VARCHAR(36) NOT NULL COMMENT 'Tenant boundary foreign key',
    sequence_number BIGINT UNSIGNED NOT NULL COMMENT 'Sequence number of the chain head at checkpoint creation',
    checkpoint_hash CHAR(64) NOT NULL COMMENT 'Chain head hash snapshot at checkpoint creation',
    hash_algorithm VARCHAR(32) NOT NULL DEFAULT 'SHA-256' COMMENT 'Cryptographic hash algorithm used',
    storage_provider VARCHAR(32) NOT NULL COMMENT 'Provider: MINIO (local) or AMAZON_S3 (cloud)',
    storage_bucket VARCHAR(128) NOT NULL COMMENT 'Bucket or container name',
    storage_object_key VARCHAR(512) NOT NULL COMMENT 'Unique object key path in external storage',
    object_version_reference VARCHAR(128) NULL COMMENT 'S3 / MinIO object version ID for immutability verification',
    object_sha256 CHAR(64) NOT NULL COMMENT 'SHA-256 integrity hash of the exported checkpoint JSON document',
    verification_status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'Status: PENDING, VERIFIED, MISMATCH, UNAVAILABLE',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Checkpoint creation timestamp in UTC',
    
    PRIMARY KEY (id),
    CONSTRAINT fk_checkpoints_organization FOREIGN KEY (organization_id) 
        REFERENCES organizations (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    UNIQUE KEY uq_checkpoints_org_seq (organization_id, sequence_number),
    INDEX idx_checkpoints_org_created (organization_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Metadata references to external WORM-protected checkpoints';

-- -----------------------------------------------------------------------------
-- 7. Invitations Table
-- -----------------------------------------------------------------------------
-- Single-use invitation tokens created manually by Admin to invite new users.
-- Raw token is shared via URL; only the SHA-256 hash of the token is stored.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS invitations (
    id VARCHAR(36) NOT NULL COMMENT 'UUID v4 invitation record identifier',
    organization_id VARCHAR(36) NOT NULL COMMENT 'Tenant boundary foreign key',
    email VARCHAR(255) NOT NULL COMMENT 'Target invitee email address',
    role VARCHAR(32) NOT NULL COMMENT 'Target assigned role: ADMIN, AUDITOR',
    token_hash CHAR(64) NOT NULL COMMENT 'SHA-256 hex digest of the raw one-time invitation token',
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'State: PENDING, ACCEPTED, EXPIRED, REVOKED',
    expires_at DATETIME(6) NOT NULL COMMENT 'Token expiration timestamp in UTC (default 48h)',
    used_at DATETIME(6) NULL COMMENT 'Timestamp when invitation was redeemed',
    created_by_user_id VARCHAR(36) NOT NULL COMMENT 'Admin user who generated this invitation',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Invitation creation timestamp in UTC',
    
    PRIMARY KEY (id),
    CONSTRAINT fk_invitations_organization FOREIGN KEY (organization_id) 
        REFERENCES organizations (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_invitations_creator FOREIGN KEY (created_by_user_id) 
        REFERENCES users (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    UNIQUE KEY uq_invitations_token_hash (token_hash),
    INDEX idx_invitations_org_email (organization_id, email),
    INDEX idx_invitations_org_status (organization_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Single-use onboarding invitations for enterprise users';

-- -----------------------------------------------------------------------------
-- 8. Verification Runs Table
-- -----------------------------------------------------------------------------
-- Records audit integrity verification jobs (manual trigger or hourly scheduled).
-- Checks hash-chain continuity from sequence 1..N and verifies against external checkpoints.
-- Results distinguish VERIFIED, FAILED, PENDING, INCOMPLETE, and CHECKPOINT_UNAVAILABLE.
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS verification_runs (
    id VARCHAR(36) NOT NULL COMMENT 'UUID v4 verification run identifier',
    organization_id VARCHAR(36) NOT NULL COMMENT 'Tenant boundary foreign key',
    trigger_type VARCHAR(32) NOT NULL COMMENT 'Trigger mechanism: MANUAL, SCHEDULED_HOURLY',
    initiated_by_user_id VARCHAR(36) NULL COMMENT 'Admin or Auditor who triggered manual run; NULL for scheduled',
    started_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT 'Verification job start timestamp in UTC',
    completed_at DATETIME(6) NULL COMMENT 'Verification job completion timestamp in UTC',
    start_sequence_number BIGINT UNSIGNED NOT NULL COMMENT 'Lower bound sequence number inspected',
    end_sequence_number BIGINT UNSIGNED NOT NULL COMMENT 'Upper bound sequence number inspected',
    total_events_checked BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Total event count validated in this run',
    result VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT 'Outcome: PENDING, VERIFIED, FAILED, INCOMPLETE, CHECKPOINT_UNAVAILABLE',
    checkpoint_id VARCHAR(36) NULL COMMENT 'Checkpoint against which verification was anchored, if applicable',
    failure_sequence_number BIGINT UNSIGNED NULL COMMENT 'Sequence number where integrity breach was detected',
    failure_reason VARCHAR(512) NULL COMMENT 'Sanitized summary of the failure (no raw DB errors exposed)',
    failure_details JSON NULL COMMENT 'Structured details of the failure (expected hash, actual hash, discrepancy type)',
    
    PRIMARY KEY (id),
    CONSTRAINT fk_verification_runs_org FOREIGN KEY (organization_id) 
        REFERENCES organizations (id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_verification_runs_user FOREIGN KEY (initiated_by_user_id) 
        REFERENCES users (id) ON DELETE SET NULL ON UPDATE RESTRICT,
    CONSTRAINT fk_verification_runs_checkpoint FOREIGN KEY (checkpoint_id) 
        REFERENCES checkpoints (id) ON DELETE SET NULL ON UPDATE RESTRICT,
    INDEX idx_verification_runs_org_time (organization_id, started_at DESC),
    INDEX idx_verification_runs_org_result (organization_id, result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Audit integrity verification history and tamper evidence logs';

SET FOREIGN_KEY_CHECKS = 1;
