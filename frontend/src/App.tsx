import React, { useState } from 'react';
import { 
  ChevronRight, 
  ArrowUpRight, 
  Terminal 
} from 'lucide-react';
import { LiveAuditConsole } from './components/LiveAuditConsole';

export const App: React.FC = () => {
  const [activeStage, setActiveStage] = useState<number>(1);

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col font-sans selection:bg-signal/15 selection:text-signal">
      {/* Top Editorial Rule & Header */}
      <header className="border-b border-border bg-background/95 backdrop-blur sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="text-xl font-bold tracking-tight text-foreground font-serif">
              AuditRail
            </span>
            <span className="mono-tag px-2 py-0.5 rounded border border-border text-muted-foreground bg-secondary/50">
              v1.0-ENTERPRISE
            </span>
          </div>

          <nav className="hidden md:flex items-center gap-8 text-sm font-medium text-muted-foreground">
            <a href="#problem" className="hover:text-foreground transition-colors">The Problem</a>
            <a href="#how-it-works" className="hover:text-foreground transition-colors">How It Works</a>
            <a href="#architecture" className="hover:text-foreground transition-colors">Architecture</a>
            <a href="#live-console" className="hover:text-foreground transition-colors">Live Console</a>
            <a href="#security" className="hover:text-foreground transition-colors">Security Principles</a>
          </nav>

          <div className="flex items-center gap-3">
            <a 
              href="https://github.com/YashBiware/AuditRail.git" 
              target="_blank" 
              rel="noreferrer"
              className="text-xs font-mono text-muted-foreground hover:text-foreground px-3 py-1.5 rounded border border-border hover:bg-secondary transition-all flex items-center gap-1.5"
            >
              <Terminal className="h-3.5 w-3.5" />
              <span>GitHub</span>
              <ArrowUpRight className="h-3 w-3" />
            </a>

            <a 
              href="#live-console"
              className="text-xs font-semibold px-4 py-2 rounded bg-primary text-primary-foreground hover:bg-primary/90 transition-all font-mono"
            >
              Open Live Ledger
            </a>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="flex-1">
        <section className="pt-20 pb-16 px-6 border-b border-border">
          <div className="max-w-4xl mx-auto text-center space-y-6">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-border bg-secondary/60 text-xs font-mono text-muted-foreground">
              <span className="h-2 w-2 rounded-full bg-signal"></span>
              Application-Independent Tamper-Evident Ledger
            </div>

            <h1 className="text-4xl sm:text-6xl lg:text-7xl serif-heading text-foreground tracking-tight leading-[1.08]">
              Every action leaves a trail.<br />
              <span className="italic">Make yours verifiable.</span>
            </h1>

            <p className="max-w-2xl mx-auto text-base sm:text-lg text-muted-foreground leading-relaxed">
              AuditRail creates a structured, cryptographically verifiable record of enterprise actions. 
              Built on sequential SHA-256 hash chaining, RFC 8785 canonical metadata normalization, 
              and out-of-band Write-Once-Read-Many (WORM) storage checkpoints.
            </p>

            <div className="pt-4 flex flex-wrap items-center justify-center gap-4">
              <a
                href="#live-console"
                className="px-5 py-2.5 rounded bg-primary text-primary-foreground font-semibold text-xs hover:bg-primary/90 transition-all shadow-subtle flex items-center gap-2 font-mono"
              >
                Access Live Audit Stream
                <ChevronRight className="h-4 w-4" />
              </a>
              <a
                href="#architecture"
                className="px-5 py-2.5 rounded border border-border bg-card text-foreground font-semibold text-xs hover:bg-secondary transition-all"
              >
                View System Architecture
              </a>
            </div>

            {/* Scope / Model / Access Strip */}
            <div className="pt-12 grid grid-cols-1 sm:grid-cols-3 gap-px bg-border max-w-3xl mx-auto border border-border rounded-lg overflow-hidden shadow-subtle">
              <div className="bg-card p-4 text-left">
                <span className="mono-tag text-muted-foreground block">SCOPE</span>
                <span className="text-sm font-semibold text-foreground mt-0.5 block">Audit Trail Layer</span>
                <span className="text-xs text-muted-foreground mt-1 block">Contextual metadata only; zero raw PII persisted.</span>
              </div>
              <div className="bg-card p-4 text-left">
                <span className="mono-tag text-muted-foreground block">MODEL</span>
                <span className="text-sm font-semibold text-foreground mt-0.5 block">Append-Only Records</span>
                <span className="text-xs text-muted-foreground mt-1 block">Monotonic sequences under pessimistic row locks.</span>
              </div>
              <div className="bg-card p-4 text-left">
                <span className="mono-tag text-muted-foreground block">ACCESS</span>
                <span className="text-sm font-semibold text-foreground mt-0.5 block">Role-Scoped Review</span>
                <span className="text-xs text-muted-foreground mt-1 block">Dual-role segregation for Administrators & Auditors.</span>
              </div>
            </div>
          </div>
        </section>

        {/* Live Operational Console */}
        <section id="live-console" className="py-16 px-6 bg-[#FBF9F5] border-b border-border">
          <div className="max-w-6xl mx-auto space-y-6">
            <div className="max-w-2xl">
              <span className="mono-tag text-signal font-semibold uppercase tracking-wider">
                Interactive Operational Environment
              </span>
              <h2 className="text-2xl sm:text-3xl serif-heading text-foreground mt-1">
                Connected Backend State & Verification
              </h2>
              <p className="text-xs text-muted-foreground mt-1.5 leading-relaxed">
                Directly communicate with the running Spring Boot backend. Query stored events, append new 
                records into the SHA-256 chain, or trigger real-time cryptographic verification walks.
              </p>
            </div>

            <LiveAuditConsole />
          </div>
        </section>

        {/* The Problem & Evidentiary Value */}
        <section id="problem" className="py-20 px-6 border-b border-border">
          <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
            <div className="space-y-4">
              <span className="mono-tag text-muted-foreground uppercase tracking-wider">The Problem</span>
              <h2 className="text-3xl sm:text-4xl serif-heading text-foreground leading-tight">
                Records of what happened are easy to keep — and easy to doubt.
              </h2>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Organizations that operate critical data systems are frequently asked a simple question: 
                <span className="text-foreground font-medium"> what happened, when, by whom, and can you show it?</span>
              </p>
              <p className="text-sm text-muted-foreground leading-relaxed">
                Ordinary database tables and application logs can be edited, dropped, or reconstructed by 
                privileged database administrators (DBAs) or compromised cloud credentials. Because of this, 
                conventional logs carry limited evidentiary weight when scrutinized under compliance reviews.
              </p>
              <p className="text-sm text-foreground font-medium leading-relaxed">
                AuditRail explores the opposite posture — a record designed from the start to be append-only, 
                cryptographically chained, and independently recomputable.
              </p>
            </div>

            {/* Step-by-step pipeline card */}
            <div className="bg-card border border-border rounded-lg p-6 shadow-subtle space-y-5">
              <h4 className="text-xs font-semibold text-foreground uppercase tracking-wider mono-tag border-b border-border pb-3">
                From Action to Evidentiary Proof
              </h4>

              <div className="space-y-3.5 text-xs">
                <div className="flex items-start gap-3">
                  <span className="mono-tag h-5 w-5 rounded bg-secondary flex items-center justify-center font-bold text-foreground">01</span>
                  <div>
                    <span className="font-semibold text-foreground block">Action</span>
                    <span className="text-muted-foreground">A human operator or background service mutates a critical system.</span>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <span className="mono-tag h-5 w-5 rounded bg-secondary flex items-center justify-center font-bold text-foreground">02</span>
                  <div>
                    <span className="font-semibold text-foreground block">Record</span>
                    <span className="text-muted-foreground">A structured event captures actor, action, resource target, and timestamp.</span>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <span className="mono-tag h-5 w-5 rounded bg-secondary flex items-center justify-center font-bold text-foreground">03</span>
                  <div>
                    <span className="font-semibold text-foreground block">Hash</span>
                    <span className="text-muted-foreground">The event is linked to its predecessor via a deterministic SHA-256 digest.</span>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <span className="mono-tag h-5 w-5 rounded bg-secondary flex items-center justify-center font-bold text-foreground">04</span>
                  <div>
                    <span className="font-semibold text-foreground block">Verification</span>
                    <span className="text-muted-foreground">Any authorized compliance officer can recompute and compare the entire chain.</span>
                  </div>
                </div>

                <div className="flex items-start gap-3">
                  <span className="mono-tag h-5 w-5 rounded bg-secondary flex items-center justify-center font-bold text-signal">05</span>
                  <div>
                    <span className="font-semibold text-foreground block">Evidence</span>
                    <span className="text-muted-foreground">An immutable trail that can be logically and mathematically reasoned about.</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Five Stages: One Continuous Record */}
        <section id="how-it-works" className="py-20 px-6 border-b border-border bg-[#FBF9F5]">
          <div className="max-w-5xl mx-auto space-y-12">
            <div className="text-center space-y-3">
              <span className="mono-tag text-muted-foreground uppercase tracking-wider">How It Works</span>
              <h2 className="text-3xl sm:text-4xl serif-heading text-foreground">
                Five stages, one continuous record.
              </h2>
              <p className="text-sm text-muted-foreground max-w-xl mx-auto">
                How every audit event transitions from an application touchpoint to an externally anchored evidentiary ledger.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
              <div 
                onClick={() => setActiveStage(1)}
                className={`p-5 rounded-lg border transition-all cursor-pointer ${
                  activeStage === 1 ? 'bg-card border-foreground shadow-subtle' : 'bg-card/60 border-border hover:bg-card'
                }`}
              >
                <span className="mono-tag text-muted-foreground text-xs block">STAGE 01</span>
                <h4 className="text-base font-semibold text-foreground mt-2">Capture</h4>
                <p className="text-xs text-muted-foreground mt-2 leading-relaxed">
                  A structured event records actor, action, resource target, purpose, and timestamp.
                </p>
              </div>

              <div 
                onClick={() => setActiveStage(2)}
                className={`p-5 rounded-lg border transition-all cursor-pointer ${
                  activeStage === 2 ? 'bg-card border-foreground shadow-subtle' : 'bg-card/60 border-border hover:bg-card'
                }`}
              >
                <span className="mono-tag text-muted-foreground text-xs block">STAGE 02</span>
                <h4 className="text-base font-semibold text-foreground mt-2">Hash</h4>
                <p className="text-xs text-muted-foreground mt-2 leading-relaxed">
                  Every event participates in a SHA-256 chain linking directly to its predecessor.
                </p>
              </div>

              <div 
                onClick={() => setActiveStage(3)}
                className={`p-5 rounded-lg border transition-all cursor-pointer ${
                  activeStage === 3 ? 'bg-card border-foreground shadow-subtle' : 'bg-card/60 border-border hover:bg-card'
                }`}
              >
                <span className="mono-tag text-muted-foreground text-xs block">STAGE 03</span>
                <h4 className="text-base font-semibold text-foreground mt-2">Order</h4>
                <p className="text-xs text-muted-foreground mt-2 leading-relaxed">
                  Pessimistic row locking enforces strictly monotonic sequences without concurrency gaps.
                </p>
              </div>

              <div 
                onClick={() => setActiveStage(4)}
                className={`p-5 rounded-lg border transition-all cursor-pointer ${
                  activeStage === 4 ? 'bg-card border-foreground shadow-subtle' : 'bg-card/60 border-border hover:bg-card'
                }`}
              >
                <span className="mono-tag text-muted-foreground text-xs block">STAGE 04</span>
                <h4 className="text-base font-semibold text-foreground mt-2">WORM Anchor</h4>
                <p className="text-xs text-muted-foreground mt-2 leading-relaxed">
                  Periodic chain snapshots export to AWS S3 Object Lock (Compliance Mode).
                </p>
              </div>

              <div 
                onClick={() => setActiveStage(5)}
                className={`p-5 rounded-lg border transition-all cursor-pointer ${
                  activeStage === 5 ? 'bg-card border-foreground shadow-subtle' : 'bg-card/60 border-border hover:bg-card'
                }`}
              >
                <span className="mono-tag text-muted-foreground text-xs block">STAGE 05</span>
                <h4 className="text-base font-semibold text-foreground mt-2">Verify</h4>
                <p className="text-xs text-muted-foreground mt-2 leading-relaxed">
                  Continuous automated hourly walks validate chain continuity and external anchors.
                </p>
              </div>
            </div>
          </div>
        </section>

        {/* Architecture & Security Principles */}
        <section id="architecture" className="py-20 px-6 border-b border-border">
          <div className="max-w-5xl mx-auto space-y-12">
            <div className="text-center space-y-3">
              <span className="mono-tag text-muted-foreground uppercase tracking-wider">Architecture</span>
              <h2 className="text-3xl sm:text-4xl serif-heading text-foreground">
                A conceptual map of the system.
              </h2>
              <p className="text-sm text-muted-foreground max-w-xl mx-auto">
                Modular monolith architecture decoupling transaction ingestion from immutable evidentiary verification.
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              <div className="space-y-4">
                <div className="paper-card p-5 rounded-lg space-y-2">
                  <span className="mono-tag text-signal font-semibold">L1 • FRONTEND CONSOLE</span>
                  <h4 className="text-sm font-semibold text-foreground">Admin Console & Scoped Auditor Views</h4>
                  <p className="text-xs text-muted-foreground">
                    Role-tailored interfaces allowing administrators to configure tenants and auditors to inspect immutable records.
                  </p>
                </div>

                <div className="paper-card p-5 rounded-lg space-y-2">
                  <span className="mono-tag text-signal font-semibold">L2 • INGESTION API GATEWAY</span>
                  <h4 className="text-sm font-semibold text-foreground">Machine Authentication & Scoped Routing</h4>
                  <p className="text-xs text-muted-foreground">
                    High-throughput REST gateway validating callers via hashed API keys and establishing tenant context.
                  </p>
                </div>

                <div className="paper-card p-5 rounded-lg space-y-2">
                  <span className="mono-tag text-signal font-semibold">L3 • CANONICAL NORMALIZATION</span>
                  <h4 className="text-sm font-semibold text-foreground">RFC 8785 JSON Canonicalization Scheme (JCS)</h4>
                  <p className="text-xs text-muted-foreground">
                    Converts arbitrary contextual metadata into deterministic byte sequences before digest calculation.
                  </p>
                </div>
              </div>

              <div className="space-y-4">
                <div className="paper-card p-5 rounded-lg space-y-2">
                  <span className="mono-tag text-signal font-semibold">L4 • CONCURRENCY & CHAINING</span>
                  <h4 className="text-sm font-semibold text-foreground">Pessimistic Row Locking (`SELECT ... FOR UPDATE`)</h4>
                  <p className="text-xs text-muted-foreground">
                    Serializes monotonic event sequence allocation per tenant without cross-tenant database locking.
                  </p>
                </div>

                <div className="paper-card p-5 rounded-lg space-y-2">
                  <span className="mono-tag text-signal font-semibold">L5 • IMMUTABLE STORAGE (WORM)</span>
                  <h4 className="text-sm font-semibold text-foreground">Decoupled AWS S3 Object Lock Checkpoints</h4>
                  <p className="text-xs text-muted-foreground">
                    Snapshots stored in write-once-read-many mode preventing retroactive database rewrite attacks.
                  </p>
                </div>

                <div className="paper-card p-5 rounded-lg space-y-2">
                  <span className="mono-tag text-signal font-semibold">L6 • VERIFICATION ENGINE</span>
                  <h4 className="text-sm font-semibold text-foreground">Automated Chain Walk & Drift Detection</h4>
                  <p className="text-xs text-muted-foreground">
                    Continuously walks the stored chain to immediately flag content modifications or deleted row gaps.
                  </p>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Security Principles */}
        <section id="security" className="py-20 px-6 bg-[#FBF9F5]">
          <div className="max-w-5xl mx-auto space-y-12">
            <div className="text-center space-y-3">
              <span className="mono-tag text-muted-foreground uppercase tracking-wider">Security Principles</span>
              <h2 className="text-3xl sm:text-4xl serif-heading text-foreground">
                The posture the architecture is built around.
              </h2>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4">
              <div className="paper-card p-5 rounded-lg">
                <span className="text-xs font-semibold text-foreground block">Append-only records</span>
                <span className="text-xs text-muted-foreground mt-1.5 block">Records are added, never overwritten or deleted.</span>
              </div>

              <div className="paper-card p-5 rounded-lg">
                <span className="text-xs font-semibold text-foreground block">Hash-chain integrity</span>
                <span className="text-xs text-muted-foreground mt-1.5 block">Every record references the previous SHA-256 digest.</span>
              </div>

              <div className="paper-card p-5 rounded-lg">
                <span className="text-xs font-semibold text-foreground block">Immutable WORM storage</span>
                <span className="text-xs text-muted-foreground mt-1.5 block">Anchored against AWS S3 Compliance Object Lock.</span>
              </div>

              <div className="paper-card p-5 rounded-lg">
                <span className="text-xs font-semibold text-foreground block">Role-scoped review</span>
                <span className="text-xs text-muted-foreground mt-1.5 block">Auditors receive strictly read-only scoped access.</span>
              </div>

              <div className="paper-card p-5 rounded-lg">
                <span className="text-xs font-semibold text-foreground block">Mandatory TOTP MFA</span>
                <span className="text-xs text-muted-foreground mt-1.5 block">Second factor required for every human account.</span>
              </div>

              <div className="paper-card p-5 rounded-lg">
                <span className="text-xs font-semibold text-foreground block">Lockout protection</span>
                <span className="text-xs text-muted-foreground mt-1.5 block">15-minute lockout after 5 consecutive failed attempts.</span>
              </div>

              <div className="paper-card p-5 rounded-lg">
                <span className="text-xs font-semibold text-foreground block">Tenant boundary</span>
                <span className="text-xs text-muted-foreground mt-1.5 block">Zero trust placed on client-provided tenant input.</span>
              </div>

              <div className="paper-card p-5 rounded-lg">
                <span className="text-xs font-semibold text-foreground block">Verification history</span>
                <span className="text-xs text-muted-foreground mt-1.5 block">Every verification walk is itself immutably recorded.</span>
              </div>
            </div>
          </div>
        </section>
      </main>

      {/* Editorial Clean Footer */}
      <footer className="border-t border-border py-12 px-6 bg-background">
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-between gap-6 text-xs text-muted-foreground">
          <div className="flex items-center gap-3">
            <span className="text-base font-serif font-bold text-foreground">AuditRail</span>
            <span>•</span>
            <span>Application-Independent Audit Log Integrity</span>
          </div>

          <div className="flex items-center gap-6 mono-tag">
            <a href="https://github.com/YashBiware/AuditRail.git" target="_blank" rel="noreferrer" className="hover:text-foreground">
              GitHub Repository
            </a>
            <span>•</span>
            <span>DPDP Act 2023 Safeguards</span>
            <span>•</span>
            <span>Not Blockchain • Tamper-Evident</span>
          </div>
        </div>
      </footer>
    </div>
  );
};
export default App;
