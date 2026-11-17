# AuditRail Hash-Chain & Integrity Specification

**Platform:** AuditRail — Enterprise Audit Integrity Platform  
**Specification Version:** 1.0.0  
**Algorithm:** SHA-256 (FIPS 180-4)  
**Encoding:** UTF-8  

---

## 1. Cryptographic Principles

AuditRail establishes tamper-evidence across enterprise audit logs through **linear cryptographic hash chaining** isolated per tenant organization, combined with **periodic out-of-band checkpointing** to an independent WORM-compliant storage target (MinIO locally / Amazon S3 with S3 Object Lock in production).

AuditRail is **not** a blockchain:
- No proof-of-work, proof-of-stake, or consensus mechanism is used.
- No peer-to-peer gossip protocol or decentralized miners exist.
- No tokens, smart contracts, or cryptocurrency concepts exist.
- Each organization owns an independent, strictly linear chain.
- The chain operates within an enterprise multi-tenant relational database (MySQL 8.0+ InnoDB), protected by application-enforced concurrency locks and external WORM checkpointing.

---

## 2. Hash Formula and Chaining

For any organization $O$, the hash of the event at sequence number $N$ ($N \ge 1$) is defined as:

$$\text{event\_hash}_N = \text{SHA-256}(\text{canonical\_payload}_N \,\|\, \text{previous\_hash}_N)$$

Where:
- $\|$ denotes string concatenation.
- $\text{previous\_hash}_N$ is the `current_hash` of event $N-1$.
- For the genesis event ($N = 1$), $\text{previous\_hash}_1$ is defined as the 64-character hex zero constant:
  `0000000000000000000000000000000000000000000000000000000000000000`

---

## 3. Canonical Serialization Specification

A fundamental requirement in cryptographic systems is determinism: **identical logical events must produce identical binary representations and identical hashes**, regardless of JSON key ordering, whitespace, or serializer library variations.

### 3.1 Metadata Hash Sub-Component
Source applications provide arbitrary contextual metadata as JSON (e.g. client IP, user agent, before/after diffs). To eliminate whitespace and key-order nondeterminism:
1. The metadata JSON object is canonicalized according to **RFC 8785 (JSON Canonicalization Scheme - JCS)**:
   - Object keys sorted lexicographically by UTF-16 code units.
   - Whitespace stripped outside string literals.
   - Numbers formatted strictly per ECMAScript specifications (no trailing zeros, standard IEEE-754 representation).
2. The canonical JSON bytes are hashed with SHA-256:
   $$\text{metadata\_hash} = \text{SHA-256}(\text{JCS}(\text{metadata\_json}))$$
3. Both the raw JSON (in MySQL `JSON` column `metadata_json`) and the resulting `metadata_hash` (CHAR(64)) are stored.

### 3.2 Canonical Payload Format (v1 Pipe-Delimited Standard)
The full canonical payload string to be hashed is assembled using the standard pipe-delimited format:

```text
v1|{organization_id}|{sequence_number}|{actor_reference}|{action}|{resource_reference}|{event_timestamp_iso8601}|{metadata_hash}|{previous_hash}
```

#### Field Specifications:
1. `version`: Literal string `v1` (allows forward-compatible algorithm versioning).
2. `organization_id`: Normalized lowercase UUID string (e.g. `00000000-0000-0000-0000-000000000001`).
3. `sequence_number`: Base-10 unsigned integer string without leading zeros (e.g. `1`, `42`, `10052`).
4. `actor_reference`: UTF-8 string with `|` and `\` characters backslash-escaped (`\|`, `\\`).
5. `action`: Uppercase ASCII identifier string (e.g. `USER_LOGIN`, `FINANCIAL_LEDGER_UPDATE`).
6. `resource_reference`: UTF-8 string with `|` and `\` characters backslash-escaped.
7. `event_timestamp_iso8601`: Canonical ISO-8601 UTC timestamp string with microsecond precision and `Z` suffix:
   `YYYY-MM-DDTHH:mm:ss.SSSSSSZ` (e.g. `2026-11-17T10:00:00.000000Z`).
8. `metadata_hash`: 64-character lowercase hex string of the SHA-256 digest of the canonical metadata JSON.
9. `previous_hash`: 64-character lowercase hex string of the previous event's `current_hash` (or 64 zeros for $N=1$).

#### Complete Hashing Operation:
```java
String canonicalString = String.format(
    "v1|%s|%d|%s|%s|%s|%s|%s|%s",
    organizationId,
    sequenceNumber,
    escape(actorReference),
    action.toUpperCase(),
    escape(resourceReference),
    timestampIso8601Utc,
    metadataHashHex,
    previousHashHex
);

byte[] hashBytes = MessageDigest.getInstance("SHA-256")
                                .digest(canonicalString.getBytes(StandardCharsets.UTF_8));
String currentHash = HexFormat.of().formatHex(hashBytes);
```

---

## 4. Concurrency Control and Transactional Integrity

In an enterprise environment, multiple microservices or background workers may submit audit events for the same organization concurrently. 

To maintain cryptographic chain integrity, the sequence numbers must be strictly consecutive ($1, 2, 3, \dots, N$) and the chain must never fork.

### 4.1 Chain-Head Row-Level Pessimistic Locking
AuditRail uses MySQL InnoDB row-level locking on the `chain_heads` table via `SELECT ... FOR UPDATE`:

```sql
-- Step 1: Begin ACID Transaction
START TRANSACTION;

