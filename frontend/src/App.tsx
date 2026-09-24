import React from 'react';
import { AuthProvider } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import { OnboardingProvider } from './context/OnboardingContext';
import AppRoutes from './routes/AppRoutes';

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <OnboardingProvider>
        <ToastProvider>
          <AppRoutes />
        </ToastProvider>
      </OnboardingProvider>
    </AuthProvider>
  );
};

export default App;
