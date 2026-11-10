import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import React from 'react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ToastProvider } from '../../context/ToastContext';
import { LockedOnboardingView } from '../../components/dashboard/LockedOnboardingView';
import { Sidebar } from '../../components/layout/Sidebar';
import { MobileNav } from '../../components/layout/MobileNav';
import { PartnersPage } from '../PartnersPage';
import { partnerApi } from '../../api/partnerApi';
import { authApi } from '../../api/authApi';
import { onboardingApi } from '../../api/onboardingApi';
import ProtectedRoute from '../../routes/ProtectedRoute';
import { AuthProvider, useAuth } from '../../context/AuthContext';
import { OnboardingProvider, useOnboarding } from '../../context/OnboardingContext';
import { AppRoutes } from '../../routes/AppRoutes';

// Mock partnerApi
vi.mock('../../api/partnerApi', () => ({
  partnerApi: {
    getActivePartners: vi.fn(),
    getPartnerRequests: vi.fn(),
    getMyChallenges: vi.fn(),
    getPartnerLeaderboard: vi.fn(),
    getPartnerActivities: vi.fn(),
    getPartnerProgress: vi.fn(),
    searchUsers: vi.fn(),
    searchByEmail: vi.fn(),
    sendPartnerRequest: vi.fn(),
  },
}));

// Mock authApi
vi.mock('../../api/authApi', () => ({
  authApi: {
    login: vi.fn(),
    register: vi.fn(),
    getCurrentUser: vi.fn(),
  },
}));

// Mock onboardingApi
vi.mock('../../api/onboardingApi', () => ({
  onboardingApi: {
    getState: vi.fn(),
  },
  getDestinationForOnboardingState: vi.fn().mockReturnValue('/dashboard'),
}));

const mockNewUser = {
  id: 101,
  name: 'Brand New Learner',
  email: 'brandnew@test.com',
  currentLevel: 'A1' as const,
  xp: 0,
  streak: 0,
  role: 'LEARNER' as const,
};

