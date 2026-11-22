import React, { useState } from 'react';
import { 
  Building2, 
  Lock, 
  Mail, 
  Copy, 
  Check, 
  ArrowRight, 
  CheckCircle2, 
  AlertCircle 
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const AuthPage: React.FC = () => {
  const { login, setCurrentPage } = useAuth();
  const [mode, setMode] = useState<'LOGIN' | 'REGISTER'>('LOGIN');

  // Login form
  const [loginEmail, setLoginEmail] = useState('admin@acme-logistics.com');
  const [loginPassword, setLoginPassword] = useState('Admin@AuditRail2026!');
  const [loginError, setLoginError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Register form
  const [orgName, setOrgName] = useState('');
  const [orgSlug, setOrgSlug] = useState('');
  const [adminEmail, setAdminEmail] = useState('');
  const [adminPassword, setAdminPassword] = useState('');
  const [registerError, setRegisterError] = useState<string | null>(null);

  // One-time API key modal state
  const [registeredApiKey, setRegisteredApiKey] = useState<string | null>(null);
  const [registeredOrgData, setRegisteredOrgData] = useState<any | null>(null);
  const [copiedKey, setCopiedKey] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setLoginError(null);

    try {
      const res = await fetch('http://localhost:8080/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: loginEmail, password: loginPassword })
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || 'Authentication failed. Please verify your email and password.');
      }

      const data = await res.json();
      login({
        token: data.token,
        userId: data.userId,
        email: data.email,
        role: data.role,
        organizationId: data.organizationId,
        organizationName: data.organizationName
      });
    } catch (err: any) {
      setLoginError(err.message || 'Connection error. Ensure the AuditRail server is running.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setRegisterError(null);

    try {
      const res = await fetch('http://localhost:8080/api/v1/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          organizationName: orgName,
          organizationSlug: orgSlug || orgName.toLowerCase().replace(/[^a-z0-9]/g, '-'),
          adminEmail,
          adminPassword
        })
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(errData.message || 'Registration failed. Organization slug or email might already exist.');
      }

      const data = await res.json();
      // Store API Key for one-time display modal
      setRegisteredApiKey(data.rawApiKey);
      setRegisteredOrgData(data);
    } catch (err: any) {
      setRegisterError(err.message || 'Failed to register organization.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(true);
    setTimeout(() => setCopiedKey(false), 3000);
  };

  const finishRegistrationAndEnter = () => {
    if (!registeredOrgData) return;
    login({
      token: registeredOrgData.token,
      userId: registeredOrgData.adminUserId,
      email: registeredOrgData.adminEmail,
      role: 'ADMIN',
      organizationId: registeredOrgData.organizationId,
      organizationName: registeredOrgData.organizationName,
      organizationSlug: registeredOrgData.organizationSlug,
      rawApiKey: registeredOrgData.rawApiKey
    });
  };

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col justify-center py-12 sm:px-6 lg:px-8 font-sans selection:bg-signal/15 selection:text-signal">
      {/* Return to Home link */}
      <div className="absolute top-6 left-6">
        <button
          onClick={() => setCurrentPage('LANDING')}
          className="text-xs font-mono text-muted-foreground hover:text-foreground flex items-center gap-1.5 transition-colors"
        >
          &larr; Back to Overview
        </button>
      </div>

      <div className="sm:mx-auto sm:w-full sm:max-w-md text-center">
        <h1 className="text-3xl font-bold font-serif tracking-tight text-foreground">AuditRail</h1>
        <p className="mt-2 text-xs uppercase tracking-widest font-mono text-signal font-semibold">
          Organization Authentication Gateway
        </p>
      </div>

      {/* ONE-TIME API KEY MODAL (If registration succeeded) */}
      {registeredApiKey && (
        <div className="fixed inset-0 bg-foreground/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-card border border-border rounded-xl shadow-float max-w-lg w-full p-8 text-left animate-in fade-in zoom-in-95">
            <div className="flex items-center gap-3 mb-4 text-emerald-600">
              <CheckCircle2 className="h-6 w-6" />
              <h2 className="text-lg font-bold font-serif text-foreground">Organization Registered Successfully</h2>
            </div>
            <p className="text-sm text-muted-foreground mb-4">
              Your organization <strong className="text-foreground">{registeredOrgData?.organizationName}</strong> has been provisioned with dedicated cryptographic isolation.
            </p>
            <div className="bg-amber-500/10 border border-amber-500/30 rounded-lg p-3.5 mb-5 text-xs text-amber-900 dark:text-amber-200">
              <strong>Notice:</strong> This secret ingestion API key is shown <u>only once</u>. Copy and store it immediately in your client application's secure environment file (<code className="font-mono">.env</code>).
            </div>

            <div className="space-y-2 mb-6">
              <label className="text-xs font-mono font-medium text-muted-foreground uppercase">
                Secret Ingestion API Key
              </label>
              <div className="flex items-center gap-2">
                <input
                  type="text"
                  readOnly
                  value={registeredApiKey}
                  className="w-full font-mono text-xs p-3 rounded bg-secondary/80 border border-border text-foreground select-all"
                />
                <button
                  onClick={() => copyToClipboard(registeredApiKey)}
                  className="px-4 py-3 rounded bg-primary text-primary-foreground text-xs font-mono font-medium flex items-center gap-1.5 hover:bg-primary/90 transition-all shrink-0"
                >
                  {copiedKey ? <Check className="h-4 w-4 text-emerald-400" /> : <Copy className="h-4 w-4" />}
                  <span>{copiedKey ? 'Copied' : 'Copy'}</span>
                </button>
              </div>
            </div>

            <button
              onClick={finishRegistrationAndEnter}
              className="w-full py-3 px-4 rounded bg-signal text-signal-foreground font-medium text-sm flex items-center justify-center gap-2 hover:bg-signal/90 transition-all shadow-subtle"
            >
              <span>I have saved my key &mdash; Enter Dashboard</span>
              <ArrowRight className="h-4 w-4" />
            </button>
          </div>
        </div>
      )}

      {/* Main Login / Register Card */}
      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <div className="bg-card py-8 px-6 border border-border shadow-card rounded-xl sm:px-10">
          {/* Mode Switch Tabs */}
          <div className="flex rounded-lg bg-secondary/70 p-1 mb-6 border border-border">
            <button
              type="button"
              onClick={() => { setMode('LOGIN'); setLoginError(null); }}
              className={`flex-1 py-1.5 text-xs font-mono font-medium rounded-md transition-all ${
                mode === 'LOGIN' ? 'bg-card text-foreground shadow-subtle' : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              Organization Sign In
            </button>
            <button
              type="button"
              onClick={() => { setMode('REGISTER'); setRegisterError(null); }}
              className={`flex-1 py-1.5 text-xs font-mono font-medium rounded-md transition-all ${
                mode === 'REGISTER' ? 'bg-card text-foreground shadow-subtle' : 'text-muted-foreground hover:text-foreground'
              }`}
            >
              Register Organization
            </button>
          </div>

          {mode === 'LOGIN' ? (
            <form onSubmit={handleLogin} className="space-y-4">
              {loginError && (
                <div className="p-3 rounded-lg bg-destructive/10 border border-destructive/20 text-destructive text-xs flex items-center gap-2">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{loginError}</span>
                </div>
              )}

              <div>
                <label className="block text-xs font-medium text-foreground mb-1">
                  Corporate Admin Email
                </label>
                <div className="relative">
                  <Mail className="h-4 w-4 absolute left-3 top-2.5 text-muted-foreground" />
                  <input
                    type="email"
                    required
                    value={loginEmail}
                    onChange={(e) => setLoginEmail(e.target.value)}
                    className="w-full pl-9 pr-3 py-2 text-sm bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-1 focus:ring-signal"
                    placeholder="admin@yourcompany.com"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-foreground mb-1">
                  Password
                </label>
                <div className="relative">
                  <Lock className="h-4 w-4 absolute left-3 top-2.5 text-muted-foreground" />
                  <input
                    type="password"
                    required
                    value={loginPassword}
                    onChange={(e) => setLoginPassword(e.target.value)}
                    className="w-full pl-9 pr-3 py-2 text-sm bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-1 focus:ring-signal"
                    placeholder="••••••••••••"
                  />
                </div>
              </div>

              <div className="pt-2">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full py-2.5 px-4 rounded-lg bg-primary text-primary-foreground font-medium text-xs font-mono flex items-center justify-center gap-2 hover:bg-primary/90 transition-all disabled:opacity-50"
                >
                  <span>{isSubmitting ? 'Authenticating...' : 'Sign In to Organization'}</span>
                  <ArrowRight className="h-4 w-4" />
                </button>
              </div>

              <div className="pt-4 border-t border-border text-center">
                <p className="text-xs text-muted-foreground">
                  Pre-configured Demo Accounts:
                </p>
                <div className="mt-2 text-xs font-mono text-muted-foreground space-y-1">
                  <div>Acme Logistics: <code className="text-foreground">admin@acme-logistics.com</code></div>
                  <div>Password: <code className="text-foreground">Admin@AuditRail2026!</code></div>
                </div>
              </div>
            </form>
          ) : (
            <form onSubmit={handleRegister} className="space-y-4">
              {registerError && (
                <div className="p-3 rounded-lg bg-destructive/10 border border-destructive/20 text-destructive text-xs flex items-center gap-2">
                  <AlertCircle className="h-4 w-4 shrink-0" />
                  <span>{registerError}</span>
                </div>
              )}

              <div>
                <label className="block text-xs font-medium text-foreground mb-1">
                  Organization Legal Name
                </label>
                <div className="relative">
                  <Building2 className="h-4 w-4 absolute left-3 top-2.5 text-muted-foreground" />
                  <input
                    type="text"
                    required
                    value={orgName}
                    onChange={(e) => {
                      setOrgName(e.target.value);
                      if (!orgSlug) {
                        setOrgSlug(e.target.value.toLowerCase().replace(/[^a-z0-9]/g, '-'));
                      }
                    }}
                    className="w-full pl-9 pr-3 py-2 text-sm bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-1 focus:ring-signal"
                    placeholder="Apex National Bank"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-foreground mb-1">
                  Organization Identifier (Slug)
                </label>
                <input
                  type="text"
                  required
                  value={orgSlug}
                  onChange={(e) => setOrgSlug(e.target.value.toLowerCase().replace(/[^a-z0-9-]/g, ''))}
                  className="w-full px-3 py-2 text-sm font-mono bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-1 focus:ring-signal"
                  placeholder="apex-bank"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-foreground mb-1">
                  Administrator Email
                </label>
                <div className="relative">
                  <Mail className="h-4 w-4 absolute left-3 top-2.5 text-muted-foreground" />
                  <input
                    type="email"
                    required
                    value={adminEmail}
                    onChange={(e) => setAdminEmail(e.target.value)}
                    className="w-full pl-9 pr-3 py-2 text-sm bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-1 focus:ring-signal"
                    placeholder="admin@apexbank.com"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-medium text-foreground mb-1">
                  Set Admin Password
                </label>
                <div className="relative">
                  <Lock className="h-4 w-4 absolute left-3 top-2.5 text-muted-foreground" />
                  <input
                    type="password"
                    required
                    minLength={8}
                    value={adminPassword}
                    onChange={(e) => setAdminPassword(e.target.value)}
                    className="w-full pl-9 pr-3 py-2 text-sm bg-background border border-border rounded-lg text-foreground focus:outline-none focus:ring-1 focus:ring-signal"
                    placeholder="At least 8 characters"
                  />
                </div>
              </div>

              <div className="pt-2">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="w-full py-2.5 px-4 rounded-lg bg-signal text-signal-foreground font-medium text-xs font-mono flex items-center justify-center gap-2 hover:bg-signal/90 transition-all disabled:opacity-50"
                >
                  <span>{isSubmitting ? 'Provisioning...' : 'Provision Organization & Key'}</span>
                  <ArrowRight className="h-4 w-4" />
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};