-- Step 2: Acquire exclusive lock on the organization's chain head
SELECT 
    latest_sequence_number, 
    latest_event_hash 
FROM chain_heads 
WHERE organization_id = :org_id 
FOR UPDATE;

-- Step 3: Compute next sequence and cryptographic hash
-- new_seq  = latest_sequence_number + 1
-- prev_hash = (latest_sequence_number == 0) ? GENESIS_HASH : latest_event_hash
-- curr_hash = SHA256(canonical_payload(new_seq, ...) + prev_hash)

-- Step 4: Insert the audit event record
INSERT INTO audit_events (
    id, organization_id, sequence_number, actor_reference, action,
    resource_reference, event_timestamp, metadata_json, metadata_hash,
    previous_hash, current_hash, ingested_at
) VALUES (
    :id, :org_id, :new_seq, :actor, :action,
    :resource, :timestamp, :metadata_json, :metadata_hash,
    :prev_hash, :curr_hash, NOW(6)
);

-- Step 5: Update the organization's chain head
UPDATE chain_heads 
SET 
    latest_sequence_number = :new_seq,
    latest_event_hash = :curr_hash,
    updated_at = NOW(6)
WHERE organization_id = :org_id;

-- Step 6: Commit transaction, releasing the row lock
COMMIT;
```

### 4.2 Concurrency Characteristics
- **Zero Cross-Tenant Contention:** Because `SELECT ... FOR UPDATE` locks only the specific row matching `organization_id = :org_id`, Organizations A and B ingest events completely in parallel with zero lock contention.
- **Strict Monotonicity:** Event sequences within an organization are strictly monotonic without gaps.
- **Deadlock Avoidance:** All ingestion transactions acquire exactly one row lock in a consistent order (`chain_heads` first, followed by `audit_events` insert and `chain_heads` update).
- **Crash Recovery:** If a transaction fails or rolls back, InnoDB automatically releases the lock; no intermediate sequence or corrupted hash is written.

---

## 5. Independent External Checkpointing (Model A)

### 5.1 The Threat Model
A rogue database administrator with `SUPER` or `mysql` root privileges could theoretically:
1. Modify a historical row in `audit_events`.
2. Recalculate all subsequent hashes in that organization's chain up to the chain head.
3. Overwrite the `chain_heads` table to match the recalculated hash.

In such a scenario, an internal database-only check would report the chain as "valid" because the internal hashes are self-consistent!

### 5.2 Out-of-Band WORM Checkpoint Architecture
To counter this threat, AuditRail exports periodic chain-head snapshots to an external object store completely outside the MySQL database boundary:
- **Local Development:** MinIO object store.
- **Production AWS Target:** Amazon S3 with **S3 Object Lock** enabled in Compliance or Governance mode.

### 5.3 External Checkpoint Payload Structure
Each checkpoint is exported as a standalone JSON object:

```json
{
  "checkpoint_version": "1.0",
  "organization_id": "00000000-0000-0000-0000-000000000001",
  "organization_slug": "acme-global",
  "sequence_number": 10500,
  "chain_head_hash": "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
  "hash_algorithm": "SHA-256",
  "created_at_utc": "2026-11-17T10:00:00.000000Z",
  "storage_provider": "MINIO",
  "bucket": "auditrail-checkpoints",
  "object_key": "checkpoints/00000000-0000-0000-0000-000000000001/cp_seq_10500.json"
}
```

### 5.4 Checkpoint Immutability Guarantees
1. When saved to S3 Object Lock, the object cannot be overwritten or deleted even by the AWS root account during the retention period.
2. The MySQL `checkpoints` table records metadata (`storage_object_key`, `object_sha256`, `object_version_reference`).
3. If an attacker modifies the database and recalculates the chain, the database head will no longer match the externally locked checkpoint.

---

## 6. Verification Logic & Tamper Detection

The verification engine executes two distinct levels of verification:

```
[Start Verification Run]
          |
          v
[Level 1: Internal Hash Chain Walk]
  For sequence i = 1 .. N:
    - Read row i
    - Recompute metadata_hash = SHA256(JCS(metadata_json))
    - Verify row[i].metadata_hash == recomputed
    - Recompute current_hash = SHA256(canonical_payload + row[i].previous_hash)
    - Verify row[i].current_hash == recomputed
    - Verify row[i].previous_hash == row[i-1].current_hash
          |
          +---> Discrepancy found? ---> Status: FAILED (record sequence & hashes)
          |
          v (Chain is internally continuous)
[Level 2: External Checkpoint Anchor Comparison]
  For each registered checkpoint C:
    - Fetch raw JSON object from external storage (MinIO / S3)
    - Verify external object SHA-256 matches MySQL metadata `object_sha256`
    - Compare C.chain_head_hash against row[C.sequence_number].current_hash
          |
          +---> Mismatch found? ---> Status: FAILED (Database rewritten post-checkpoint!)
          +---> Storage unreachable? ---> Status: CHECKPOINT_UNAVAILABLE
          |
          v
[Outcome: VERIFIED]
```

### Status Codes:
- `VERIFIED`: Internal hash chain is continuous and matches all external checkpoints.
- `FAILED`: Internal chain is broken OR external checkpoint does not match the database state.
- `CHECKPOINT_UNAVAILABLE`: Internal chain is valid, but external storage could not be contacted to verify checkpoints.
- `INCOMPLETE`: Verification job timed out or was interrupted before completion.
- `PENDING`: Verification job is currently queued or in progress.
