-- =============================================================================
-- AuditRail — Database Test Cases and Validation Queries (MySQL 8.0+)
-- Architecture: Verification of Schema Integrity, Multi-Tenancy, and Tamper-Evidence
-- =============================================================================

-- -----------------------------------------------------------------------------
-- TEST CASE 1: Foreign Key Enforcement
-- -----------------------------------------------------------------------------
-- Attempting to insert an audit event for a non-existent organization must fail.
-- Expected Result: Error 1452 (Cannot add or update a child row: a foreign key constraint fails)
-- -----------------------------------------------------------------------------
-- EXPECTED_ERROR: 1452
INSERT INTO audit_events (
    id, organization_id, sequence_number, actor_reference, action,
    resource_reference, event_timestamp, metadata_json, metadata_hash,
    previous_hash, current_hash
) VALUES (
    'ffffffff-ffff-ffff-ffff-ffffffffffff',
    '99999999-9999-9999-9999-999999999999', -- Non-existent organization
    1, 'test-actor', 'TEST_ACTION', 'test-res', NOW(6), '{}',
    'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
    '0000000000000000000000000000000000000000000000000000000000000000',
    '1111111111111111111111111111111111111111111111111111111111111111'
);

-- -----------------------------------------------------------------------------
-- TEST CASE 2: Organization-Scoped Monotonic Sequence Uniqueness
-- -----------------------------------------------------------------------------
-- Two events within the same organization cannot have the same sequence number.
-- Expected Result: Error 1062 (Duplicate entry '...' for key 'uq_audit_events_org_seq')
-- -----------------------------------------------------------------------------
-- Step 2A: Valid Insert for Demo Organization (Sequence 1)
INSERT INTO audit_events (
    id, organization_id, sequence_number, actor_reference, action,
    resource_reference, event_timestamp, metadata_json, metadata_hash,
    previous_hash, current_hash
) VALUES (
    '10000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    1, 'admin@acme-logistics.com', 'USER_LOGIN', 'auth:session_101',
    '2026-11-17 10:00:00.000000', '{"ip":"192.168.1.5"}',
    SHA2('{"ip":"192.168.1.5"}', 256),
    '0000000000000000000000000000000000000000000000000000000000000000',
    SHA2(CONCAT('v1|00000000-0000-0000-0000-000000000001|1|admin@acme-logistics.com|USER_LOGIN|auth:session_101|2026-11-17T10:00:00.000000Z|', SHA2('{"ip":"192.168.1.5"}', 256), '|0000000000000000000000000000000000000000000000000000000000000000'), 256)
);

-- Step 2B: Duplicate Sequence 1 in Same Organization (MUST FAIL)
-- EXPECTED_ERROR: 1062
INSERT INTO audit_events (
    id, organization_id, sequence_number, actor_reference, action,
    resource_reference, event_timestamp, metadata_json, metadata_hash,
    previous_hash, current_hash
) VALUES (
    '10000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001',
    1, -- DUPLICATE SEQUENCE IN SAME TENANT
    'admin@acme-logistics.com', 'DOCUMENT_ACCESS', 'doc:doc_102',
    '2026-11-17 10:01:00.000000', '{"ip":"192.168.1.5"}',
    SHA2('{"ip":"192.168.1.5"}', 256),
    '0000000000000000000000000000000000000000000000000000000000000000',
    '2222222222222222222222222222222222222222222222222222222222222222'
);

-- -----------------------------------------------------------------------------
-- TEST CASE 3: Multi-Tenant Sequence Independence
-- -----------------------------------------------------------------------------
-- Sequence 1 CAN exist independently in Organization B without conflict.
-- -----------------------------------------------------------------------------
-- Step 3A: Create Organization B
INSERT INTO organizations (id, name, slug, status)
VALUES ('00000000-0000-0000-0000-000000000002', 'Beta Global Corp', 'beta-global', 'ACTIVE');

