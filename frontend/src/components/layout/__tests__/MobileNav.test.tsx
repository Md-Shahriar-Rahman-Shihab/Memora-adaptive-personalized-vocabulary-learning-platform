import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { MobileNav } from '../MobileNav';
import { ToastProvider } from '../../../context/ToastContext';

// Mock OnboardingContext with isLearningUnlocked: false (newly registered user)
vi.mock('../../../context/OnboardingContext', () => ({
  useOnboarding: () => ({
    onboardingState: { state: 'ONBOARDING_REQUIRED', assessmentId: null, learningPathId: null },
    isLoading: false,
    isLearningUnlocked: false,
    refreshOnboardingState: vi.fn(),
  }),
}));

describe('MobileNav for newly registered user', () => {
  it('includes Partners in navigation items and keeps it unlocked', () => {
    render(
      <MemoryRouter>
        <ToastProvider>
          <MobileNav />
        </ToastProvider>
      </MemoryRouter>
    );

    const partnersLink = screen.getByRole('link', { name: /Partners/i });
    expect(partnersLink).toBeInTheDocument();
    expect(partnersLink).toHaveAttribute('href', '/partners');
  });

  it('keeps core learning features locked with toast alert on mobile', () => {
    render(
      <MemoryRouter>
        <ToastProvider>
          <MobileNav />
        </ToastProvider>
      </MemoryRouter>
    );

    const learnLink = screen.getByRole('link', { name: /Learn/i });
    expect(learnLink).toBeInTheDocument();
    fireEvent.click(learnLink);
    expect(screen.getByText(/Learn is Locked 🔒/i)).toBeInTheDocument();
  });
});

