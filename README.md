# AuditRail

> **Application-Independent Audit Log Integrity Platform**

"AuditRail is an application-independent, multi-tenant audit integrity platform that uses cryptographic hash chaining and independently stored checkpoints to make unauthorized modifications to historical audit records detectable."

---

## Overview

AuditRail serves as an enterprise-grade, independent integrity layer that collects audit events from diverse systems (e.g. SAP, core banking, custom Java services, internal admin tools), assigns strict monotonic sequences, computes SHA-256 hash chains, exports out-of-band WORM checkpoints to MinIO/S3, and provides automated and on-demand verification to make tampering detectable.

---

## Directory Structure

```text
AuditRail/
├── PROJECT_STATE.md          # Complete project history, phase gates, and decisions
├── README.md                 # Project overview and directory guide
└── database/                 # Phase 1: Database Architecture and Schema
    ├── data_dictionary.md    # Detailed table and column specifications
    ├── hash_chain_spec.md    # Cryptographic formulas, serialization, and concurrency
    ├── schema.sql            # MySQL 8.0+ DDL with 8 normalized tables
    ├── test_cases.sql        # SQL validation queries for isolation and tamper detection
    └── migrations/
        ├── V1__init_auditrail_schema.sql       # Flyway V1 schema migration
        └── V2__seed_demo_organization.sql      # Flyway V2 seed data migration
```

---

## Project Status

- **Phase 1 (Database Architecture & MySQL Implementation):** Complete.
- **Phase 2 (Java Spring Boot Backend):** Pending approval.
- **Phase 3 (Figma UI/UX & React Frontend):** Locked.
- **Phase 4 (Local Infrastructure & Docker):** Locked.
- **Phase 5 (AWS Cloud & Security Integration):** Locked.
- **Phase 6 (Security Testing & Final Handover):** Locked.

See [PROJECT_STATE.md](file:///C:/Users/yashb/Desktop/AuditRail/PROJECT_STATE.md) for full architecture specifications and phase gate controls.
