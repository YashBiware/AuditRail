import React, { useState, useEffect } from 'react';
import { 
  ShieldCheck, 
  RefreshCw, 
  CheckCircle2, 
  XCircle, 
  Database,
  Send,
  Copy,
  Check
} from 'lucide-react';

interface LiveEvent {
  id: string;
  sequenceNumber: number;
  actorReference: string;
  action: string;
  resourceReference: string;
  eventTimestamp: string;
  metadataJson: string;
  metadataHash: string;
  previousHash: string;
  currentHash: string;
}

export const LiveAuditConsole: React.FC = () => {
  const [events, setEvents] = useState<LiveEvent[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [activeTab, setActiveTab] = useState<'CONSOLE' | 'INGEST' | 'VERIFY'>('CONSOLE');
  const [apiKey] = useState('ar_live_demo01_acme_secret_key_9981');
  const [verificationResult, setVerificationResult] = useState<any | null>(null);
  const [isVerifying, setIsVerifying] = useState(false);
  const [copiedText, setCopiedText] = useState<string | null>(null);

  // New Event Form
  const [actor, setActor] = useState('svc-billing');
  const [action, setAction] = useState('RECORD_READ');
  const [resource, setResource] = useState('account:usr_9982');
  const [metaJson, setMetaJson] = useState('{"purpose": "quarterly_audit", "scope": "financial"}');
  const [ingestStatus, setIngestStatus] = useState<string | null>(null);

  const fetchLiveEvents = async () => {
    setIsLoading(true);
    try {
      const res = await fetch('http://localhost:8080/api/v1/audit/events?size=10', {
        headers: { 'X-AuditRail-API-Key': apiKey }
      });
      if (res.ok) {
        const data = await res.json();
        setEvents(data.items || []);
      }
    } catch (err) {
      console.error('Failed to fetch events from backend:', err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchLiveEvents();
  }, []);

  const handleCopy = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedText(text);
    setTimeout(() => setCopiedText(null), 2000);
  };

  const handleIngest = async (e: React.FormEvent) => {
    e.preventDefault();
    setIngestStatus('Ingesting into SHA-256 chain under pessimistic lock...');
    try {
      const res = await fetch('http://localhost:8080/api/v1/events', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-AuditRail-API-Key': apiKey,
        },
        body: JSON.stringify({
          actorReference: actor,
          action: action,
          resourceReference: resource,
          eventTimestamp: new Date().toISOString(),
          metadataJson: metaJson
        })
      });

      if (res.ok) {
        const result = await res.json();
        setIngestStatus(`Success: Ingested Sequence #${result.sequenceNumber} with hash ${result.currentHash.slice(0, 16)}...`);
        fetchLiveEvents();
      } else {
        const err = await res.text();
        setIngestStatus(`Error (${res.status}): ${err}`);
      }
    } catch (err: any) {
      setIngestStatus(`Connection error: ${err.message}`);
    }
  };

  const handleRunVerification = async () => {
    setIsVerifying(true);
    setVerificationResult(null);
    try {
      const res = await fetch('http://localhost:8080/api/v1/verification/run', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-AuditRail-API-Key': apiKey,
        },
        body: JSON.stringify({ triggerType: 'MANUAL_DASHBOARD' })
      });

      if (res.ok) {
        const data = await res.json();
        setVerificationResult(data);
      } else {
        const err = await res.text();
        setVerificationResult({ result: 'FAILED', failureReason: err });
      }
    } catch (err: any) {
      setVerificationResult({ result: 'FAILED', failureReason: err.message });
    } finally {
      setIsVerifying(false);
    }
  };

  return (
    <div className="w-full bg-card rounded-lg border border-border shadow-card overflow-hidden">
      {/* Console Top Bar */}
      <div className="border-b border-border bg-[#FBF9F5] px-6 py-4 flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="h-2 w-2 rounded-full bg-signal"></div>
          <div>
            <h3 className="text-sm font-semibold text-foreground tracking-tight flex items-center gap-2">
              Live Hash-Chained Record Ledger
              <span className="mono-tag px-2 py-0.5 rounded bg-secondary text-secondary-foreground">
                Connected: Spring Boot :8080
              </span>
            </h3>
            <p className="text-xs text-muted-foreground mt-0.5">
              Live database state maintained under continuous cryptographic continuity.
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <div className="inline-flex rounded-md p-1 bg-secondary text-xs">
            <button
              onClick={() => setActiveTab('CONSOLE')}
              className={`px-3 py-1.5 rounded font-medium transition-all ${
                activeTab === 'CONSOLE' ? 'bg-card text-foreground shadow-subtle' : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              Audit Stream ({events.length})
            </button>
            <button
              onClick={() => setActiveTab('INGEST')}
              className={`px-3 py-1.5 rounded font-medium transition-all ${
                activeTab === 'INGEST' ? 'bg-card text-foreground shadow-subtle' : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              Ingest Event
            </button>
            <button
              onClick={() => setActiveTab('VERIFY')}
              className={`px-3 py-1.5 rounded font-medium transition-all ${
                activeTab === 'VERIFY' ? 'bg-card text-foreground shadow-subtle' : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              Verification Engine
            </button>
          </div>

          <button
            onClick={fetchLiveEvents}
            disabled={isLoading}
            className="p-2 text-muted-foreground hover:text-foreground border border-border rounded-md hover:bg-secondary transition-colors"
            title="Refresh Ledger"
          >
            <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Main Panel Content */}
      <div className="p-6">
        {activeTab === 'CONSOLE' && (
          <div className="space-y-4">
            {events.length === 0 ? (
              <div className="py-12 text-center space-y-3">
                <Database className="h-8 w-8 text-muted-foreground/40 mx-auto" />
                <p className="text-sm text-muted-foreground">
                  No records stored yet in the active tenant ledger.
                </p>
                <button
                  onClick={() => setActiveTab('INGEST')}
                  className="px-4 py-2 text-xs font-semibold bg-primary text-primary-foreground rounded-md hover:bg-primary/90 transition-all inline-flex items-center gap-2"
                >
                  <Send className="h-3.5 w-3.5" />
                  Append First Event
                </button>
              </div>
            ) : (
              <div className="divide-y divide-border">
                {events.map((ev) => (
                  <div key={ev.id} className="py-3.5 flex flex-col md:flex-row md:items-center justify-between gap-3 hover:bg-[#FAF9F6] px-3 rounded transition-colors">
                    <div className="flex items-start md:items-center gap-3">
                      <span className="mono-tag h-6 w-8 flex items-center justify-center rounded bg-secondary text-foreground font-semibold">
                        #{ev.sequenceNumber}
                      </span>
                      <div>
                        <div className="flex items-center gap-2">
                          <span className="text-sm font-semibold font-mono text-foreground">{ev.action}</span>
                          <span className="text-xs text-muted-foreground">by</span>
                          <span className="text-xs font-mono text-secondary-foreground bg-secondary px-1.5 py-0.5 rounded">
                            {ev.actorReference}
                          </span>
                        </div>
                        <div className="flex items-center gap-2 mt-1 text-xs text-muted-foreground font-mono">
                          <span>Target: {ev.resourceReference}</span>
                          <span>•</span>
                          <span>{new Date(ev.eventTimestamp).toLocaleTimeString()}</span>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-3 text-xs font-mono">
                      <div className="text-right">
                        <div className="flex items-center gap-1.5 justify-end">
                          <span className="text-muted-foreground text-[11px]">PREV:</span>
                          <span className="text-muted-foreground">{ev.previousHash.slice(0, 10)}…</span>
                        </div>
                        <div className="flex items-center gap-1.5 justify-end mt-0.5">
                          <span className="text-signal text-[11px] font-semibold">CURR:</span>
                          <span className="text-foreground font-semibold">{ev.currentHash.slice(0, 10)}…</span>
                        </div>
                      </div>

                      <button
                        onClick={() => handleCopy(ev.currentHash)}
                        className="p-1.5 text-muted-foreground hover:text-foreground rounded border border-border"
                        title="Copy SHA-256 Digest"
                      >
                        {copiedText === ev.currentHash ? <Check className="h-3.5 w-3.5 text-signal" /> : <Copy className="h-3.5 w-3.5" />}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}

        {activeTab === 'INGEST' && (
          <form onSubmit={handleIngest} className="max-w-2xl space-y-4">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-medium text-foreground mb-1">Actor Reference</label>
                <input
                  type="text"
                  value={actor}
                  onChange={(e) => setActor(e.target.value)}
                  className="w-full px-3 py-2 text-xs font-mono rounded border border-border bg-card focus:outline-none focus:border-signal"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-foreground mb-1">Action Identifier</label>
                <input
                  type="text"
                  value={action}
                  onChange={(e) => setAction(e.target.value)}
                  className="w-full px-3 py-2 text-xs font-mono rounded border border-border bg-card focus:outline-none focus:border-signal"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-medium text-foreground mb-1">Resource Target Reference</label>
              <input
                type="text"
                value={resource}
                onChange={(e) => setResource(e.target.value)}
                className="w-full px-3 py-2 text-xs font-mono rounded border border-border bg-card focus:outline-none focus:border-signal"
                required
              />
            </div>

            <div>
              <label className="block text-xs font-medium text-foreground mb-1">
                Contextual Metadata JSON (Subject to RFC 8785 Canonical Normalization)
              </label>
              <textarea
                value={metaJson}
                onChange={(e) => setMetaJson(e.target.value)}
                rows={3}
                className="w-full px-3 py-2 text-xs font-mono rounded border border-border bg-card focus:outline-none focus:border-signal"
                required
              />
            </div>

            <div className="pt-2 flex items-center justify-between">
              <span className="text-xs text-muted-foreground font-mono">
                {ingestStatus || 'Payload will be sealed into the sequential chain.'}
              </span>

              <button
                type="submit"
                className="px-4 py-2 text-xs font-semibold bg-primary text-primary-foreground rounded hover:bg-primary/90 transition-all flex items-center gap-1.5"
              >
                <Send className="h-3.5 w-3.5" />
                Commit to Chain
              </button>
            </div>
          </form>
        )}

        {activeTab === 'VERIFY' && (
          <div className="space-y-6 max-w-3xl">
            <div className="p-4 rounded border border-border bg-[#FBF9F5] flex items-center justify-between gap-4">
              <div>
                <h4 className="text-sm font-semibold text-foreground">Cryptographic Verification Engine</h4>
                <p className="text-xs text-muted-foreground mt-0.5">
                  Walks every event from sequence #1 to latest, recalculating RFC 8785 metadata and SHA-256 payload digests.
                </p>
              </div>

              <button
                onClick={handleRunVerification}
                disabled={isVerifying}
                className="px-4 py-2 text-xs font-semibold bg-signal text-signal-foreground rounded hover:bg-signal/90 transition-all flex items-center gap-2 whitespace-nowrap"
              >
                <ShieldCheck className={`h-4 w-4 ${isVerifying ? 'animate-spin' : ''}`} />
                {isVerifying ? 'Verifying Chain...' : 'Trigger Integrity Walk'}
              </button>
            </div>

            {verificationResult && (
              <div className={`p-4 rounded border font-mono text-xs ${
                verificationResult.result === 'VERIFIED'
                  ? 'border-signal/40 bg-signal/5 text-foreground'
                  : 'border-destructive/40 bg-destructive/5 text-destructive'
              }`}>
                <div className="flex items-center gap-2 font-bold mb-2">
                  {verificationResult.result === 'VERIFIED' ? (
                    <CheckCircle2 className="h-4 w-4 text-signal" />
                  ) : (
                    <XCircle className="h-4 w-4 text-destructive" />
                  )}
                  <span>VERIFICATION AUDIT RESULT: {verificationResult.result}</span>
                </div>

                <div className="space-y-1 text-muted-foreground">
                  <div>• Total Events Validated: {verificationResult.totalEventsChecked}</div>
                  <div>• Sequence Range: #{verificationResult.startSequenceNumber} to #{verificationResult.endSequenceNumber}</div>
                  <div>• Completed At: {new Date(verificationResult.completedAt).toLocaleString()}</div>
                  {verificationResult.failureReason && (
                    <div className="text-destructive font-semibold pt-2">
                      • Failure Diagnostic: {verificationResult.failureReason}
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};
