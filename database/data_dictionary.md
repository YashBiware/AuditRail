# AuditRail Data Dictionary

**Platform:** AuditRail — Enterprise Audit Integrity Platform  
**Target RDBMS:** MySQL 8.0+ (InnoDB Engine, utf8mb4 / utf8mb4_unicode_ci)  
**Architecture:** Multi-Tenant Isolated, Append-Only Cryptographic Hash Chaining  

---

## 1. Entity-Relationship Overview

AuditRail utilizes a normalized, tenant-isolated relational schema. Every tenant-specific table carries a mandatory `organization_id` foreign key referencing `organizations(id)` with `ON DELETE RESTRICT`. All secondary search and range indexes are compound indexes prefixed with `organization_id` to guarantee query isolation and index locality.

```
+--------------------+
|   organizations    |
+---------+----------+
          |
          +--------------------------------------------------------+
          | 1:1                                                    | 1:N
+---------v----------+                                   +---------v----------+
|    chain_heads     |                                   |       users        |
+--------------------+                                   +----+----------+----+
                                                              | 1:N      | 1:N
                                                              |          |
                                         +--------------------+          |
                                         |                               |
                               +---------v----------+          +---------v----------+
                               |     api_keys       |          |    invitations     |
                               +--------------------+          +--------------------+
          | 1:N                                                    | 1:N
+---------v----------+                                   +---------v----------+
|    audit_events    |                                   |    checkpoints     |
+--------------------+                                   +---------+----------+
                                                                   | 0..1:N
                                                         +---------v----------+
                                                         | verification_runs  |
                                                         +--------------------+
```

---

## 2. Table Specifications

### 2.1 `organizations`
- **Purpose:** Represents the enterprise tenant boundary. All audit trails, users, credentials, and checkpoints are partitioned by organization.
- **Engine:** InnoDB
- **Charset:** `utf8mb4` / `utf8mb4_unicode_ci`

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description |
|---|---|---|---|---|---|
| `id` | `VARCHAR(36)` | NO | None | `PRIMARY KEY` | UUID v4 tenant identifier. |
| `name` | `VARCHAR(128)` | NO | None | None | Legal or business display name of the tenant. |
| `slug` | `VARCHAR(64)` | NO | None | `UNIQUE KEY (uq_organizations_slug)` | URL-safe and human-readable tenant identifier. |
| `status` | `VARCHAR(32)` | NO | `'ACTIVE'` | `INDEX (idx_organizations_status)` | Tenant lifecycle state: `ACTIVE`, `SUSPENDED`, `DEACTIVATED`. |
| `created_at` | `DATETIME(6)` | NO | `CURRENT_TIMESTAMP(6)` | None | Tenant provisioning timestamp in UTC. |
| `updated_at` | `DATETIME(6)` | NO | `CURRENT_TIMESTAMP(6)` | `ON UPDATE CURRENT_TIMESTAMP(6)` | Last metadata update timestamp in UTC. |

---

### 2.2 `users`
- **Purpose:** Stores authenticated enterprise human operators. Access is governed by Role-Based Access Control (`ADMIN` or `AUDITOR`) and protected by BCrypt passwords and mandatory TOTP MFA.
- **Engine:** InnoDB
- **Charset:** `utf8mb4` / `utf8mb4_unicode_ci`

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description |
|---|---|---|---|---|---|
| `id` | `VARCHAR(36)` | NO | None | `PRIMARY KEY` | UUID v4 user identifier. |
| `organization_id` | `VARCHAR(36)` | NO | None | `FOREIGN KEY -> organizations(id)` | Tenant boundary foreign key (`ON DELETE RESTRICT`). |
| `email` | `VARCHAR(255)` | NO | None | `UNIQUE KEY (uq_users_org_email)` | User corporate email, unique per organization. |
| `password_hash` | `VARCHAR(255)` | NO | None | None | BCrypt salted hash (cost factor >= 12). |
| `role` | `VARCHAR(32)` | NO | None | `INDEX (organization_id, role)` | Role: `ADMIN` (read-write config/keys) or `AUDITOR` (read-only). |
| `account_status` | `VARCHAR(32)` | NO | `'ACTIVE'` | `INDEX (organization_id, account_status)` | Status: `ACTIVE`, `DISABLED`, `PENDING_INVITE`. |
| `totp_secret_encrypted`| `VARCHAR(512)`| YES| `NULL` | None | Base32 TOTP secret encrypted with AES-256-GCM. |
| `totp_enabled` | `BOOLEAN` | NO | `FALSE` | None | Whether TOTP verification is enforced on login. |
| `totp_verified_at` | `DATETIME(6)` | YES| `NULL` | None | Timestamp when user confirmed initial TOTP setup. |
| `failed_login_attempts`| `INT` | NO | `0` | None | Consecutive failed login counter for lockouts. |
| `locked_until` | `DATETIME(6)` | YES| `NULL` | None | Expiration timestamp of temporary account lockout. |
| `created_at` | `DATETIME(6)` | NO | `CURRENT_TIMESTAMP(6)` | None | Account creation timestamp in UTC. |
| `updated_at` | `DATETIME(6)` | NO | `CURRENT_TIMESTAMP(6)` | `ON UPDATE CURRENT_TIMESTAMP(6)` | Profile modification timestamp in UTC. |

