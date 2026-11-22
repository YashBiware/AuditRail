import React, { createContext, useContext, useState, useEffect } from 'react';

export interface UserSession {
  token: string;
  userId: string;
  email: string;
  role: string;
  organizationId: string;
  organizationName: string;
  organizationSlug?: string;
  rawApiKey?: string; // Only populated immediately upon signup
}

interface AuthContextType {
  session: UserSession | null;
  login: (sessionData: UserSession) => void;
  logout: () => void;
  currentPage: 'LANDING' | 'AUTH' | 'DASHBOARD';
  setCurrentPage: (page: 'LANDING' | 'AUTH' | 'DASHBOARD') => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [session, setSession] = useState<UserSession | null>(() => {
    const saved = localStorage.getItem('auditrail_session');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        return null;
      }
    }
    return null;
  });

  const [currentPage, setCurrentPage] = useState<'LANDING' | 'AUTH' | 'DASHBOARD'>('LANDING');

  useEffect(() => {
    if (session) {
      localStorage.setItem('auditrail_session', JSON.stringify(session));
    } else {
      localStorage.removeItem('auditrail_session');
    }
  }, [session]);

  const login = (sessionData: UserSession) => {
    setSession(sessionData);
    setCurrentPage('DASHBOARD');
  };

  const logout = () => {
    setSession(null);
    setCurrentPage('LANDING');
  };

  return (
    <AuthContext.Provider value={{ session, login, logout, currentPage, setCurrentPage }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
