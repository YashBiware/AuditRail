# AuditRail: Live Verification & Tamper Simulation Demo Runbook

**Audience:** Technical Evaluators, Security Auditors, Compliance Officers & Enterprise Investors  
**Technology Foundation:** SHA-256 Hash Chaining, RFC 8785 Canonical JSON Serialization (JCS), AWS S3 Object Lock (Compliance Mode WORM), Pessimistic Concurrency Serialization (`SELECT ... FOR UPDATE`).

---

## 1. Executive Demonstration Narrative

AuditRail solves the fundamental flaw of legacy audit logs: **database administrator (DBA) or attacker mutability**. In traditional architectures, an attacker with elevated SQL access can alter, backdate, or delete log rows to erase evidence of financial fraud or insider data exfiltration.

In AuditRail:
1. Every event is cryptographically linked to the previous event via its `record_hash`:
   $$\text{record\_hash}_n = \text{SHA-256}(\text{JCS}(\text{payload}_n) \parallel \text{record\_hash}_{n-1})$$
2. Periodic cryptographic checkpoints are written to an out-of-band Write-Once-Read-Many (WORM) storage bucket (AWS S3 Object Lock in `COMPLIANCE` mode).
3. Any SQL tampering immediately triggers mathematical verification failure, isolating the exact sequence number where the breach occurred.

---

## 2. Pre-Requisites & System Status

- **Backend Service:** Running at `http://localhost:8080` (or via Docker Compose at `http://localhost:8080`)
- **Frontend Console:** Running at `http://localhost:3000` (or `http://localhost:5173`)
- **Demo API Key:** `ar_live_demo_key_77ab53b0_medicaps_2026`
- **Demo Organization ID:** `e8c8bb1c-0aa1-415d-b4eb-62b96df1dd1a`

---

## 3. Step-by-Step Investor Demonstration Script

### Step 1: Establish Valid Chain State (The Baseline)
Open the **AuditRail Live Verification Console** in your browser (`http://localhost:3000`):
1. Notice the live cryptographic chain visualization displaying the sequential events ($1, 2, 3 \dots$).
2. Click **Run Cryptographic Verification**.
3. **Observed Outcome:**
   - Status badge turns green: `CHAIN INTEGRITY VERIFIED`.
   - Discrepancies count: `0`.
   - Verified event count matches the chain head sequence number.

---

### Step 2: Live Event Ingestion (Real-Time Ingestion)
Demonstrate how real enterprise applications push tamper-evident logs:
1. In the console's **Submit Audit Event** panel, enter:
   - **Action:** `FUNDS_TRANSFER_AUTHORIZED`
   - **Actor ID:** `user_compliance_officer_41`
   - **Target ID:** `account_in_9088219`
   - **Payload Metadata:**
     ```json
     {
       "amount": 2500000.00,
       "currency": "INR",
       "beneficiary": "vendor_corp_escrow",
       "approval_tier": "BOARD_LEVEL_MANDATE"
     }
     ```
2. Click **Submit Event to Cryptographic Stream**.
3. **Observed Outcome:**
   - Response returns HTTP 201 Created with the assigned monotonic sequence number $N$.
   - The computed SHA-256 hash appears immediately in the stream.
   - The chain head pointer advances atomically under pessimistic row lock.

---

### Step 3: Simulate Malicious DBA Attack (Data Modification)
Now simulate an unauthorized DBA attempting to cover their tracks by altering the transaction amount:

Run the following SQL statement in the database:
```sql
-- Malicious DBA alters transaction amount from 25,00,000 to 25,000
UPDATE audit_events 
SET payload = JSON_SET(payload, '$.amount', 25000.00) 
WHERE organization_id = 'e8c8bb1c-0aa1-415d-b4eb-62b96df1dd1a' 
  AND event_action = 'FUNDS_TRANSFER_AUTHORIZED';
```

---

### Step 4: Run Cryptographic Verification (Immediate Tamper Detection)
Return to the **AuditRail Live Verification Console** and click **Run Cryptographic Verification**:
- **Observed Outcome:**
  - Status immediately flips to bold red alert: **`TAMPER DETECTED: INVALID EVENT HASH`**.
  - The verification engine highlights the exact sequence number where the content was altered.
  - The calculated hash no longer matches the canonical stored hash:
    $$\text{Expected: } e3b0c44... \ne \text{Computed: } 7f83b16...$$
  - The investor sees proof that even 1 character change in the database destroys the cryptographic continuum.

---

### Step 5: Simulate Row Deletion Attack (Sequence Gap)
Now simulate an attacker trying to delete a row entirely:
```sql
-- Rogue actor deletes an event to hide evidence
DELETE FROM audit_events 
WHERE organization_id = 'e8c8bb1c-0aa1-415d-b4eb-62b96df1dd1a' 
  AND sequence_number = 2;
```

Click **Run Cryptographic Verification**:
- **Observed Outcome:**
  - System immediately flags: **`TAMPER DETECTED: SEQUENCE GAP DETECTED`**.
  - The walk stops at sequence 1 because sequence 2 is missing, proving that deletion attacks are impossible to conceal.

---

### Step 6: Reconciliation Against Immutable WORM Checkpoints
Even if a rogue DBA alters the rows **AND** attempts to recompute all downstream hashes in MySQL:
1. AuditRail queries the out-of-band checkpoint stored in AWS S3 Object Lock (`COMPLIANCE` mode).
2. Because the S3 Object Lock bucket rejects all `s3:DeleteObject`, `s3:PutObject` overwrites, and lifecycle expirations, the external checkpoint hash remains unchanged.
3. The reconciliation step reports:
   $$\text{Database Chain Head Hash } \ne \text{WORM Anchor Checkpoint Hash}$$
4. The database rewrite is exposed definitively.

---

## 4. Key Takeaways for Investors

| Risk Vector | Traditional Enterprise Logs | AuditRail Platform |
| :--- | :--- | :--- |
| **Rogue DBA / Insider Threat** | Can edit SQL rows silently | Immediate hash chain mismatch |
| **Row Deletion / Log Truncation** | Undetected loss of records | Flags sequence gap ($N \to N+2$) |
| **Storage Rewriting** | Log files can be modified on disk | Immutable WORM Object Lock rejects mutations |
| **Concurrency Collisions** | Race conditions cause gaps | Pessimistic `SELECT ... FOR UPDATE` row locks |
| **Regulatory Compliance** | Manual sampling / audit fatigue | Automated 100% mathematical auditability |