INSERT INTO chain_heads (organization_id, latest_sequence_number, latest_event_hash)
VALUES ('00000000-0000-0000-0000-000000000002', 0, '0000000000000000000000000000000000000000000000000000000000000000');

-- Step 3B: Insert Sequence 1 for Organization B (MUST SUCCEED)
INSERT INTO audit_events (
    id, organization_id, sequence_number, actor_reference, action,
    resource_reference, event_timestamp, metadata_json, metadata_hash,
    previous_hash, current_hash
) VALUES (
    '20000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000002', -- Organization B
    1, -- Sequence 1 in Organization B (Permitted!)
    'sys_sync@beta-global.com', 'LEDGER_INITIALIZE', 'ledger:root',
    '2026-11-17 10:00:00.000000', '{"env":"prod"}',
    SHA2('{"env":"prod"}', 256),
    '0000000000000000000000000000000000000000000000000000000000000000',
    SHA2(CONCAT('v1|00000000-0000-0000-0000-000000000002|1|sys_sync@beta-global.com|LEDGER_INITIALIZE|ledger:root|2026-11-17T10:00:00.000000Z|', SHA2('{"env":"prod"}', 256), '|0000000000000000000000000000000000000000000000000000000000000000'), 256)
);

-- -----------------------------------------------------------------------------
-- TEST CASE 4: Tenant User Email Isolation
-- -----------------------------------------------------------------------------
-- The same email address can exist in Organization A and Organization B,
-- but cannot be duplicated within the same Organization.
-- -----------------------------------------------------------------------------
-- Step 4A: Insert auditor@example.com in Organization A (Succeeds)
INSERT INTO users (id, organization_id, email, password_hash, role)
VALUES ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'shared@auditfirm.com', '$2a$12$dummyhash1', 'AUDITOR');

-- Step 4B: Insert same email in Organization B (Succeeds - Cross-tenant identity)
INSERT INTO users (id, organization_id, email, password_hash, role)
VALUES ('30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002', 'shared@auditfirm.com', '$2a$12$dummyhash2', 'AUDITOR');

-- Step 4C: Duplicate email in Organization A (MUST FAIL with Error 1062)
-- EXPECTED_ERROR: 1062
INSERT INTO users (id, organization_id, email, password_hash, role)
VALUES ('30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', 'shared@auditfirm.com', '$2a$12$dummyhash3', 'ADMIN');

-- -----------------------------------------------------------------------------
-- TEST CASE 5: API Key Tenant Resolution Query
-- -----------------------------------------------------------------------------
-- Verifies that an incoming raw API key SHA-256 hash resolves the organization ID
-- without the caller supplying their own tenant ID.
-- -----------------------------------------------------------------------------
SELECT 
    ak.id AS key_id,
    ak.organization_id,
    o.name AS organization_name,
    o.status AS org_status,
    ak.status AS key_status
FROM api_keys ak
JOIN organizations o ON ak.organization_id = o.id
WHERE ak.key_hash = SHA2('ar_live_demo01_acme_secret_key_9981', 256)
  AND ak.status = 'ACTIVE'
  AND o.status = 'ACTIVE';

-- -----------------------------------------------------------------------------
-- TEST CASE 6: Pessimistic Concurrency Ingestion Pattern
-- -----------------------------------------------------------------------------
-- Demonstrates the transactional lock used by the backend ingestion pipeline.
-- -----------------------------------------------------------------------------
START TRANSACTION;

-- Lock the chain head row exclusively for Acme Global
SELECT 
    latest_sequence_number, 
    latest_event_hash 
FROM chain_heads 
WHERE organization_id = '00000000-0000-0000-0000-000000000001'
FOR UPDATE;

-- Insert Event 2 linked to Event 1
SET @prev_hash = (SELECT current_hash FROM audit_events WHERE organization_id = '00000000-0000-0000-0000-000000000001' AND sequence_number = 1);
SET @meta_hash = SHA2('{"amount":50000,"currency":"USD"}', 256);
SET @curr_hash = SHA2(CONCAT('v1|00000000-0000-0000-0000-000000000001|2|finance_cron|WIRE_TRANSFER|account:acc_1001|2026-11-17T10:05:00.000000Z|', @meta_hash, '|', @prev_hash), 256);

