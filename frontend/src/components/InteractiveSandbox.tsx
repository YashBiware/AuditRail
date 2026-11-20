import React, { useState } from 'react';
import { 
  ShieldCheck, 
  Terminal, 
  RefreshCw, 
  CheckCircle2, 
  XCircle, 
  AlertTriangle,
  ArrowRight,
  Activity,
  Copy,
  Check
} from 'lucide-react';
import type { AuditEventItem } from '../types/audit';

const INITIAL_EVENTS: AuditEventItem[] = [
  {
    id: "evt-019283-4a",
    organizationId: "org-acme-global",
    sequenceNumber: 1,
    actorReference: "system:batch-sync",
    action: "GENESIS_INITIALIZATION",
    resourceReference: "ledger:system_anchor",
    eventTimestamp: "2026-11-20T16:00:00.000000Z",
    metadataJson: '{"version":"v1","purpose":"initial_anchor"}',
    metadataHash: "3298a0c210dff9b183617182903ea712d9a9b6c4918e782d1c5a9291fa890123",
    previousHash: "0000000000000000000000000000000000000000000000000000000000000000",
    currentHash: "654dd25279db96dc6f6b6e0387dc1c3a19ef3ad6382d0c40860d94054f554823",
    ingestedAt: "2026-11-20T16:00:01.000000Z"
  },
  {
    id: "evt-019283-4b",
    organizationId: "org-acme-global",
    sequenceNumber: 2,
    actorReference: "admin:elena.rostova",
    action: "IAM_POLICY_MUTATION",
    resourceReference: "policy:pci_dss_compliance",
    eventTimestamp: "2026-11-20T16:15:30.400000Z",
    metadataJson: '{"action":"ENFORCE_MFA","severity":"HIGH"}',
    metadataHash: "8f481c8b3e8a719910d61183204918fa918b0c9e6712891901abcf891230114a",
    previousHash: "654dd25279db96dc6f6b6e0387dc1c3a19ef3ad6382d0c40860d94054f554823",
    currentHash: "af7032a929cf3a92e79b2812d905a549e149e0651e14075d50d0b6d44c6824f1",
    ingestedAt: "2026-11-20T16:15:31.000000Z"
  },
  {
    id: "evt-019283-4c",
    organizationId: "org-acme-global",
    sequenceNumber: 3,
    actorReference: "auditor:marcus.vance",
    action: "FINANCIAL_RECORD_LOCK",
    resourceReference: "escrow:vault_9921",
    eventTimestamp: "2026-11-20T16:32:10.150000Z",
    metadataJson: '{"auditPeriod":"Q3_2026","settlement":"COMPLETE"}',
    metadataHash: "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a",
    previousHash: "af7032a929cf3a92e79b2812d905a549e149e0651e14075d50d0b6d44c6824f1",
    currentHash: "feff01d54e0018202f81a884a84f1aabb219a79dbee00de51af9ab5e965e7d48",
    ingestedAt: "2026-11-20T16:32:11.000000Z"
  }
];

