# AuditRail — Services, Controllers & API Layer Architecture

This document details all controllers, REST endpoints, service implementations, and DTO contracts across AuditRail.

---

## 1. Controller Architecture & Endpoints

All controllers reside in `com.auditrail.controller` and communicate with dedicated service interfaces in `com.auditrail.service`:

| Controller | Base Path | Role Required | Purpose |
| :--- | :--- | :--- | :--- |
| `AuthController` | `/api/v1/auth` | Public / Authenticated | Login, TOTP MFA enrollment & verification, user profile |
| `AuditEventIngestionController` | `/api/v1/events` | `ROLE_INGEST` | Cryptographic event ingestion gateway |
| `AuditExplorerController` | `/api/v1/audit` | `ADMIN`, `AUDITOR`, `INGEST` | Multi-dimensional audit search, event inspection, CSV export |
| `VerificationController` | `/api/v1/verification` | `ADMIN`, `AUDITOR`, `INGEST` | Trigger chain integrity walk, fetch historical verification runs |
| `CheckpointController` | `/api/v1/checkpoints` | `ADMIN`, `AUDITOR`, `INGEST` | Create manual WORM checkpoints, inspect checkpoint catalog |
| `ApiKeyController` | `/api/v1/api-keys` | `ROLE_ADMIN` | Ingestion key generation, listing, active status, revocation |
| `UserController` | `/api/v1/users` | `ROLE_ADMIN` | Enterprise user management and role assignment |
| `InvitationController` | `/api/v1/invitations` | Public / `ADMIN` | Issue single-use tenant invitations and token redemption |
| `PolicyController` | `/api/v1/policy` | Public | DPDP Act 2023 technical & organizational safeguards |
| `OrganizationController` | `/api/v1/organizations` | Authenticated | Organization metadata and status |

---

## 2. API Endpoint Specification

### Ingestion API
- `POST /api/v1/events`
  - **Headers:** `X-AuditRail-API-Key: <raw_api_key>`, `Content-Type: application/json`
  - **Request Body (`EventIngestRequest`):**
    ```json
    {
      "actorReference": "sap_erp_sync",
      "action": "PAYMENT_RECONCILIATION",
      "resourceReference": "invoice:inv_99812",
      "eventTimestamp": "2026-11-21T18:00:00.000000Z",
      "metadataJson": "{\"amount\": 150000, \"currency\": \"INR\", \"status\": \"APPROVED\"}"
    }
    ```
  - **Response (`EventIngestResponse`):**
    ```json
    {
      "id": "6399ed91-c9b9-4ccf-83af-ba30399850e4",
      "organizationId": "00000000-0000-0000-0000-000000000001",
      "sequenceNumber": 1,
      "currentHash": "09211c8981169f303f9b4bea6b41deb0546d9238e16b00b02704137d29a8c379",
      "previousHash": "0000000000000000000000000000000000000000000000000000000000000000",
      "ingestedAt": "2026-11-21T18:00:01.021659Z"
    }
    ```

### Audit Explorer API
- `GET /api/v1/audit/events`
  - **Query Params:** `actor`, `action`, `resource`, `startDate`, `endDate`, `page`, `size`
  - **Response (`PageResponse<AuditEventResponse>`):** Paginated event objects with complete cryptographic proofs.
- `GET /api/v1/audit/events/{id}`: Detailed event by UUID.
- `GET /api/v1/audit/export/csv`: Streaming CSV export for external audit reviews.

### Verification & Checkpoint API
- `POST /api/v1/verification/run`
  - **Request Body (`VerificationTriggerRequest`):** `{"triggerType": "MANUAL"}`
  - **Response (`VerificationRunResponse`):**
    ```json
    {
      "id": "991c66f4-178c-42af-868e-4b9083dad59b",
      "organizationId": "00000000-0000-0000-0000-000000000001",
      "triggerType": "MANUAL",
      "startedAt": "2026-11-21T18:05:00.123456Z",
      "completedAt": "2026-11-21T18:05:00.458950Z",
      "startSequenceNumber": 1,
      "endSequenceNumber": 100,
      "totalEventsChecked": 100,
      "result": "VERIFIED"
    }
    ```
- `GET /api/v1/verification/history`: Historical audit verification records.
- `POST /api/v1/checkpoints`: Creates an immutable WORM snapshot of the current chain head.
- `GET /api/v1/checkpoints`: Catalogs all historical WORM checkpoints.

---

## 3. Service Layer Implementations

All services are declared as interfaces in `com.auditrail.service` and implemented in `com.auditrail.service.impl`:

### 1. `AuditEventIngestionServiceImpl`
- Enforces `@Transactional(isolation = Isolation.READ_COMMITTED)`.
- Acquires pessimistic lock on `ChainHead`.
- Canonicalizes metadata via `CanonicalJsonUtil.canonicalize()`.
- Calculates deterministic SHA-256 event hash via `CanonicalPayloadBuilder.calculateEventHash()`.
- Persists immutable `AuditEvent` and updates `ChainHead`.

### 2. `IntegrityVerificationServiceImpl`
- Implements two-tier verification logic (Level 1: DB Walk, Level 2: External WORM Storage Reconciliation).
- Scheduled hourly background audit via `@Scheduled(cron = "${auditrail.scheduling.verification-cron:0 0 * * * *}")`.

### 3. `AuditQueryServiceImpl`
- Dynamic querying via Spring Data JPA `Specification<AuditEvent>`.
- Generates RFC 4180 compliant CSV streams without loading unbounded datasets into RAM.

### 4. `AuthServiceImpl`
- BCrypt validation (`passwordEncoder.matches`).
- Enforces 5-attempt brute-force lockout window (15 minutes duration).
- Handles RFC 6238 TOTP MFA challenges and issues HMAC-SHA256 signed JWT tokens.

### 5. `ApiKeyServiceImpl`
- Generates high-entropy cryptographic keys using `SecureRandom`.
- Stores strictly SHA-256 digests (`key_hash`).
- Provides instantaneous revocation without database locks.

### 6. Storage Adapter (`CheckpointStorageService`)
- Interface pattern supporting pluggable backends:
  - `LocalFilesystemCheckpointStorageAdapter`: Local file store for offline dev & CI.
  - `MinioCheckpointStorageAdapter`: S3-compatible local MinIO container.
  - `AwsS3CheckpointStorageAdapter`: Production AWS S3 with Object Lock Compliance Mode.