---

### 2.3 `api_keys`
- **Purpose:** Ingestion authentication credentials issued to client applications (e.g. SAP ERP, Core Banking, HRMS). The plain-text key is shown only once at creation; only the SHA-256 digest is persisted.
- **Engine:** InnoDB
- **Charset:** `utf8mb4` / `utf8mb4_unicode_ci`

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description |
|---|---|---|---|---|---|
| `id` | `VARCHAR(36)` | NO | None | `PRIMARY KEY` | UUID v4 key record identifier. |
| `organization_id` | `VARCHAR(36)` | NO | None | `FOREIGN KEY -> organizations(id)` | Tenant boundary foreign key (`ON DELETE RESTRICT`). |
| `key_prefix` | `VARCHAR(16)` | NO | None | `INDEX (key_prefix)` | Public prefix for admin UI identification (e.g. `ar_live_a1b2c3d4`). |
| `key_hash` | `CHAR(64)` | NO | None | `UNIQUE KEY (uq_api_keys_hash)` | SHA-256 hex digest of the raw secret token. |
| `label` | `VARCHAR(128)` | NO | None | None | Purpose descriptor (e.g. `SAP Gateway`, `Billing Ingestion`). |
| `status` | `VARCHAR(32)` | NO | `'ACTIVE'` | `INDEX (organization_id, status)` | Key status: `ACTIVE`, `REVOKED`. |
| `created_by_user_id` | `VARCHAR(36)` | YES| `NULL` | `FOREIGN KEY -> users(id)` | Admin user who generated this key (`ON DELETE SET NULL`). |
| `created_at` | `DATETIME(6)` | NO | `CURRENT_TIMESTAMP(6)` | None | Generation timestamp in UTC. |
| `revoked_at` | `DATETIME(6)` | YES| `NULL` | None | Revocation timestamp in UTC (if revoked). |
| `last_used_at` | `DATETIME(6)` | YES| `NULL` | None | Last event ingestion receipt timestamp in UTC. |

---

### 2.4 `chain_heads`
- **Purpose:** Represents the latest cryptographic tip/head of each organization's hash chain. Serves as the concurrency serialization lock point during event ingestion.
- **Engine:** InnoDB
- **Charset:** `utf8mb4` / `utf8mb4_unicode_ci`

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description |
|---|---|---|---|---|---|
| `organization_id` | `VARCHAR(36)` | NO | None | `PRIMARY KEY`, `FOREIGN KEY -> organizations(id)` | Tenant identifier (strictly 1:1 relationship with `organizations`). |
| `latest_sequence_number`| `BIGINT UNSIGNED`| NO | `0` | None | Current highest sequence number assigned in this chain. |
| `latest_event_hash` | `CHAR(64)` | NO | None | None | SHA-256 hex digest of the latest appended event. Genesis: 64 zeros. |
| `updated_at` | `DATETIME(6)` | NO | `CURRENT_TIMESTAMP(6)` | `ON UPDATE CURRENT_TIMESTAMP(6)` | UTC timestamp of last chain modification. |

---

