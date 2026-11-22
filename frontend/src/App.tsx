import React from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { LandingPage } from './pages/LandingPage';
import { AuthPage } from './pages/AuthPage';
import { OrgDashboardPage } from './pages/OrgDashboardPage';

const AppContent: React.FC = () => {
  const { currentPage, session } = useAuth();

  // If session is active and user went to dashboard, render dashboard
  if (session && currentPage === 'DASHBOARD') {
    return <OrgDashboardPage />;
  }

  // If user requested auth page (login or register)
  if (currentPage === 'AUTH') {
    return <AuthPage />;
  }

  // Default: Public, non-technical editorial landing page
  return <LandingPage />;
};

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
};
