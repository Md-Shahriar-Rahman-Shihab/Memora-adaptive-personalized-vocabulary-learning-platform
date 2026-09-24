import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { OnboardingStateResponse } from '../types/onboarding';
import { onboardingApi } from '../api/onboardingApi';
import { useAuth } from './AuthContext';

export interface OnboardingContextType {
  onboardingState: OnboardingStateResponse | null;
  isLoading: boolean;
  isLearningUnlocked: boolean;
  refreshOnboardingState: () => Promise<OnboardingStateResponse | null>;
}

const OnboardingContext = createContext<OnboardingContextType | undefined>(undefined);

export const OnboardingProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const [onboardingState, setOnboardingState] = useState<OnboardingStateResponse | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const fetchState = useCallback(async (): Promise<OnboardingStateResponse | null> => {
    try {
      const res = await onboardingApi.getState();
      if (res.success && res.data) {
        setOnboardingState(res.data);
        return res.data;
      }
      return null;
    } catch {
      return null;
    }
  }, []);

  const refreshOnboardingState = useCallback(async (): Promise<OnboardingStateResponse | null> => {
    setIsLoading(true);
    try {
      return await fetchState();
    } finally {
      setIsLoading(false);
    }
  }, [fetchState]);

  useEffect(() => {
    if (authLoading) return;

    if (!isAuthenticated) {
      setOnboardingState(null);
      setIsLoading(false);
      return;
    }

    let isMounted = true;
    setIsLoading(true);

    fetchState().then(() => {
      if (isMounted) {
        setIsLoading(false);
      }
    });

    return () => {
      isMounted = false;
    };
  }, [isAuthenticated, authLoading, fetchState]);

  const isLearningUnlocked = onboardingState?.state === 'LEARNING_ACTIVE';

  return (
    <OnboardingContext.Provider
      value={{
        onboardingState,
        isLoading: authLoading || isLoading,
        isLearningUnlocked,
        refreshOnboardingState,
      }}
    >
      {children}
    </OnboardingContext.Provider>
  );
};

export const useOnboarding = (): OnboardingContextType => {
  const context = useContext(OnboardingContext);
  if (!context) {
    throw new Error('useOnboarding must be used within an OnboardingProvider');
  }
  return context;
};

export default OnboardingContext;
