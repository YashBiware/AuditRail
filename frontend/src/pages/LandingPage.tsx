import React from 'react';
import { 
  ArrowRight, 
  Lock, 
  ShieldCheck, 
  Layers, 
  FileCheck2 
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const LandingPage: React.FC = () => {
  const { setCurrentPage } = useAuth();

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col font-sans selection:bg-signal/15 selection:text-signal">
      {/* Top Editorial Header */}
      <header className="border-b border-border bg-background/95 backdrop-blur sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="text-xl font-bold tracking-tight text-foreground font-serif">
              AuditRail
            </span>
            <span className="text-[11px] font-mono px-2 py-0.5 rounded border border-border text-muted-foreground bg-secondary/50">
              Enterprise Assurance
            </span>
          </div>

          <nav className="hidden md:flex items-center gap-8 text-sm font-medium text-muted-foreground">
            <a href="#overview" className="hover:text-foreground transition-colors">Overview</a>
            <a href="#how-it-works" className="hover:text-foreground transition-colors">How It Works</a>
            <a href="#guarantees" className="hover:text-foreground transition-colors">Trust Guarantees</a>
            <a href="#integration" className="hover:text-foreground transition-colors">Integration Steps</a>
          </nav>

          <div className="flex items-center gap-3">
            <button
              onClick={() => setCurrentPage('AUTH')}
              className="text-xs font-mono text-foreground hover:bg-secondary px-3.5 py-2 rounded-lg border border-border transition-all"
            >
              Organization Sign In
            </button>

            <button
              onClick={() => setCurrentPage('AUTH')}
              className="text-xs font-semibold px-4 py-2 rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 transition-all font-mono shadow-subtle flex items-center gap-1.5"
            >
              <span>Get Started</span>
              <ArrowRight className="h-3.5 w-3.5" />
            </button>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section id="overview" className="py-20 md:py-28 border-b border-border">
        <div className="max-w-4xl mx-auto px-6 text-center space-y-6">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-border bg-secondary/60 text-xs font-mono text-signal">
            <ShieldCheck className="h-3.5 w-3.5" />
            <span>Application-Independent Audit Evidentiary Ledger</span>
          </div>

          <h1 className="text-4xl sm:text-5xl md:text-6xl font-bold font-serif tracking-tight text-foreground leading-[1.15]">
            Every enterprise action leaves a record.<br />
            <span className="italic font-normal text-muted-foreground">Make yours indisputable.</span>
          </h1>

          <p className="text-base sm:text-lg text-muted-foreground max-w-2xl mx-auto font-sans leading-relaxed">
            AuditRail creates an unalterable, mathematically sealed ledger of your corporate activities. 
            Designed so regulators, investors, and internal security teams can verify full data integrity with 100% certainty.
          </p>

          <div className="pt-4 flex flex-col sm:flex-row items-center justify-center gap-3.5">
            <button
              onClick={() => setCurrentPage('AUTH')}
              className="w-full sm:w-auto px-6 py-3 rounded-lg bg-signal text-signal-foreground font-medium text-sm flex items-center justify-center gap-2 hover:bg-signal/90 transition-all shadow-subtle"
            >
              <span>Open Organization Ledger</span>
              <ArrowRight className="h-4 w-4" />
            </button>
            <a
              href="#how-it-works"
              className="w-full sm:w-auto px-6 py-3 rounded-lg border border-border bg-card text-foreground font-medium text-sm hover:bg-secondary transition-all"
            >
              Explore How It Works
            </a>
          </div>
        </div>
      </section>

      {/* Simple 4-Step Conceptual Guide (Zero Jargon) */}
      <section id="how-it-works" className="py-20 border-b border-border bg-secondary/30">
        <div className="max-w-6xl mx-auto px-6 space-y-12">
          <div className="text-center max-w-2xl mx-auto space-y-3">
            <h2 className="text-3xl font-bold font-serif text-foreground">How AuditRail Secures Your Operations</h2>
            <p className="text-sm text-muted-foreground">
              A straightforward process that requires zero modification to your core business databases.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            <div className="bg-card border border-border rounded-xl p-6 shadow-subtle space-y-3">
              <div className="h-8 w-8 rounded-lg bg-secondary flex items-center justify-center font-mono font-bold text-signal text-xs">
                01
              </div>
              <h3 className="font-semibold text-foreground text-sm font-serif">You Perform an Action</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                Your enterprise software executes its normal operations—such as approving a loan, updating a customer profile, or modifying access rights.
              </p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-subtle space-y-3">
              <div className="h-8 w-8 rounded-lg bg-secondary flex items-center justify-center font-mono font-bold text-signal text-xs">
                02
              </div>
              <h3 className="font-semibold text-foreground text-sm font-serif">Instant One-Way Forwarding</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                Your client application silently notifies AuditRail over secure HTTPS using your private organization API key.
              </p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-subtle space-y-3">
              <div className="h-8 w-8 rounded-lg bg-secondary flex items-center justify-center font-mono font-bold text-signal text-xs">
                03
              </div>
              <h3 className="font-semibold text-foreground text-sm font-serif">Cryptographic Sealing</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                AuditRail links the event into your organization's sequential ledger. Once sealed, records cannot be edited, reordered, or deleted by anyone.
              </p>
            </div>

            <div className="bg-card border border-border rounded-xl p-6 shadow-subtle space-y-3">
              <div className="h-8 w-8 rounded-lg bg-secondary flex items-center justify-center font-mono font-bold text-signal text-xs">
                04
              </div>
              <h3 className="font-semibold text-foreground text-sm font-serif">Instant Audit Verification</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                Auditors or compliance leads review the timeline and run one-click mathematical proof checks to guarantee no unauthorized edits have occurred.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Core Business Guarantees */}
      <section id="guarantees" className="py-20 border-b border-border">
        <div className="max-w-6xl mx-auto px-6 space-y-12">
          <div className="text-center max-w-2xl mx-auto space-y-3">
            <h2 className="text-3xl font-bold font-serif text-foreground">Built for Corporate Accountability</h2>
            <p className="text-sm text-muted-foreground">
              Eliminating the single biggest vulnerability in conventional logs: silent database modification.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="p-6 rounded-xl border border-border bg-card shadow-subtle space-y-3">
              <FileCheck2 className="h-6 w-6 text-signal" />
              <h3 className="font-semibold text-foreground text-base font-serif">Tamper-Evident Certainty</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                Traditional logs can be edited by privileged database administrators. In AuditRail, even changing a single character breaks the chain and triggers immediate alarms.
              </p>
            </div>

            <div className="p-6 rounded-xl border border-border bg-card shadow-subtle space-y-3">
              <Layers className="h-6 w-6 text-signal" />
              <h3 className="font-semibold text-foreground text-base font-serif">Complete Tenant Isolation</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                Each company operates inside its own isolated cryptographic boundary. Your confidential operational data never mingles with other organizations.
              </p>
            </div>

            <div className="p-6 rounded-xl border border-border bg-card shadow-subtle space-y-3">
              <Lock className="h-6 w-6 text-signal" />
              <h3 className="font-semibold text-foreground text-base font-serif">Regulatory Ready Exports</h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                Export verifiable CSV and PDF reports containing complete chronological history, organization credentials, and verifiable authenticity certificates.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Client Integration Section */}
      <section id="integration" className="py-20 border-b border-border bg-secondary/30">
        <div className="max-w-4xl mx-auto px-6 space-y-8 text-center">
          <h2 className="text-3xl font-bold font-serif text-foreground">Effortless 3-Step Client Integration</h2>
          <p className="text-sm text-muted-foreground max-w-xl mx-auto">
            AuditRail works with any backend technology (Node.js, Python, Java, Go, PHP) without requiring proprietary plugins.
          </p>

          <div className="text-left bg-card border border-border rounded-xl p-6 shadow-subtle space-y-4">
            <div className="flex items-start gap-3">
              <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-secondary text-signal">1</span>
              <div>
                <strong className="text-xs text-foreground block">Sign Up and Copy Your Secret Key</strong>
                <span className="text-xs text-muted-foreground">Create your organization account and store your private API key in your server's secure configuration.</span>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-secondary text-signal">2</span>
              <div>
                <strong className="text-xs text-foreground block">Forward Sensitive Actions</strong>
                <span className="text-xs text-muted-foreground">Whenever a sensitive transaction or user action occurs, your server sends a standard HTTPS POST with the action and context.</span>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-secondary text-signal">3</span>
              <div>
                <strong className="text-xs text-foreground block">Instant Evidentiary Assurance</strong>
                <span className="text-xs text-muted-foreground">The action is permanently sealed into your organization's ledger for verifiable compliance review.</span>
              </div>
            </div>
          </div>

          <div className="pt-4">
            <button
              onClick={() => setCurrentPage('AUTH')}
              className="px-6 py-3 rounded-lg bg-primary text-primary-foreground font-medium text-xs font-mono inline-flex items-center gap-2 hover:bg-primary/90 transition-all shadow-subtle"
            >
              <span>Get Your Organization API Key</span>
              <ArrowRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-8 bg-background border-t border-border">
        <div className="max-w-7xl mx-auto px-6 flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-muted-foreground">
          <div className="flex items-center gap-2">
            <span className="font-serif font-bold text-foreground">AuditRail</span>
            <span>&bull; Enterprise Tamper-Evident Integrity Platform</span>
          </div>
          <div>
            <span>Strict Multi-Tenant Isolation &bull; Ready for Global Compliance</span>
          </div>
        </div>
      </footer>
    </div>
  );
};
