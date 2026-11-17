# AUDITRAIL — PROJECT ARCHITECTURE & DEVELOPMENT STATE
**Platform:** AuditRail — Enterprise-grade, application-independent audit integrity platform  
**Current Phase:** Phase 1 Complete (Waiting for Phase 2 Approval)  
**Location:** `C:\Users\yashb\Desktop\AuditRail`  
**Last Updated:** 2026-11-17  

---

## 1. Product Identity and Core Mandates

- **Product Name:** AuditRail
- **Category:** Application-Independent Audit Log Integrity Platform
- **Positioning Statement:**  
  *"AuditRail is an application-independent, multi-tenant audit integrity platform that uses cryptographic hash chaining and independently stored checkpoints to make unauthorized modifications to historical audit records detectable."*
- **Key Tenet:** Tamper-evident, not tamper-proof.
- **Architectural Distinction:**
  - **Not Blockchain:** No consensus, no mining, no cryptocurrency, no Merkle trees, no peer-to-peer gossip. Pure linear SHA-256 hash chaining within an enterprise relational DBMS, anchored by out-of-band WORM checkpoints.
  - **Not SAP Audit Management:** Does not manage internal audit planning, workflows, or ERP suite tasks; acts as an independent integrity preservation layer for events received from SAP, custom Java apps, financial ledgers, etc.
  - **Not SIEM:** No threat intelligence, AI security chatbots, or SOC dashboards.
  - **Not Simple CRUD Log DB:** Strict monotonic sequence assignment, canonical payload serialization, SHA-256 chaining, and external WORM checkpointing.

---

## 2. Mandatory Phase Gate Status

| Phase | Description | Status | Gate Requirement |
|---|---|---|---|
| **Phase 1** | Database Architecture & MySQL Implementation | **COMPLETED** | Completed & verified; all deliverables in `database/`. |
| **Phase 2** | Java / Spring Boot Backend | **READY TO START** | Awaiting command: `APPROVE DATABASE PHASE — START BACKEND` |
| **Phase 3** | Figma-led UI/UX & React Frontend | **LOCKED** | Requires explicit command: `START FRONTEND PHASE` |
| **Phase 4** | Docker, Terraform & Local Infrastructure | **LOCKED** | Requires explicit command |
| **Phase 5** | AWS Cloud & Security Integration | **LOCKED** | Requires explicit command |
| **Phase 6** | Security Testing & Final Handover | **LOCKED** | Requires explicit command |

---

## 3. Phase 1 Deliverables Summary

All database architecture deliverables are located in `C:\Users\yashb\Desktop\AuditRail\database\`:

1. **`schema.sql`**: Production-grade MySQL 8.0+ DDL containing all 8 normalized core tables:
   - `organizations` (tenant boundaries)
   - `users` (enterprise operators with RBAC and TOTP)
   - `api_keys` (SHA-256 hashed ingestion credentials)
   - `chain_heads` (pessimistic row-locking serialization anchor)
   - `audit_events` (append-only cryptographically chained event log)
   - `checkpoints` (metadata references to external WORM-protected checkpoints)
   - `invitations` (single-use hashed onboarding invitations)
   - `verification_runs` (dual-layer verification history & diagnostics)
2. **`migrations/V1__init_auditrail_schema.sql`**: Flyway version 1 schema migration.
3. **`migrations/V2__seed_demo_organization.sql`**: Flyway version 2 seed script initializing demo tenant (`Acme Global Logistics`), chain head at sequence 0 with 64 hex zeros, demo admin, demo auditor, and active ingestion API key (`ar_live_demo01_acme_secret_key_9981`).
4. **`data_dictionary.md`**: Complete schema reference documenting columns, types, nullability, foreign keys, and compound index strategy (`organization_id` prefixing).
5. **`hash_chain_spec.md`**: Full cryptographic specification:
   - Formula: $\text{current\_hash}_N = \text{SHA-256}(\text{canonical\_payload}_N \,\|\, \text{previous\_hash}_N)$
   - Genesis: 64 zeros (`0000...0000`).
   - RFC 8785 JSON Canonicalization Scheme (JCS) for `metadata_json`.
   - v1 pipe-delimited payload serialization.
   - Row-level locking on `chain_heads` (`SELECT ... FOR UPDATE`).
   - Dual-layer verification engine (internal chain walk + out-of-band WORM checkpoint comparison).
6. **`test_cases.sql`**: 9 SQL test suites covering foreign key rejection, sequence collisions, cross-tenant isolation, concurrency locks, tamper detection, and sequence gap queries.
7. **`database_security_isolation.md`**: Security specifications covering zero public DB exposure, least-privilege DB user, multi-tenant query isolation, error sanitization, and endpoint concealment.

---

## 4. Key Architectural Decisions Approved / Proposed

1. **Tenant-Scoped User Email (`uq_users_org_email`):** Emails are unique per organization, allowing external auditors to access multiple client tenants without account collision.
2. **Metadata Canonicalization (RFC 8785 JCS):** Deterministic SHA-256 hashing of arbitrary JSON inputs before linking into the event chain.
3. **Model A Checkpoint Storage:** AuditRail manages its own external storage (MinIO locally, Amazon S3 with S3 Object Lock in production).
4. **Row-Level Concurrency:** `chain_heads` row lock serializes writes per tenant while allowing different organizations to ingest concurrently without database lock contention.

---

## 5. Scope of Phase 2 (Spring Boot Backend)

When authorized with `APPROVE DATABASE PHASE — START BACKEND`, Phase 2 will implement:
- **Framework:** Spring Boot 3.x, Java 17+, Maven, Spring Security, Spring Data JPA / Hibernate, Flyway.
- **Modules:**
  - Authentication & MFA (`/api/v1/auth`): Login, BCrypt, TOTP enrollment/validation.
  - Ingestion API (`/api/v1/events`): API key authentication (`X-AuditRail-API-Key`), tenant resolution, pessimistic row lock, canonical payload creation, SHA-256 calculation, transactional append.
  - Audit Explorer API (`/api/v1/audit`): Filtered search, pagination, detail view, tenant-isolated CSV export.
  - Integrity Verification Engine (`/api/v1/verification`): Manual & hourly scheduled verification walker, checkpoint comparison, tamper diagnostics.
  - Checkpoint Storage Service: Storage abstraction with local MinIO client, ready for future S3 Object Lock adapter.
  - User & Invitation Management (`/api/v1/users`, `/api/v1/invitations`).