INSERT INTO audit_events (
    id, organization_id, sequence_number, actor_reference, action,
    resource_reference, event_timestamp, metadata_json, metadata_hash,
    previous_hash, current_hash
) VALUES (
    '10000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001',
    2, 'finance_cron', 'WIRE_TRANSFER', 'account:acc_1001',
    '2026-11-17 10:05:00.000000', '{"amount":50000,"currency":"USD"}',
    @meta_hash, @prev_hash, @curr_hash
);

UPDATE chain_heads 
SET 
    latest_sequence_number = 2,
    latest_event_hash = @curr_hash,
    updated_at = NOW(6)
WHERE organization_id = '00000000-0000-0000-0000-000000000001';

COMMIT;

-- -----------------------------------------------------------------------------
-- TEST CASE 7: Tamper Detection Query (Chain Link Verification)
-- -----------------------------------------------------------------------------
-- Checks if any row's `previous_hash` fails to match the prior row's `current_hash`.
-- In a healthy chain, this query returns ZERO rows.
-- If any row is modified or deleted, this query flags the exact broken sequence.
-- -----------------------------------------------------------------------------
SELECT 
    curr.sequence_number AS broken_at_sequence,
    curr.id AS event_id,
    curr.previous_hash AS stored_previous_hash,
    prev.current_hash AS actual_prior_event_hash,
    'HASH_MISMATCH_DETECTED' AS integrity_flag
FROM audit_events curr
JOIN audit_events prev 
  ON curr.organization_id = prev.organization_id 
 AND curr.sequence_number = prev.sequence_number + 1
WHERE curr.organization_id = '00000000-0000-0000-0000-000000000001'
  AND curr.previous_hash != prev.current_hash;

-- -----------------------------------------------------------------------------
-- TEST CASE 8: Sequence Gap Detection Query (Deleted Row Detection)
-- -----------------------------------------------------------------------------
-- Detects if an attacker with DB access deleted a row (e.g. sequence 2 was dropped).
-- Expected Result: Zero rows if no sequence gaps exist.
-- -----------------------------------------------------------------------------
SELECT 
    a.sequence_number + 1 AS missing_sequence_number,
    'SEQUENCE_GAP_DETECTED' AS integrity_flag
FROM audit_events a
LEFT JOIN audit_events b 
  ON a.organization_id = b.organization_id 
 AND a.sequence_number + 1 = b.sequence_number
WHERE a.organization_id = '00000000-0000-0000-0000-000000000001'
  AND a.sequence_number < (SELECT latest_sequence_number FROM chain_heads WHERE organization_id = '00000000-0000-0000-0000-000000000001')
  AND b.sequence_number IS NULL;

-- -----------------------------------------------------------------------------
-- TEST CASE 9: External Checkpoint Anchor Validation Query
-- -----------------------------------------------------------------------------
-- Verifies that the chain head recorded in the checkpoint matches the actual event
-- at that sequence number in the database.
-- -----------------------------------------------------------------------------
SELECT 
    cp.id AS checkpoint_id,
    cp.sequence_number AS checkpoint_sequence,
    cp.checkpoint_hash AS checkpoint_stored_hash,
    ae.current_hash AS database_event_hash,
    CASE 
        WHEN ae.current_hash IS NULL THEN 'EVENT_MISSING_IN_DB'
        WHEN cp.checkpoint_hash = ae.current_hash THEN 'CHECKPOINT_MATCH_CONFIRMED'
        ELSE 'CHECKPOINT_MISMATCH_TAMPER_ALERT'
    END AS checkpoint_validation_result
FROM checkpoints cp
LEFT JOIN audit_events ae 
  ON cp.organization_id = ae.organization_id 
 AND cp.sequence_number = ae.sequence_number
WHERE cp.organization_id = '00000000-0000-0000-0000-000000000001';
