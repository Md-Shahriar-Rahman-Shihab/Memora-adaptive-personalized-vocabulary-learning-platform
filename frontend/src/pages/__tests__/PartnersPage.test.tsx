import { describe, it, expect, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { PartnersPage } from '../PartnersPage';
import { ToastProvider } from '../../context/ToastContext';

// Mock AuthContext
vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 1,
      name: 'New Learner',
      email: 'newlearner@test.com',
      currentLevel: 'A1',
      xp: 0,
      streak: 0,
      role: 'LEARNER',
    },
    token: 'mock-jwt-token',
    isAuthenticated: true,
    isLoading: false,
    logout: vi.fn(),
    requestLogout: vi.fn(),
    confirmLogout: vi.fn(),
    cancelLogout: vi.fn(),
    isLogoutModalOpen: false,
    refreshUser: vi.fn(),
  }),
}));

// Mock OnboardingContext
vi.mock('../../context/OnboardingContext', () => ({
  useOnboarding: () => ({
    onboardingState: { state: 'ONBOARDING_REQUIRED', assessmentId: null, learningPathId: null },
    isLoading: false,
    isLearningUnlocked: false,
    refreshOnboardingState: vi.fn(),
  }),
}));

// Mock partnerApi
vi.mock('../../api/partnerApi', () => ({
  partnerApi: {
    getActivePartners: vi.fn().mockResolvedValue({ success: true, data: [] }),
    getPartnerRequests: vi.fn().mockResolvedValue({ success: true, data: { incoming: [], outgoing: [] } }),
    getMyChallenges: vi.fn().mockResolvedValue({ success: true, data: [] }),
    getPartnerLeaderboard: vi.fn().mockResolvedValue({ success: true, data: [] }),
    getPartnerActivities: vi.fn().mockResolvedValue({ success: true, data: [] }),
    getPartnerProgress: vi.fn().mockResolvedValue({ success: true, data: null }),
    searchUsers: vi.fn().mockResolvedValue({ success: true, data: [] }),
    searchByEmail: vi.fn().mockResolvedValue({ success: true, data: null }),
  },
}));

describe('PartnersPage for newly registered user', () => {
  it('renders correctly for a newly registered user with zero partners', async () => {
    render(
      <MemoryRouter initialEntries={['/partners']}>
        <ToastProvider>
          <PartnersPage />
        </ToastProvider>
      </MemoryRouter>
    );

    // Initial loading or loaded state
    await waitFor(() => {
      expect(screen.getByText(/Partner Hub/i)).toBeInTheDocument();
    });

    // Should display empty state message
    expect(screen.getByText(/You don't have a learning partner yet/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Find a Learning Partner/i })).toBeInTheDocument();
  });
});
