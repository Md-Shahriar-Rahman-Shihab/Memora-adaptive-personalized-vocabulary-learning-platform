import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { MobileNav } from '../MobileNav';
import { ToastProvider } from '../../../context/ToastContext';

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

describe('MobileNav access policy', () => {
  it('locks Partners and Learning features for newly registered user before placement', () => {
    mockPlacementCompleted = false;
    mockLearningUnlocked = false;

    render(
      <MemoryRouter>
        <ToastProvider>
          <MobileNav />
        </ToastProvider>
      </MemoryRouter>
    );

    const partnersLink = screen.getByRole('link', { name: /Partners/i });
    expect(partnersLink).toBeInTheDocument();

    // Clicking Partners triggers locked toast
    fireEvent.click(partnersLink);
    expect(screen.getByText(/Partners is Locked 🔒/i)).toBeInTheDocument();

    // Core learning features locked
    const learnLink = screen.getByRole('link', { name: /Learn/i });
    expect(learnLink).toBeInTheDocument();
    fireEvent.click(learnLink);
    expect(screen.getByText(/Learn is Locked 🔒/i)).toBeInTheDocument();
  });

  it('unlocks Partners when placement test is completed', () => {
    mockPlacementCompleted = true;
    mockLearningUnlocked = true;

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

    fireEvent.click(partnersLink);
    expect(screen.queryByText(/Partners is Locked 🔒/i)).not.toBeInTheDocument();
  });
});
