-- =============================================================================
-- Migration V3: Seed Dual Tenant Isolation Demo Organizations
-- Tenant A: Apex National Bank (Bank Org)
-- Tenant B: Horizon Global Healthcare (Healthcare Org)
-- =============================================================================

SET NAMES utf8mb4;

-- 1. Insert Tenant A (Apex National Bank)
INSERT INTO organizations (
    id, name, slug, status, created_at, updated_at
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Apex National Bank',
    'apex-bank',
    'ACTIVE',
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Initialize Genesis Chain Head for Tenant A
INSERT INTO chain_heads (
    organization_id, 
    latest_sequence_number, 
    latest_event_hash, 
    updated_at
) VALUES (
    '11111111-1111-1111-1111-111111111111',
    0,
    '0000000000000000000000000000000000000000000000000000000000000000',
    CURRENT_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE updated_at=CURRENT_TIMESTAMP(6);

-- 3. Tenant A API Key
-- Raw Key: ar_live_apex_bank_secret_key_8821
-- SHA-256 of 'ar_live_apex_bank_secret_key_8821':
-- 29fa2b23a10e7ef0879685a065842c1c38fa53a8ae3f47cbe6014459fce8f31b
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
    '11111111-1111-1111-1111-111111111120',
    '11111111-1111-1111-1111-111111111111',
    'ar_live_apex_bank',
    '29fa2b23a10e7ef0879685a065842c1c38fa53a8ae3f47cbe6014459fce8f31b',
    'Apex Core Banking Transaction Gateway',
    'ACTIVE',
    '00000000-0000-0000-0000-000000000010',
    CURRENT_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE label=VALUES(label);


-- 4. Insert Tenant B (Horizon Global Healthcare)
INSERT INTO organizations (
    id, name, slug, status, created_at, updated_at
) VALUES (
    '22222222-2222-2222-2222-222222222222',
    'Horizon Global Healthcare',
    'horizon-health',
    'ACTIVE',
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 5. Initialize Genesis Chain Head for Tenant B
INSERT INTO chain_heads (
    organization_id, 
    latest_sequence_number, 
    latest_event_hash, 
    updated_at
) VALUES (
    '22222222-2222-2222-2222-222222222222',
    0,
    '0000000000000000000000000000000000000000000000000000000000000000',
    CURRENT_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE updated_at=CURRENT_TIMESTAMP(6);

-- 6. Tenant B API Key
-- Raw Key: ar_live_horizon_health_key_4419
-- SHA-256 of 'ar_live_horizon_health_key_4419':
-- ed6bcddcb167096d0ea80cbf32c1eb603833d739c9da9b54ec32c0f65fc2b740
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
    '22222222-2222-2222-2222-222222222220',
    '22222222-2222-2222-2222-222222222222',
    'ar_live_horizon',
    'ed6bcddcb167096d0ea80cbf32c1eb603833d739c9da9b54ec32c0f65fc2b740',
    'Horizon EHR Patient Record Gateway',
    'ACTIVE',
    '00000000-0000-0000-0000-000000000010',
    CURRENT_TIMESTAMP(6)
) ON DUPLICATE KEY UPDATE label=VALUES(label);
