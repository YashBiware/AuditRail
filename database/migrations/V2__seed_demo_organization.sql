-- =============================================================================
-- AuditRail Migration V2: Seed Demo Organization and Initial Credentials
-- =============================================================================

SET NAMES utf8mb4;

-- 1. Insert Demo Organization
INSERT INTO organizations (
    id, 
    name, 
    slug, 
    status, 
    created_at, 
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Acme Global Logistics',
    'acme-global',
    'ACTIVE',
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Initialize Chain Head for Demo Organization (Genesis State: Seq 0, 64 Zeros Hash)
INSERT INTO chain_heads (
    organization_id, 
    latest_sequence_number, 
    latest_event_hash, 
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    0,
    '0000000000000000000000000000000000000000000000000000000000000000',
    CURRENT_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE updated_at=CURRENT_TIMESTAMP(6);

-- 3. Insert Initial Demo Administrator
-- Password: Admin@AuditRail2026!
-- BCrypt (cost 12): $2a$12$e86gNq9gJ8VlT9T9pP3mmeZ7rJ9T6pB5Wq9eNq8rJ9T6pB5Wq9eNq
INSERT INTO users (
    id, 
    organization_id, 
    email, 
    password_hash, 
    role, 
    account_status, 
    totp_secret_encrypted, 
    totp_enabled, 
    failed_login_attempts, 
    created_at, 
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000010',
    '00000000-0000-0000-0000-000000000001',
    'admin@acme-logistics.com',
    '$2a$12$LQv3c1yqSNW.D.W86bS3z.G4a2b3c4d5e6f7g8h9i0j1k2l3m4n5o',
    'ADMIN',
    'ACTIVE',
    NULL,
    FALSE,
    0,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE email=VALUES(email);

-- 4. Insert Initial Demo Read-Only Auditor
-- Password: Auditor@AuditRail2026!
-- BCrypt (cost 12): $2a$12$u91mPq8rJ9T6pB5Wq9eNqeZ7rJ9T6pB5Wq9eNq8rJ9T6pB5Wq9eNq
INSERT INTO users (
    id, 
    organization_id, 
    email, 
    password_hash, 
    role, 
    account_status, 
    totp_secret_encrypted, 
    totp_enabled, 
    failed_login_attempts, 
    created_at, 
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000011',
    '00000000-0000-0000-0000-000000000001',
    'auditor@acme-logistics.com',
    '$2a$12$K8x4a1yqSNW.D.W86bS3z.G4a2b3c4d5e6f7g8h9i0j1k2l3m4n5p',
    'AUDITOR',
    'ACTIVE',
    NULL,
    FALSE,
    0,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE email=VALUES(email);

-- 5. Insert Demo External System Ingestion API Key
-- Raw Key: ar_live_demo01_acme_secret_key_9981
-- SHA-256 Digest: d160f4247bf13db98c1623ecb2bdde1ceabe3f250e9e63aa7c2bd5b535e84a59
INSERT INTO api_keys (
    id, 
    organization_id, 
    key_prefix, 
    key_hash, 
    label, 
    status, 
    created_by_user_id, 
    created_at
) VALUES (
    '00000000-0000-0000-0000-000000000020',
    '00000000-0000-0000-0000-000000000001',
    'ar_live_demo01',
    'd160f4247bf13db98c1623ecb2bdde1ceabe3f250e9e63aa7c2bd5b535e84a59',
    'Acme SAP ERP Ingestion Gateway',
    'ACTIVE',
    '00000000-0000-0000-0000-000000000010',
    CURRENT_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE label=VALUES(label);