### 2.5 `audit_events`
- **Purpose:** Primary append-only audit event log. Every row is cryptographically linked to the previous row via SHA-256 hash chaining.
- **Engine:** InnoDB
- **Charset:** `utf8mb4` / `utf8mb4_unicode_ci`

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description |
|---|---|---|---|---|---|
| `id` | `VARCHAR(36)` | NO | None | `PRIMARY KEY` | UUID v4 event identifier. |
| `organization_id` | `VARCHAR(36)` | NO | None | `FOREIGN KEY -> organizations(id)` | Tenant boundary foreign key (`ON DELETE RESTRICT`). |
| `sequence_number` | `BIGINT UNSIGNED`| NO | None | `UNIQUE KEY (organization_id, sequence_number)` | Monotonically increasing counter (1, 2, 3...) per organization. |
| `actor_reference` | `VARCHAR(255)` | NO | None | `INDEX (organization_id, actor_reference, event_timestamp DESC)` | Actor who performed the action (username, user ID, system service). |
| `action` | `VARCHAR(128)` | NO | None | `INDEX (organization_id, action, event_timestamp DESC)` | Standardized event action code (e.g. `USER_LOGIN`, `RECORD_UPDATE`). |
| `resource_reference`| `VARCHAR(255)` | NO | None | `INDEX (organization_id, resource_reference, event_timestamp DESC)` | Target resource (e.g. `account:acc_9812`, `policy:pol_45`). |
| `event_timestamp` | `DATETIME(6)` | NO | None | `INDEX (organization_id, event_timestamp DESC)` | Source event occurrence timestamp in UTC. |
| `metadata_json` | `JSON` | NO | None | None | Arbitrary contextual JSON attributes (IP, user agent, diffs). |
| `metadata_hash` | `CHAR(64)` | NO | None | None | SHA-256 hex digest of canonical RFC 8785 metadata JSON. |
| `previous_hash` | `CHAR(64)` | NO | None | None | SHA-256 hash of previous event (64 zeros for sequence 1). |
| `current_hash` | `CHAR(64)` | NO | None | `UNIQUE KEY (organization_id, current_hash)` | SHA-256 hash of canonical event payload + `previous_hash`. |
| `ingested_at` | `DATETIME(6)` | NO | `CURRENT_TIMESTAMP(6)` | None | Platform receipt timestamp in UTC. |

---

### 2.6 `checkpoints`
- **Purpose:** Records metadata for external, independent checkpoints stored in MinIO or Amazon S3. The external object contains the authoritative proof outside MySQL.
- **Engine:** InnoDB
- **Charset:** `utf8mb4` / `utf8mb4_unicode_ci`

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description |
|---|---|---|---|---|---|
| `id` | `VARCHAR(36)` | NO | None | `PRIMARY KEY` | UUID v4 checkpoint record identifier. |
| `organization_id` | `VARCHAR(36)` | NO | None | `FOREIGN KEY -> organizations(id)` | Tenant boundary foreign key (`ON DELETE RESTRICT`). |
| `sequence_number` | `BIGINT UNSIGNED`| NO | None | `UNIQUE KEY (organization_id, sequence_number)` | Chain head sequence number at checkpoint snapshot. |
| `checkpoint_hash` | `CHAR(64)` | NO | None | None | Chain head hash snapshot at checkpoint time. |
| `hash_algorithm` | `VARCHAR(32)` | NO | `'SHA-256'` | None | Cryptographic algorithm name. |
| `storage_provider` | `VARCHAR(32)` | NO | None | None | External storage provider: `MINIO` or `AMAZON_S3`. |
| `storage_bucket` | `VARCHAR(128)` | NO | None | None | Bucket or container name in external storage. |
| `storage_object_key`| `VARCHAR(512)` | NO | None | None | Full object key path (e.g. `checkpoints/org_id/cp_seq_100.json`). |
| `object_version_reference`| `VARCHAR(128)`| YES| `NULL` | None | External S3/MinIO Object Version ID. |
| `object_sha256` | `CHAR(64)` | NO | None | None | SHA-256 integrity digest of the external JSON file. |
| `verification_status`| `VARCHAR(32)` | NO | `'PENDING'` | None | Status: `PENDING`, `VERIFIED`, `MISMATCH`, `UNAVAILABLE`. |
| `created_at` | `DATETIME(6)` | NO | `CURRENT_TIMESTAMP(6)` | `INDEX (organization_id, created_at DESC)` | Checkpoint generation timestamp in UTC. |

---

### 2.7 `invitations`
- **Purpose:** Manages one-time onboarding invitation tokens generated manually by Admins. Plain-text token is shared via URL; only the SHA-256 hash is persisted. Single-use and time-bound.
- **Engine:** InnoDB
- **Charset:** `utf8mb4` / `utf8mb4_unicode_ci`

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description |
|---|---|---|---|---|---|
| `id` | `VARCHAR(36)` | NO | None | `PRIMARY KEY` | UUID v4 invitation identifier. |
| `organization_id` | `VARCHAR(36)` | NO | None | `FOREIGN KEY -> organizations(id)` | Tenant boundary foreign key (`ON DELETE RESTRICT`). |
| `email` | `VARCHAR(255)` | NO | None | `INDEX (organization_id, email)` | Invitee email address. |
| `role` | `VARCHAR(32)` | NO | None | None | Target role upon acceptance: `ADMIN`, `AUDITOR`. |
| `token_hash` | `CHAR(64)` | NO | None | `UNIQUE KEY (uq_invitations_token_hash)` | SHA-256 hex digest of the one-time raw token. |
| `status` | `VARCHAR(32)` | NO | `'PENDING'` | `INDEX (organization_id, status)` | Status: `PENDING`, `ACCEPTED`, `EXPIRED`, `REVOKED`. |
| `expires_at` | `DATETIME(6)` | NO | None | None | Token expiration timestamp in UTC. |
| `used_at` | `DATETIME(6)` | YES| `NULL` | None | Timestamp when invitation was redeemed. |
| `created_by_user_id`| `VARCHAR(36)` | NO | None | `FOREIGN KEY -> users(id)` | Admin user who created this invitation (`ON DELETE RESTRICT`). |
| `created_at` | `DATETIME(6)` | NO | `CURRENT_TIMESTAMP(6)` | None | Invitation creation timestamp in UTC. |

