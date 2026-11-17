# AuditRail — Database Security, Isolation & Endpoint Concealment Specification

## 1. Network & Database Perimeter Isolation
1. **Zero Public Database Exposure:**
   - The MySQL database process binds strictly to `127.0.0.1` locally or an isolated internal Docker bridge network (`auditrail-internal-net`).
   - The database port (`3306`) is **never** published to the public internet or host external network interfaces.
2. **Least-Privilege Database User:**
   - The backend service connects using a dedicated non-root user (`auditrail_app`).
   - Permissions are restricted strictly to `DML` (`SELECT`, `INSERT`, `UPDATE`) on the `auditrail` database.
   - Administrative commands (`DROP`, `GRANT`, `SUPER`, `SHUTDOWN`) are strictly revoked.
3. **Encrypted In-Transit & At-Rest:**
   - All backend-to-database connections enforce TLS (`useSSL=true&requireSSL=true`).

---

## 2. Multi-Tenant Query Isolation (Zero Cross-Organization Access)
1. **Untrusted Client Boundary:**
   - External clients and internal users are **never permitted** to supply an `organization_id` parameter.
   - Organization identity is resolved exclusively through server-side cryptographic tokens:
     * **External Apps:** Header `X-AuditRail-API-Key` -> SHA-256 hashed -> resolved to `organization_id`.
     * **Dashboard Users:** Verified JWT Bearer token -> claims extracted -> resolved to `organization_id`.
2. **Mandatory Query Predicates:**
   - Every single backend repository query automatically appends `WHERE organization_id = :tenantOrgId`.
   - Attempts to access resources belonging to another organization automatically return `404 Not Found` (never revealing whether the resource exists in another tenant).
3. **Database-Level Compound Uniqueness:**
   - `uq_audit_events_org_seq (organization_id, sequence_number)`
   - `uq_users_org_email (organization_id, email)`
   - `uq_checkpoints_org_seq (organization_id, sequence_number)`
   - Ensures the database engine physically rejects cross-tenant duplicates and collisions.

---

## 3. Endpoint Concealment & Attack Surface Reduction
1. **Actuator & Internal Route Concealment:**
   - Management endpoints (`/actuator/**`) are bound to an internal port or completely disabled in production.
   - Swagger / OpenAPI documentation is restricted to authenticated development profiles only.
2. **Sanitized Error Responses (Zero Information Leakage):**
   - Database exceptions (SQL syntax errors, constraint violations, table names, connection strings) are intercepted by a global exception handler.
   - Attackers receive standard, sanitized responses (e.g. `401 Unauthorized` or `400 Invalid Request`) with zero internal details exposed.
3. **Endpoint Rate Limiting & Obfuscation:**
   - Rate limiting on `/api/v1/auth/login` and `/api/v1/events` to prevent brute-force key enumeration and endpoint probing.
   - Public ingress router exposes only the specific ingestion gateway and authentication routes; administrative database endpoints do not exist.
