import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { Sidebar } from '../Sidebar';
import { ToastProvider } from '../../../context/ToastContext';

// Mock AuthContext
vi.mock('../../../context/AuthContext', () => ({
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

let mockPlacementCompleted = false;
let mockLearningUnlocked = false;

// Mock OnboardingContext
vi.mock('../../../context/OnboardingContext', () => ({
  useOnboarding: () => ({
    onboardingState: {
      state: mockPlacementCompleted ? 'LEARNING_ACTIVE' : 'ONBOARDING_REQUIRED',
      assessmentId: null,
      learningPathId: null,
    },
    isLoading: false,
    isLearningUnlocked: mockLearningUnlocked,
    isPlacementCompleted: mockPlacementCompleted,
    refreshOnboardingState: vi.fn(),
  }),
}));

describe('Sidebar access policy', () => {
  it('locks Partners and Learning features for newly registered user before placement', () => {
    mockPlacementCompleted = false;
    mockLearningUnlocked = false;

    render(
      <MemoryRouter>
        <ToastProvider>
          <Sidebar />
        </ToastProvider>
      </MemoryRouter>
    );

    const partnersLink = screen.getByRole('link', { name: /Partners/i });
    expect(partnersLink).toBeInTheDocument();

    // Clicking Partners triggers locked notification
    fireEvent.click(partnersLink);
    expect(screen.getByText(/Partners is Locked 🔒/i)).toBeInTheDocument();

    // Learn Path is also locked
    const learnLink = screen.getByRole('link', { name: /Learn Path/i });
    expect(learnLink).toBeInTheDocument();
    fireEvent.click(learnLink);
    expect(screen.getByText(/Learn Path is Locked 🔒/i)).toBeInTheDocument();
  });

  it('unlocks Partners when placement test is completed', () => {
    mockPlacementCompleted = true;
    mockLearningUnlocked = true;

    render(
      <MemoryRouter>
        <ToastProvider>
          <Sidebar />
        </ToastProvider>
      </MemoryRouter>
    );

    const partnersLink = screen.getByRole('link', { name: /Partners/i });
    expect(partnersLink).toBeInTheDocument();
    expect(partnersLink).toHaveAttribute('href', '/partners');

    fireEvent.click(partnersLink);
    expect(screen.queryByText(/Partners is Locked 🔒/i)).not.toBeInTheDocument();
  });
});
