import React from 'react';
import { 
  ShieldCheck, 
  Terminal, 
  Database, 
  Lock, 
  CheckCircle2, 
  ChevronRight, 
  ArrowUpRight 
} from 'lucide-react';
import { InteractiveSandbox } from './components/InteractiveSandbox';

export const App: React.FC = () => {
  return (
    <div className="min-h-screen bg-bg-primary text-text-primary selection:bg-accent-emerald/20 selection:text-accent-emerald flex flex-col font-sans">
      {/* Top Ambient Glow */}
      <div className="absolute top-0 left-1/2 -translate-x-1/2 w-3/4 max-w-5xl h-96 bg-gradient-to-b from-accent-emerald/10 via-accent-cyan/5 to-transparent blur-3xl pointer-events-none -z-10" />

      {/* Navigation Header */}
      <header className="sticky top-0 z-50 backdrop-blur-md bg-bg-primary/80 border-b border-border-subtle">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="h-9 w-9 rounded-xl bg-gradient-to-br from-accent-emerald/30 to-accent-cyan/20 border border-accent-emerald/40 flex items-center justify-center glow-emerald">
              <ShieldCheck className="h-5 w-5 text-accent-emerald" />
            </div>
            <span className="text-lg font-bold tracking-tight text-text-primary">
              Audit<span className="text-accent-emerald">Rail</span>
            </span>
            <span className="text-[10px] font-mono px-2 py-0.5 rounded-full border border-border-strong text-text-secondary bg-bg-secondary">
              v1.0-ENTERPRISE
            </span>
          </div>

          <nav className="hidden md:flex items-center gap-8 text-sm font-medium text-text-secondary">
            <a href="#how-it-works" className="hover:text-text-primary transition-colors">How It Works</a>
            <a href="#architecture" className="hover:text-text-primary transition-colors">Architecture</a>
            <a href="#interactive-simulator" className="hover:text-text-primary transition-colors">Live Chaining</a>
            <a href="#compliance" className="hover:text-text-primary transition-colors">Compliance</a>
          </nav>

          <div className="flex items-center gap-3">
            <a 
              href="https://github.com/YashBiware/AuditRail.git" 
              target="_blank" 
              rel="noreferrer"
              className="text-xs font-mono text-text-secondary hover:text-text-primary px-3 py-2 rounded-lg border border-border-subtle hover:border-border-strong transition-all flex items-center gap-1.5"
            >
              <Terminal className="h-3.5 w-3.5" />
              <span>GitHub</span>
              <ArrowUpRight className="h-3 w-3 text-text-muted" />
            </a>
            <a 
              href="#interactive-simulator"
              className="text-xs font-semibold px-4 py-2 rounded-lg bg-accent-emerald text-bg-primary hover:bg-emerald-400 transition-all font-mono"
            >
              Launch Simulator
            </a>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-1">
        <section className="relative pt-24 pb-20 px-6 overflow-hidden">
          <div className="max-w-5xl mx-auto text-center space-y-6">
            <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-accent-emerald/30 bg-accent-emerald/5 text-xs font-mono text-accent-emerald">
              <span className="relative flex h-2 w-2">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-accent-emerald opacity-75"></span>
                <span className="relative inline-flex rounded-full h-2 w-2 bg-accent-emerald"></span>
              </span>
              Enterprise Tamper-Evident Audit Platform • Not Blockchain
            </div>

            <h1 className="text-4xl sm:text-6xl lg:text-7xl font-extrabold tracking-tight text-text-primary leading-[1.1]">
              Cryptographic Integrity for <br />
              <span className="bg-gradient-to-r from-text-primary via-accent-emerald to-accent-cyan bg-clip-text text-transparent">
                Mission-Critical Audit Trails
              </span>
            </h1>

            <p className="max-w-3xl mx-auto text-base sm:text-lg text-text-secondary leading-relaxed">
              AuditRail delivers mathematical certainty to compliance logging. Built with 
              <span className="text-text-primary font-medium"> SHA-256 sequential hash-chaining</span>, 
              RFC 8785 canonical metadata serialization, and independent 
              <span className="text-text-primary font-medium"> AWS S3 Object Lock (WORM)</span> checkpoints.
            </p>

            <div className="pt-4 flex flex-wrap items-center justify-center gap-4">
              <a
                href="#interactive-simulator"
                className="px-6 py-3 rounded-xl bg-accent-emerald text-bg-primary font-semibold text-sm hover:bg-emerald-400 transition-all shadow-lg shadow-emerald-950/30 flex items-center gap-2 font-mono"
              >
                Explore Live Chaining Simulator
                <ChevronRight className="h-4 w-4" />
              </a>
              <a
                href="#architecture"
                className="px-6 py-3 rounded-xl border border-border-strong bg-bg-secondary text-text-primary font-semibold text-sm hover:bg-bg-card transition-all flex items-center gap-2"
              >
                Inspect Technical Architecture
              </a>
            </div>

            {/* Quick Metrics Bar */}
            <div className="pt-12 grid grid-cols-2 md:grid-cols-4 gap-4 max-w-4xl mx-auto text-left">
              <div className="p-4 rounded-xl border border-border-subtle bg-bg-secondary/70">
                <span className="text-xs font-mono text-text-muted block">DIGEST ALGORITHM</span>
                <span className="text-lg font-bold text-text-primary font-mono mt-1 block">SHA-256</span>
                <span className="text-[11px] text-accent-emerald mt-0.5 block">NIST FIPS 180-4</span>
              </div>
              <div className="p-4 rounded-xl border border-border-subtle bg-bg-secondary/70">
                <span className="text-xs font-mono text-text-muted block">STORAGE IMMUTABILITY</span>
                <span className="text-lg font-bold text-text-primary font-mono mt-1 block">AWS S3 WORM</span>
                <span className="text-[11px] text-accent-cyan mt-0.5 block">Object Lock Compliance</span>
              </div>
              <div className="p-4 rounded-xl border border-border-subtle bg-bg-secondary/70">
                <span className="text-xs font-mono text-text-muted block">CONCURRENCY ENGINE</span>
                <span className="text-lg font-bold text-text-primary font-mono mt-1 block">Pessimistic Lock</span>
                <span className="text-[11px] text-accent-emerald mt-0.5 block">Zero Tenant Collisions</span>
              </div>
              <div className="p-4 rounded-xl border border-border-subtle bg-bg-secondary/70">
                <span className="text-xs font-mono text-text-muted block">JSON NORMALIZATION</span>
                <span className="text-lg font-bold text-text-primary font-mono mt-1 block">RFC 8785 JCS</span>
                <span className="text-[11px] text-text-secondary mt-0.5 block">Deterministic Hash</span>
              </div>
            </div>
          </div>
        </section>

        {/* Live Simulator Section */}
        <section id="interactive-simulator" className="py-16 px-6 bg-bg-secondary/40 border-y border-border-subtle">
          <div className="max-w-6xl mx-auto space-y-6">
            <div className="text-center space-y-2">
              <h2 className="text-2xl sm:text-3xl font-bold tracking-tight text-text-primary">
                Experience Cryptographic Evidentiary Proof
              </h2>
              <p className="text-sm text-text-secondary max-w-2xl mx-auto">
                Any retroactive alteration—even by a database superuser—instantly invalidates the 
                SHA-256 continuum and triggers audit mismatch flags.
              </p>
            </div>

            <InteractiveSandbox />
          </div>
        </section>

        {/* Technical Architecture Section */}
        <section id="architecture" className="py-20 px-6">
          <div className="max-w-6xl mx-auto space-y-12">
            <div className="text-center space-y-3">
              <span className="text-xs font-mono text-accent-emerald uppercase tracking-widest">
                Defense In Depth
              </span>
              <h2 className="text-3xl sm:text-4xl font-extrabold tracking-tight text-text-primary">
                Two-Tier Verification Architecture
              </h2>
              <p className="text-text-secondary max-w-2xl mx-auto text-sm sm:text-base">
                AuditRail combines relational performance with decoupled physical storage anchors to prevent database rewriting.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {/* Level 1 Card */}
              <div className="rounded-2xl border border-border-subtle bg-bg-secondary p-8 space-y-5 relative overflow-hidden">
                <div className="h-10 w-10 rounded-xl bg-accent-emerald/10 border border-accent-emerald/20 flex items-center justify-center">
                  <Database className="h-5 w-5 text-accent-emerald" />
                </div>
                <h3 className="text-xl font-bold text-text-primary">
                  Level 1: Internal Hash-Chain Continuity
                </h3>
                <p className="text-sm text-text-secondary leading-relaxed">
                  Every ingested event computes a strictly deterministic payload:
                  <code className="block mt-2 p-2.5 rounded bg-bg-primary text-xs font-mono text-accent-emerald border border-border-subtle">
                    v1|orgId|seq|actor|action|resource|isoTime|metaHash|prevHash
                  </code>
                  Serialized under <span className="text-text-primary font-medium">Pessimistic Row Locking (`SELECT ... FOR UPDATE`)</span>, 
                  guaranteeing atomic monotonic sequences (1, 2, 3...) per tenant without sequence gaps.
                </p>
                <ul className="text-xs text-text-secondary space-y-2 pt-2">
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-accent-emerald" />
                    <span>Detects dropped rows (sequence gap detection)</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-accent-emerald" />
                    <span>Detects modified values in actor, action, timestamp, or payload</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-accent-emerald" />
                    <span>Zero blockchain overhead, high throughput MySQL transaction speed</span>
                  </li>
                </ul>
              </div>

              {/* Level 2 Card */}
              <div className="rounded-2xl border border-border-subtle bg-bg-secondary p-8 space-y-5 relative overflow-hidden">
                <div className="h-10 w-10 rounded-xl bg-accent-cyan/10 border border-accent-cyan/20 flex items-center justify-center">
                  <Lock className="h-5 w-5 text-accent-cyan" />
                </div>
                <h3 className="text-xl font-bold text-text-primary">
                  Level 2: External Checkpoint Anchor (WORM)
                </h3>
                <p className="text-sm text-text-secondary leading-relaxed">
                  Internal chaining protects against casual tampering. However, if a malicious DBA recalculates 
                  the entire chain, Level 2 stops them cold. Periodic state snapshots are exported to 
                  <span className="text-text-primary font-medium"> AWS S3 Object Lock</span>.
                </p>
                <div className="p-3 rounded-lg bg-bg-primary border border-border-subtle font-mono text-xs text-text-secondary">
                  <span className="text-accent-cyan block">AWS S3 WORM Compliance Mode:</span>
                  <span className="text-text-muted text-[11px]">Cannot be deleted or overwritten by any IAM user or root AWS account.</span>
                </div>
                <ul className="text-xs text-text-secondary space-y-2 pt-2">
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-accent-cyan" />
                    <span>Root-level DBA rewrite immunity</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-accent-cyan" />
                    <span>Continuous automated reconciliation job</span>
                  </li>
                  <li className="flex items-center gap-2">
                    <CheckCircle2 className="h-4 w-4 text-accent-cyan" />
                    <span>Cross-referenced cryptographic receipts for external auditors</span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        {/* Compliance & Standards */}
        <section id="compliance" className="py-16 px-6 bg-bg-secondary/30 border-t border-border-subtle">
          <div className="max-w-6xl mx-auto space-y-8">
            <div className="text-center space-y-2">
              <h2 className="text-2xl font-bold text-text-primary">
                Built for Enterprise Regulatory Frameworks
              </h2>
              <p className="text-xs text-text-secondary">
                Designed to satisfy stringent evidentiary audit requirements across jurisdictions.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="p-5 rounded-xl border border-border-subtle bg-bg-card space-y-2">
                <span className="text-xs font-mono font-bold text-accent-emerald">DPDP ACT 2023</span>
                <h4 className="text-sm font-semibold text-text-primary">Data Protection & Privacy</h4>
                <p className="text-xs text-text-secondary">
                  Enforces strict purpose limitation, non-repudiation of data modifications, and full tenant query isolation.
                </p>
              </div>

              <div className="p-5 rounded-xl border border-border-subtle bg-bg-card space-y-2">
                <span className="text-xs font-mono font-bold text-accent-cyan">SOC 2 TYPE II</span>
                <h4 className="text-sm font-semibold text-text-primary">Trust Services Criteria</h4>
                <p className="text-xs text-text-secondary">
                  Unbroken chain verification satisfies CC6.1, CC6.8, and CC7.2 controls for system monitoring and change integrity.
                </p>
              </div>

              <div className="p-5 rounded-xl border border-border-subtle bg-bg-card space-y-2">
                <span className="text-xs font-mono font-bold text-accent-amber">HIPAA & PCI-DSS</span>
                <h4 className="text-sm font-semibold text-text-primary">Audit Trail Immutability</h4>
                <p className="text-xs text-text-secondary">
                  Requires 1-year verifiable retention of access logs with tamper detection and immutable backup stores.
                </p>
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="border-t border-border-subtle bg-bg-primary py-12 px-6">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6 text-xs text-text-muted">
          <div className="flex items-center gap-3">
            <ShieldCheck className="h-4 w-4 text-accent-emerald" />
            <span className="text-text-secondary font-semibold">AuditRail Platform</span>
            <span>•</span>
            <span>Application-Independent Audit Log Integrity</span>
          </div>

          <div className="flex items-center gap-6">
            <a 
              href="https://github.com/YashBiware/AuditRail.git" 
              target="_blank" 
              rel="noreferrer"
              className="hover:text-text-primary transition-colors font-mono"
            >
              GitHub Repository
            </a>
            <span>•</span>
            <span>MIT License</span>
            <span>•</span>
            <span>Not Blockchain • Tamper-Evident</span>
          </div>
        </div>
      </footer>
    </div>
  );
};
export default App;
