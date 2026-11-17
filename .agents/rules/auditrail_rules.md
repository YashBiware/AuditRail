# AuditRail Project Rules & Mandatory Phase Controls

## 1. Product Identity
- Name: AuditRail
- Category: Application-Independent Audit Log Integrity Platform
- Positioning: "AuditRail is an application-independent, multi-tenant audit integrity platform that uses cryptographic hash chaining and independently stored checkpoints to make unauthorized modifications to historical audit records detectable."
- Strictly tamper-evident, never claimed as tamper-proof.
- Distinct from blockchain, SAP audit management, and SIEM platforms.

## 2. Mandatory Phase Gate Protocol
Development must proceed strictly through the authorized phases one by one:
1. Phase 1: Database architecture and schema (COMPLETED).
2. Phase 2: Backend development (Java / Spring Boot).
3. Phase 3: Frontend and Figma-led UI/UX (React).
4. Phase 4: Local infrastructure (Docker, MinIO, Terraform).
5. Phase 5: AWS integration (S3 Object Lock, IAM, KMS).
6. Phase 6: Security testing and final documentation.

**Gate Enforcement:**
- Never automatically proceed to the next phase without explicit user command.
- Phase 2 begins only with: `APPROVE DATABASE PHASE — START BACKEND`
- Phase 3 begins only with: `START FRONTEND PHASE`
- Never write frontend or backend code before their respective authorized phases.

## 3. Code Cleanliness & Production Standards
- Zero garbage code, zero dead code, zero bloated dependencies.
- Keep all code clean, strictly typed, minimal, and laser-specific to requirements.
- No unrequested features, no over-engineered boilerplate, no extra fluff.
- Every class, function, query, and configuration must have a direct, necessary purpose.
