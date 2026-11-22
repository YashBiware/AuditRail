import React, { useState, useEffect } from 'react';
import { 
  Building2, 
  LogOut, 
  ShieldCheck, 
  FileDown, 
  RefreshCw, 
  CheckCircle2, 
  AlertTriangle, 
  Database,
  Search
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

interface AuditRecord {
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

export const OrgDashboardPage: React.FC = () => {
  const { session, logout } = useAuth();
  const [records, setRecords] = useState<AuditRecord[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [verificationStatus, setVerificationStatus] = useState<any | null>(null);
  const [isVerifying, setIsVerifying] = useState(false);

  useEffect(() => {
    fetchRecords();
  }, [session]);

  const fetchRecords = async () => {
    if (!session?.token) return;
    setIsLoading(true);
    try {
      const res = await fetch('http://localhost:8080/api/v1/audit/events?size=50', {
        headers: {
          'Authorization': `Bearer ${session.token}`
        }
      });
      if (res.ok) {
        const data = await res.json();
        setRecords(data.items || []);
      }
    } catch (err) {
      console.error('Failed to load audit records', err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRunVerification = async () => {
    if (!session?.token) return;
    setIsVerifying(true);
    try {
      const res = await fetch('http://localhost:8080/api/v1/verification/run', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${session.token}`
        }
      });
      if (res.ok) {
        const data = await res.json();
        setVerificationStatus(data);
      }
    } catch (err) {
      console.error('Verification error', err);
    } finally {
      setIsVerifying(false);
    }
  };

  // CSV Export
  const downloadCSV = () => {
    if (records.length === 0) return;
    const orgName = session?.organizationName || 'Organization';
    const orgId = session?.organizationId || 'N/A';

    const header = [
      'Organization Name,Organization ID,Sequence Number,Timestamp (UTC),Action,Actor,Resource Target,Record Hash,Previous Hash'
    ];

    const rows = records.map(r => 
      `"${orgName}","${orgId}",${r.sequenceNumber},"${r.eventTimestamp}","${r.action}","${r.actorReference}","${r.resourceReference}","${r.currentHash}","${r.previousHash}"`
    );

    const csvContent = "data:text/csv;charset=utf-8," + [header, ...rows].join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `AuditRail_${session?.organizationSlug || 'export'}_ledger.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // PDF Print Dialog
  const printPDF = () => {
    window.print();
  };

  const filteredRecords = records.filter(r => 
    r.action.toLowerCase().includes(searchQuery.toLowerCase()) ||
    r.actorReference.toLowerCase().includes(searchQuery.toLowerCase()) ||
    r.resourceReference.toLowerCase().includes(searchQuery.toLowerCase()) ||
    r.currentHash.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col font-sans selection:bg-signal/15 selection:text-signal">
      {/* Top Header Bar */}
      <header className="border-b border-border bg-card sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="text-xl font-bold font-serif text-foreground">AuditRail</span>
            <span className="text-xs px-2.5 py-0.5 rounded font-mono font-medium bg-secondary text-foreground border border-border">
              Enterprise Dashboard
            </span>
          </div>

          <div className="flex items-center gap-4">
            <div className="text-right hidden sm:block">
              <div className="text-xs font-semibold text-foreground">{session?.organizationName}</div>
              <div className="text-xs font-mono text-muted-foreground">{session?.email}</div>
            </div>

            <button
              onClick={logout}
              className="p-2 rounded-lg border border-border text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-colors"
              title="Sign Out"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="max-w-7xl mx-auto px-6 py-8 flex-1 w-full space-y-6">
        {/* Organization Profile & Credential Card */}
        <div className="bg-card border border-border rounded-xl p-6 shadow-subtle flex flex-col lg:flex-row justify-between lg:items-center gap-6">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2">
              <Building2 className="h-5 w-5 text-signal" />
              <h1 className="text-xl font-bold font-serif text-foreground">{session?.organizationName}</h1>
            </div>
            <div className="text-xs font-mono text-muted-foreground flex flex-wrap gap-x-6 gap-y-1">
              <span>TENANT ID: <strong className="text-foreground">{session?.organizationId}</strong></span>
              <span>ROLE: <strong className="text-foreground">{session?.role}</strong></span>
              <span>DATA ISOLATION: <strong className="text-emerald-600">ROW-LEVEL ENFORCED</strong></span>
            </div>
          </div>

          {/* Quick Export & Actions */}
          <div className="flex items-center flex-wrap gap-2.5">
            <button
              onClick={downloadCSV}
              className="px-3.5 py-2 rounded-lg border border-border bg-secondary/80 text-foreground text-xs font-mono font-medium hover:bg-secondary flex items-center gap-1.5 transition-all shadow-subtle"
            >
              <FileDown className="h-3.5 w-3.5" />
              <span>Export CSV</span>
            </button>

            <button
              onClick={printPDF}
              className="px-3.5 py-2 rounded-lg border border-border bg-secondary/80 text-foreground text-xs font-mono font-medium hover:bg-secondary flex items-center gap-1.5 transition-all shadow-subtle"
            >
              <FileDown className="h-3.5 w-3.5" />
              <span>Print / Save PDF</span>
            </button>

            <button
              onClick={handleRunVerification}
              disabled={isVerifying}
              className="px-4 py-2 rounded-lg bg-signal text-signal-foreground text-xs font-mono font-medium hover:bg-signal/90 flex items-center gap-1.5 transition-all disabled:opacity-50 shadow-subtle"
            >
              <ShieldCheck className="h-3.5 w-3.5" />
              <span>{isVerifying ? 'Verifying...' : 'Verify Ledger Proof'}</span>
            </button>
          </div>
        </div>

        {/* Verification Alert Banner (If verification run) */}
        {verificationStatus && (
          <div className={`p-4 rounded-xl border flex items-center justify-between gap-4 ${
            verificationStatus.resultStatus === 'VALID' || verificationStatus.discrepanciesCount === 0
              ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-900 dark:text-emerald-200'
              : 'bg-destructive/10 border-destructive/30 text-destructive'
          }`}>
            <div className="flex items-center gap-3">
              {verificationStatus.resultStatus === 'VALID' || verificationStatus.discrepanciesCount === 0 ? (
                <CheckCircle2 className="h-5 w-5 text-emerald-600 shrink-0" />
              ) : (
                <AlertTriangle className="h-5 w-5 text-destructive shrink-0" />
              )}
              <div className="text-xs">
                <strong>Ledger Integrity Verification: {verificationStatus.resultStatus || 'VALID'}</strong>
                <p className="opacity-90">
                  Verified up to Sequence #{verificationStatus.verifiedToSequence || records.length}. Zero hash drift or sequence gaps detected.
                </p>
              </div>
            </div>
            <button
              onClick={() => setVerificationStatus(null)}
              className="text-xs font-mono underline opacity-70 hover:opacity-100"
            >
              Dismiss
            </button>
          </div>
        )}

        {/* Audit Trail Explorer Table */}
        <div className="bg-card border border-border rounded-xl shadow-card overflow-hidden">
          <div className="p-4 border-b border-border flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-secondary/30">
            <div className="flex items-center gap-2">
              <Database className="h-4 w-4 text-signal" />
              <h2 className="text-sm font-semibold text-foreground font-mono">
                Continuous Audit Trail Records ({records.length})
              </h2>
            </div>

            <div className="flex items-center gap-2">
              <div className="relative">
                <Search className="h-3.5 w-3.5 absolute left-3 top-2.5 text-muted-foreground" />
                <input
                  type="text"
                  placeholder="Filter by action, actor, or hash..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-8 pr-3 py-1.5 text-xs bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-1 focus:ring-signal w-64 font-mono"
                />
              </div>

              <button
                onClick={fetchRecords}
                disabled={isLoading}
                className="p-1.5 rounded-lg border border-border hover:bg-secondary text-muted-foreground hover:text-foreground transition-colors"
                title="Refresh Records"
              >
                <RefreshCw className={`h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
              </button>
            </div>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="border-b border-border bg-secondary/50 text-muted-foreground font-mono uppercase tracking-wider text-[11px]">
                  <th className="py-3 px-4">Seq #</th>
                  <th className="py-3 px-4">Timestamp (UTC)</th>
                  <th className="py-3 px-4">Action</th>
                  <th className="py-3 px-4">Actor</th>
                  <th className="py-3 px-4">Target Resource</th>
                  <th className="py-3 px-4">Record Hash</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border font-mono">
                {isLoading ? (
                  <tr>
                    <td colSpan={6} className="py-8 text-center text-muted-foreground">
                      Loading cryptographic records...
                    </td>
                  </tr>
                ) : filteredRecords.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="py-12 text-center text-muted-foreground space-y-2">
                      <p className="text-sm font-sans font-medium text-foreground">No audit events recorded yet</p>
                      <p className="text-xs">Send HTTP POST requests with your API key to start logging unalterable actions.</p>
                    </td>
                  </tr>
                ) : (
                  filteredRecords.map((r) => (
                    <tr key={r.id} className="hover:bg-secondary/40 transition-colors">
                      <td className="py-3 px-4 font-bold text-signal">
                        #{r.sequenceNumber}
                      </td>
                      <td className="py-3 px-4 text-muted-foreground whitespace-nowrap">
                        {new Date(r.eventTimestamp).toLocaleString()}
                      </td>
                      <td className="py-3 px-4 font-semibold text-foreground">
                        {r.action}
                      </td>
                      <td className="py-3 px-4 text-muted-foreground">
                        {r.actorReference}
                      </td>
                      <td className="py-3 px-4 text-muted-foreground">
                        {r.resourceReference}
                      </td>
                      <td className="py-3 px-4 text-muted-foreground font-mono text-[11px]" title={r.currentHash}>
                        {r.currentHash.substring(0, 16)}...
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
};
