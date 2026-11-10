import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { PartnersPage } from '../PartnersPage';
import { ToastProvider } from '../../context/ToastContext';
import { partnerApi } from '../../api/partnerApi';

// Mock AuthContext
vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 1,
      name: 'Test Learner',
      email: 'testlearner@test.com',
      currentLevel: 'B1',
      xp: 100,
      streak: 2,
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

let mockPlacementCompleted = false;
let mockIsLoading = false;

// Mock OnboardingContext
vi.mock('../../context/OnboardingContext', () => ({
  useOnboarding: () => ({
    onboardingState: {
      state: mockPlacementCompleted ? 'LEARNING_ACTIVE' : 'ONBOARDING_REQUIRED',
      assessmentId: null,
      learningPathId: null,
    },
    isLoading: mockIsLoading,
    isLearningUnlocked: mockPlacementCompleted,
    isPlacementCompleted: mockPlacementCompleted,
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

describe('PartnersPage Access Policy', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders locked state when user has not completed placement test', async () => {
    mockPlacementCompleted = false;
    mockIsLoading = false;

    render(
      <MemoryRouter initialEntries={['/partners']}>
        <ToastProvider>
          <PartnersPage />
        </ToastProvider>
      </MemoryRouter>
    );

    expect(screen.getByText(/Diagnostic Placement Test Required/i)).toBeInTheDocument();
    expect(screen.getByText(/Learning Partners and vocabulary duels remain locked/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Go to Placement Assessment/i })).toBeInTheDocument();

    // Verify partner APIs were NOT called when placement is incomplete
    expect(partnerApi.getActivePartners).not.toHaveBeenCalled();
  });

  it('renders Partner Hub when user has completed placement test', async () => {
    mockPlacementCompleted = true;
    mockIsLoading = false;

    render(
      <MemoryRouter initialEntries={['/partners']}>
        <ToastProvider>
          <PartnersPage />
        </ToastProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText(/Partner Hub/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/You don't have a learning partner yet/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Find a Learning Partner/i })).toBeInTheDocument();
    expect(partnerApi.getActivePartners).toHaveBeenCalled();
  });
});