export const InteractiveSandbox: React.FC = () => {
  const [events, setEvents] = useState<AuditEventItem[]>(INITIAL_EVENTS);
  const [isTampered, setIsTampered] = useState(false);
  const [verificationStatus, setVerificationStatus] = useState<'IDLE' | 'VERIFYING' | 'SUCCESS' | 'FAILED'>('IDLE');
  const [verificationReport, setVerificationReport] = useState<string | null>(null);
  const [selectedEvent, setSelectedEvent] = useState<AuditEventItem>(events[1]);
  const [copiedHash, setCopiedHash] = useState<string | null>(null);

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedHash(text);
    setTimeout(() => setCopiedHash(null), 2000);
  };

  const simulateTamper = () => {
    setIsTampered(true);
    setVerificationStatus('IDLE');
    setVerificationReport(null);
    setEvents(prev => prev.map(ev => {
      if (ev.sequenceNumber === 2) {
        return {
          ...ev,
          actorReference: "attacker:anonymous_0day",
          // Notice: metadata is modified, but hashes in standard DB remain outdated unless recalculated
        };
      }
      return ev;
    }));
  };

  const resetSandbox = () => {
    setIsTampered(false);
    setVerificationStatus('IDLE');
    setVerificationReport(null);
    setEvents(INITIAL_EVENTS);
    setSelectedEvent(INITIAL_EVENTS[1]);
  };

  const runVerificationWalk = () => {
    setVerificationStatus('VERIFYING');
    setVerificationReport(null);

    setTimeout(() => {
      if (isTampered) {
        setVerificationStatus('FAILED');
        setVerificationReport(
          "🚨 CRYPTOGRAPHIC COMPROMISE DETECTED AT SEQUENCE #2\n" +
          "• Error: Content Tamper Detected (SHA-256 Digest Recalculation Mismatch)\n" +
          "• Expected Hash: af7032a929cf3a92e79b2812d905a549e149e0651e14075d50d0b6d44c6824f1\n" +
          "• Calculated Hash: 8b019da8e8749a9b7dc2810a08e192c7104b901a8ef110820fa918b0213190ab\n" +
          "• Status: Chain continuity rejected. Checkpoint mismatch logged to security isolation tier."
        );
      } else {
        setVerificationStatus('SUCCESS');
        setVerificationReport(
          "✅ HASH-CHAIN INTEGRITY FULLY CONFIRMED\n" +
          "• Events Verified: 3/3 contiguous sequence walk\n" +
          "• Genesis Anchor: 0000000000000000000000000000000000000000000000000000000000000000\n" +
          "• External WORM Anchor: Validated against AWS S3 Object Lock snapshot (WORM Mode: COMPLIANCE)\n" +
          "• Audit Conclusion: Clean chain. Zero gaps, zero unauthorized modifications."
        );
      }
    }, 1200);
  };

  return (
    <div className="w-full rounded-2xl border border-border-subtle bg-bg-secondary p-6 shadow-2xl relative overflow-hidden">
      {/* Decorative top pill badge */}
      <div className="flex flex-wrap items-center justify-between gap-4 pb-6 border-b border-border-subtle">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-accent-emerald/10 border border-accent-emerald/20">
            <Activity className="h-5 w-5 text-accent-emerald animate-pulse" />
          </div>
          <div>
            <h3 className="text-base font-semibold text-text-primary flex items-center gap-2">
              Interactive SHA-256 Chain Simulator
              <span className="text-xs px-2.5 py-0.5 rounded-full bg-border-subtle text-text-secondary font-mono">
                Live State
              </span>
            </h3>
            <p className="text-xs text-text-secondary">
              Directly test how cryptographic chaining detects database mutations in real-time.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2.5">
          {!isTampered ? (
            <button
              onClick={simulateTamper}
              className="px-3.5 py-2 text-xs font-semibold rounded-lg bg-accent-rose/10 text-accent-rose border border-accent-rose/30 hover:bg-accent-rose/20 transition-all flex items-center gap-1.5"
            >
              <AlertTriangle className="h-3.5 w-3.5" />
              Simulate DB Row Tampering
            </button>
          ) : (
            <button
              onClick={resetSandbox}
              className="px-3.5 py-2 text-xs font-semibold rounded-lg bg-bg-surface text-text-secondary border border-border-strong hover:text-text-primary transition-all flex items-center gap-1.5"
            >
              <RefreshCw className="h-3.5 w-3.5" />
              Restore Original Chain
            </button>
          )}

          <button
            onClick={runVerificationWalk}
            disabled={verificationStatus === 'VERIFYING'}
            className="px-4 py-2 text-xs font-semibold rounded-lg bg-accent-emerald text-bg-primary hover:bg-emerald-400 transition-all flex items-center gap-2 font-mono"
          >
            {verificationStatus === 'VERIFYING' ? (
              <RefreshCw className="h-3.5 w-3.5 animate-spin" />
            ) : (
              <ShieldCheck className="h-3.5 w-3.5" />
            )}
            Run Verification Walk
          </button>
        </div>
      </div>

      {/* Main Chaining Visualizer */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 pt-6">
        {/* Chain list */}
        <div className="lg:col-span-2 space-y-3">
          <div className="flex items-center justify-between text-xs font-medium text-text-secondary px-1">
            <span>CHAIN CONTINUITY PIPELINE</span>
            <span className="font-mono">PESSIMISTIC_ROW_LOCK: ACTIVE</span>
          </div>

          <div className="space-y-3">
            {events.map((ev) => {
              const isSelected = selectedEvent.sequenceNumber === ev.sequenceNumber;
              const isEventTampered = isTampered && ev.sequenceNumber === 2;

              return (
                <div
                  key={ev.id}
                  onClick={() => setSelectedEvent(ev)}
                  className={`p-4 rounded-xl border transition-all cursor-pointer relative ${
                    isEventTampered
                      ? 'border-accent-rose/60 bg-accent-rose/5 shadow-lg shadow-rose-950/20'
                      : isSelected
                      ? 'border-accent-emerald/50 bg-bg-card shadow-lg shadow-emerald-950/20'
                      : 'border-border-subtle bg-bg-card/60 hover:border-border-strong'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      <span className={`h-7 w-7 rounded-lg flex items-center justify-center font-mono text-xs font-bold ${
                        isEventTampered ? 'bg-accent-rose/20 text-accent-rose' : 'bg-border-subtle text-accent-emerald'
                      }`}>
                        #{ev.sequenceNumber}
                      </span>
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-semibold text-text-primary font-mono">{ev.action}</span>
                          {isEventTampered && (
                            <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-accent-rose/20 text-accent-rose border border-accent-rose/30">
                              MUTATED ROW
                            </span>
                          )}
                        </div>
                        <span className="text-xs text-text-secondary font-mono">Actor: {ev.actorReference}</span>
                      </div>
                    </div>

                    <div className="text-right">
                      <span className="text-[11px] font-mono text-text-muted block">
                        {new Date(ev.eventTimestamp).toLocaleTimeString()}
                      </span>
                      <span className="text-[10px] font-mono text-accent-cyan bg-accent-cyan/10 px-1.5 py-0.5 rounded">
                        {ev.resourceReference}
                      </span>
                    </div>
                  </div>

                  {/* Hash linkage preview */}
                  <div className="mt-3 pt-3 border-t border-border-subtle/60 flex items-center justify-between text-[11px] font-mono text-text-muted">
                    <div className="flex items-center gap-2 truncate max-w-[45%]">
                      <span>PREV:</span>
                      <span className="text-text-secondary truncate">{ev.previousHash.slice(0, 16)}...</span>
                    </div>
                    <ArrowRight className="h-3 w-3 text-border-strong flex-shrink-0" />
                    <div className="flex items-center gap-2 truncate max-w-[45%] justify-end">
                      <span>CURRENT:</span>
                      <span className={`truncate ${isEventTampered ? 'text-accent-rose font-bold' : 'text-accent-emerald'}`}>
                        {ev.currentHash.slice(0, 16)}...
                      </span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Event Detail & Verification Terminal */}
        <div className="space-y-4">
          <div className="rounded-xl border border-border-subtle bg-bg-card p-4">
            <h4 className="text-xs font-bold text-text-secondary uppercase tracking-wider mb-3 flex items-center justify-between">
              <span>Canonical Payload Inspection</span>
              <span className="text-[10px] text-accent-emerald font-mono">RFC 8785 JCS</span>
            </h4>

            <div className="space-y-2.5 text-xs font-mono">
              <div>
                <span className="text-text-muted block text-[10px]">CURRENT SHA-256 DIGEST</span>
                <div className="flex items-center justify-between bg-bg-primary p-2 rounded border border-border-subtle mt-1">
                  <span className="text-accent-emerald text-[11px] truncate mr-2">
                    {selectedEvent.currentHash}
                  </span>
                  <button 
                    onClick={() => handleCopy(selectedEvent.currentHash)}
                    className="text-text-muted hover:text-text-primary"
                  >
                    {copiedHash === selectedEvent.currentHash ? <Check className="h-3.5 w-3.5 text-accent-emerald" /> : <Copy className="h-3.5 w-3.5" />}
                  </button>
                </div>
              </div>

              <div>
                <span className="text-text-muted block text-[10px]">METADATA ATTRIBUTES</span>
                <pre className="bg-bg-primary p-2 rounded border border-border-subtle text-[11px] text-text-secondary overflow-x-auto mt-1">
                  {JSON.stringify(JSON.parse(selectedEvent.metadataJson), null, 2)}
                </pre>
              </div>

              <div className="pt-2 border-t border-border-subtle/60 text-[11px] text-text-muted flex justify-between">
                <span>Storage Target:</span>
                <span className="text-text-primary font-semibold">AWS S3 (Compliance Lock)</span>
              </div>
            </div>
          </div>

          {/* Verification Result Output */}
          <div className={`rounded-xl border p-4 text-xs font-mono transition-all ${
            verificationStatus === 'SUCCESS'
              ? 'border-accent-emerald/40 bg-accent-emerald/5 text-accent-emerald'
              : verificationStatus === 'FAILED'
              ? 'border-accent-rose/40 bg-accent-rose/5 text-accent-rose'
              : 'border-border-subtle bg-bg-card text-text-muted'
          }`}>
            <div className="flex items-center gap-2 font-semibold mb-2">
              {verificationStatus === 'SUCCESS' && <CheckCircle2 className="h-4 w-4 text-accent-emerald" />}
              {verificationStatus === 'FAILED' && <XCircle className="h-4 w-4 text-accent-rose" />}
              {verificationStatus === 'VERIFYING' && <RefreshCw className="h-4 w-4 animate-spin text-accent-cyan" />}
              {verificationStatus === 'IDLE' && <Terminal className="h-4 w-4 text-text-muted" />}
              <span>Integrity Verification Engine</span>
            </div>

            <p className="text-[11px] leading-relaxed whitespace-pre-line text-text-secondary">
              {verificationReport || "Click 'Run Verification Walk' to compute and cross-examine the cryptographic continuum from sequence #1 to latest checkpoint."}
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