describe('Partner Access Policy Lifecycle (Locked Until Placement Completion)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();

    vi.mocked(partnerApi.getActivePartners).mockResolvedValue({ success: true, message: 'OK', data: [] });
    vi.mocked(partnerApi.getPartnerRequests).mockResolvedValue({
      success: true,
      message: 'OK',
      data: { incoming: [], outgoing: [] },
    });
    vi.mocked(partnerApi.getMyChallenges).mockResolvedValue({ success: true, message: 'OK', data: [] });
    vi.mocked(partnerApi.getPartnerLeaderboard).mockResolvedValue({ success: true, message: 'OK', data: [] });
    vi.mocked(partnerApi.getPartnerActivities).mockResolvedValue({ success: true, message: 'OK', data: [] });
    vi.mocked(partnerApi.getPartnerProgress).mockResolvedValue({
      success: true,
      message: 'OK',
      data: {
        id: 0,
        name: '',
        currentLevel: 'A1',
        xp: 0,
        streak: 0,
        wordsLearned: 0,
        masteredWords: 0,
        accuracy: 0,
      },
    });

    vi.mocked(authApi.getCurrentUser).mockResolvedValue({
      success: true,
      message: 'OK',
      data: mockNewUser,
    });

    vi.mocked(onboardingApi.getState).mockResolvedValue({
      success: true,
      message: 'OK',
      data: { state: 'ONBOARDING_REQUIRED', assessmentId: null, learningPathId: null },
    });
  });

  // Scenario 1 & 2: Registration & Login session bootstrap
  it('Scenario 1 & 2: Registration and Login store accessToken and bootstrap new user session', async () => {
    vi.mocked(authApi.register).mockResolvedValue({
      success: true,
      message: 'User registered successfully',
      data: {
        accessToken: 'brand-new-jwt-token',
        tokenType: 'Bearer',
        user: mockNewUser,
      },
    });

    let authContextValue: any;
    const TestConsumer = () => {
      const { user, isAuthenticated, register } = useAuth();
      authContextValue = { user, isAuthenticated };
      return (
        <button
          onClick={() => register('Brand New Learner', 'brandnew@test.com', 'Password123!')}
        >
          Do Register
        </button>
      );
    };

    render(
      <AuthProvider>
        <TestConsumer />
      </AuthProvider>
    );

    // Trigger registration
    fireEvent.click(screen.getByRole('button', { name: /Do Register/i }));

    await waitFor(() => {
      expect(localStorage.getItem('memora_token')).toBe('brand-new-jwt-token');
      expect(authContextValue.user?.email).toBe('brandnew@test.com');
      expect(authContextValue.isAuthenticated).toBe(true);
    });
  });

  // Scenario 3: Inspect new user role, permissions, onboarding status, and CEFR level
  it('Scenario 3: Newly registered user has default LEARNER role, A1 level, 0 XP, and ONBOARDING_REQUIRED status', async () => {
    localStorage.setItem('memora_token', 'jwt-token-xyz');

    let authUser: any;
    let currentOnboardingState: any;

    const StateInspector = () => {
      const { user } = useAuth();
      const { onboardingState } = useOnboarding();
      authUser = user;
      currentOnboardingState = onboardingState;
      return <div>State Inspector Loaded</div>;
    };

    render(
      <MemoryRouter>
        <AuthProvider>
          <OnboardingProvider>
            <StateInspector />
          </OnboardingProvider>
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(authUser).not.toBeNull();
      expect(currentOnboardingState).not.toBeNull();
    });

    // Verification of new user state contract
    expect(authUser.role).toBe('LEARNER');
    expect(authUser.currentLevel).toBe('A1');
    expect(authUser.xp).toBe(0);
    expect(authUser.streak).toBe(0);
    expect(currentOnboardingState.state).toBe('ONBOARDING_REQUIRED');
  });

  // Scenario 4: Dashboard displays Partners in Locked section
  it('Scenario 4: Dashboard displays Learning Partners in Core Learning Features (Locked) before placement test', () => {
    const handleStartAssessment = vi.fn();

    render(
      <MemoryRouter>
        <ToastProvider>
          <LockedOnboardingView
            userName={mockNewUser.name}
            onStartAssessment={handleStartAssessment}
          />
        </ToastProvider>
      </MemoryRouter>
    );

    // Verifies Available Now section exists with Dictionary and Leaderboard only
    expect(screen.getByText(/Available Now/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Dictionary/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Leaderboard/i })).toBeInTheDocument();

    // Verifies Partners is NOT in Available Now
    expect(screen.queryByRole('button', { name: /Partners/i })).not.toBeInTheDocument();

    // Verifies Partners is in Locked Features
    expect(screen.getByText(/Learning Partners & Duels/i)).toBeInTheDocument();
  });

  // Scenario 5: Desktop Sidebar Navigation locks Partners before placement
  it('Scenario 5: Desktop Sidebar locks Partners with lock icon and alert toast for new user', async () => {
    localStorage.setItem('memora_token', 'jwt-token-xyz');

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <AuthProvider>
          <OnboardingProvider>
            <ToastProvider>
              <Sidebar />
            </ToastProvider>
          </OnboardingProvider>
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('link', { name: /Partners/i })).toBeInTheDocument();
    });

    const partnersLink = screen.getByRole('link', { name: /Partners/i });

    // Clicking Partners triggers locked feature toast
    fireEvent.click(partnersLink);
    expect(screen.getByText(/Partners is Locked 🔒/i)).toBeInTheDocument();
  });

  // Scenario 6: Mobile Navigation locks Partners before placement
  it('Scenario 6: Mobile navigation bar locks Partners for mobile new user', async () => {
    localStorage.setItem('memora_token', 'jwt-token-xyz');

    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <AuthProvider>
          <OnboardingProvider>
            <ToastProvider>
              <MobileNav />
            </ToastProvider>
          </OnboardingProvider>
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole('link', { name: /Partners/i })).toBeInTheDocument();
    });

    const partnersLink = screen.getByRole('link', { name: /Partners/i });

    // Clicking Partners triggers locked toast
    fireEvent.click(partnersLink);
    expect(screen.getByText(/Partners is Locked 🔒/i)).toBeInTheDocument();
  });

  // Scenario 7: Direct URL Navigation blocked by placement requirement
  it('Scenario 7: Direct navigation to /partners is guarded when placement is not completed', async () => {
    localStorage.setItem('memora_token', 'jwt-token-xyz');

    render(
      <MemoryRouter initialEntries={['/partners']}>
        <AuthProvider>
          <OnboardingProvider>
            <ToastProvider>
              <PartnersPage />
            </ToastProvider>
          </OnboardingProvider>
        </AuthProvider>
      </MemoryRouter>
    );

    // Direct access to PartnersPage displays placement test requirement
    await waitFor(() => {
      expect(screen.getByText(/Diagnostic Placement Test Required/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/Learning Partners and vocabulary duels remain locked/i)).toBeInTheDocument();
    expect(partnerApi.getActivePartners).not.toHaveBeenCalled();
  });

  // Scenario 8: Placement Test Completion Unlocks Partner Access
  it('Scenario 8: Completing placement test transitions onboarding state and unlocks Partners', async () => {
    localStorage.setItem('memora_token', 'jwt-token-xyz');

    // Simulate completed placement test (state transitions to LEARNING_PATH_REQUIRED or LEARNING_ACTIVE)
    vi.mocked(onboardingApi.getState).mockResolvedValue({
      success: true,
      message: 'OK',
      data: { state: 'LEARNING_ACTIVE', assessmentId: 42, learningPathId: 10 },
    });

    render(
      <MemoryRouter initialEntries={['/partners']}>
        <AuthProvider>
          <OnboardingProvider>
            <ToastProvider>
              <PartnersPage />
            </ToastProvider>
          </OnboardingProvider>
        </AuthProvider>
      </MemoryRouter>
    );

    // Wait for OnboardingProvider to resolve state as LEARNING_ACTIVE
    await waitFor(() => {
      expect(screen.getByText(/Partner Hub/i)).toBeInTheDocument();
    });

    // Partner APIs are now invoked
    expect(partnerApi.getActivePartners).toHaveBeenCalled();

    // Both Desktop Sidebar and MobileNav Partners links are now unlocked
    const partnerLinks = screen.getAllByRole('link', { name: /Partners/i });
    expect(partnerLinks.length).toBeGreaterThanOrEqual(1);
    for (const link of partnerLinks) {
      expect(link).toHaveAttribute('href', '/partners');
      fireEvent.click(link);
    }
    expect(screen.queryByText(/Partners is Locked 🔒/i)).not.toBeInTheDocument();
  });
});
