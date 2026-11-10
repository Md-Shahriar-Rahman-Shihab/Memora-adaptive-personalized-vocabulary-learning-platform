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

// Mock OnboardingContext with isLearningUnlocked: false (new user)
vi.mock('../../../context/OnboardingContext', () => ({
  useOnboarding: () => ({
    onboardingState: { state: 'ONBOARDING_REQUIRED', assessmentId: null, learningPathId: null },
    isLoading: false,
    isLearningUnlocked: false,
    refreshOnboardingState: vi.fn(),
  }),
}));

describe('Sidebar for newly registered user', () => {
  it('renders Partners navigation link with correct href and unlocked status', () => {
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

    // It should not be locked with #
    expect(partnersLink.getAttribute('href')).not.toBe('#');
  });

  it('locks learning path and review for newly registered user', () => {
    render(
      <MemoryRouter>
        <ToastProvider>
          <Sidebar />
        </ToastProvider>
      </MemoryRouter>
    );

    const learnLink = screen.getByRole('link', { name: /Learn Path/i });
    expect(learnLink).toBeInTheDocument();
    fireEvent.click(learnLink);
    expect(screen.getByText(/Learn Path is Locked 🔒/i)).toBeInTheDocument();
  });
});