---

### 2.8 `verification_runs`
- **Purpose:** Historical audit trail verification log. Captures manual or hourly scheduled verification results, sequence ranges inspected, checkpoint validation, and tamper breach diagnostics.
- **Engine:** InnoDB
- **Charset:** `utf8mb4` / `utf8mb4_unicode_ci`

| Column Name | Data Type | Nullable | Default | Constraints & Keys | Description |
|---|---|---|---|---|---|
| `id` | `VARCHAR(36)` | NO | None | `PRIMARY KEY` | UUID v4 verification run identifier. |
| `organization_id` | `VARCHAR(36)` | NO | None | `FOREIGN KEY -> organizations(id)` | Tenant boundary foreign key (`ON DELETE RESTRICT`). |
| `trigger_type` | `VARCHAR(32)` | NO | None | None | Trigger: `MANUAL` or `SCHEDULED_HOURLY`. |
| `initiated_by_user_id`| `VARCHAR(36)` | YES| `NULL` | `FOREIGN KEY -> users(id)` | User who triggered run (`NULL` for automated hourly runs). |
| `started_at` | `DATETIME(6)` | NO | `CURRENT_TIMESTAMP(6)` | `INDEX (organization_id, started_at DESC)` | Job start timestamp in UTC. |
| `completed_at` | `DATETIME(6)` | YES| `NULL` | None | Job completion timestamp in UTC. |
| `start_sequence_number`| `BIGINT UNSIGNED`| NO | None | None | Starting sequence number checked (typically 1). |
| `end_sequence_number` | `BIGINT UNSIGNED`| NO | None | None | Ending sequence number checked (chain head). |
| `total_events_checked`| `BIGINT UNSIGNED`| NO | `0` | None | Count of events mathematically verified. |
| `result` | `VARCHAR(32)` | NO | `'PENDING'` | `INDEX (organization_id, result)` | Result: `PENDING`, `VERIFIED`, `FAILED`, `INCOMPLETE`, `CHECKPOINT_UNAVAILABLE`. |
| `checkpoint_id` | `VARCHAR(36)` | YES| `NULL` | `FOREIGN KEY -> checkpoints(id)` | Checkpoint record evaluated in this verification run. |
| `failure_sequence_number`| `BIGINT UNSIGNED`| YES| `NULL` | None | First sequence number where hash mismatch occurred. |
| `failure_reason` | `VARCHAR(512)`| YES| `NULL` | None | Sanitized summary of the discrepancy. |
| `failure_details` | `JSON` | YES| `NULL` | None | Structured failure diagnostic (expected hash, actual hash). |

---

## 3. Index Strategy Justification

1. **Mandatory Tenant Prefixing:**
   Every secondary index on tenant-scoped tables (`users`, `api_keys`, `audit_events`, `checkpoints`, `invitations`, `verification_runs`) begins with `organization_id`. Under MySQL InnoDB B-Tree indexing, this ensures all index lookups immediately partition into the organization's subtree, preventing index scan leakage and optimizing cache utilization.

2. **Composite Uniqueness on Sequence:**
   `audit_events (organization_id, sequence_number)` is enforced via a `UNIQUE KEY`. This guarantees at the database storage engine level that two events for the same organization can never share the same sequence number, completely precluding chain forks.

3. **Composite Uniqueness on Hash:**
   `audit_events (organization_id, current_hash)` is enforced via a `UNIQUE KEY`. This prevents duplicate event hashes within any tenant's chain.

4. **Range Scan Optimization on Audit Explorer:**
   The index `idx_audit_events_org_time (organization_id, event_timestamp DESC)` powers the primary UI view (Audit Log Explorer) allowing fast date-range filtering and descending temporal ordering without requiring temporary files or `filesort`.
