import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import React from 'react';
import { MemoryRouter } from 'react-router-dom';
import { LoginPage } from '../LoginPage';

// Mock AuthContext
const mockLogin = vi.fn();
vi.mock('../../context/AuthContext', () => ({
  useAuth: () => ({
    login: mockLogin,
    user: null,
    token: null,
    isAuthenticated: false,
    isLoading: false,
    register: vi.fn(),
    logout: vi.fn(),
    requestLogout: vi.fn(),
    confirmLogout: vi.fn(),
    cancelLogout: vi.fn(),
    isLogoutModalOpen: false,
    refreshUser: vi.fn(),
  }),
}));

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock onboardingApi
vi.mock('../../api/onboardingApi', () => ({
  onboardingApi: {
    getState: vi.fn().mockResolvedValue({ success: false }),
  },
  getDestinationForOnboardingState: vi.fn(),
}));

const renderLoginPage = () => {
  return render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>
  );
};

describe('LoginPage Component UX & Error Handling', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('1. Valid email + valid password submits successfully and navigates to dashboard', async () => {
    mockLogin.mockResolvedValueOnce(undefined);

    renderLoginPage();

    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/^password$/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'learner@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'Password123' } });

    fireEvent.click(submitButton);

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledTimes(1);
      expect(mockLogin).toHaveBeenCalledWith('learner@example.com', 'Password123');
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard', { replace: true });
    });
  });

  it('2. Invalid email (401) displays generic invalid credentials message and preserves entered email', async () => {
    mockLogin.mockRejectedValueOnce({
      response: {
        status: 401,
        data: { message: 'Invalid email or password' },
      },
    });

    renderLoginPage();

    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/^password$/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'nonexistent@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'AnyPassword123' } });

    fireEvent.click(submitButton);

    const errorMessage = await screen.findByRole('alert');
    expect(errorMessage).toBeInTheDocument();
    expect(errorMessage).toHaveTextContent(
      'Invalid email or password. Please check your credentials and try again.'
    );

    // Verify email was preserved and no navigation occurred
    expect(emailInput).toHaveValue('nonexistent@example.com');
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('3. Valid email + incorrect password (401) displays the same generic message', async () => {
    mockLogin.mockRejectedValueOnce({
      response: {
        status: 401,
        data: { message: 'Invalid email or password' },
      },
    });

    renderLoginPage();

    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/^password$/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'validuser@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'WrongPassword' } });

    fireEvent.click(submitButton);

    const errorMessage = await screen.findByRole('alert');
    expect(errorMessage).toHaveTextContent(
      'Invalid email or password. Please check your credentials and try again.'
    );
  });

  it('4. Network failure displays network error message', async () => {
    mockLogin.mockRejectedValueOnce({
      message: 'Network Error',
    });

    renderLoginPage();

    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/^password$/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'user@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'Password123' } });

    fireEvent.click(submitButton);

    const errorMessage = await screen.findByRole('alert');
    expect(errorMessage).toHaveTextContent(
      'Unable to connect. Please check your internet connection and try again.'
    );
  });

  it('5. Backend 500 error displays server error message', async () => {
    mockLogin.mockRejectedValueOnce({
      response: {
        status: 500,
        data: { message: 'Internal Server Error' },
      },
    });

    renderLoginPage();

    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/^password$/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'user@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'Password123' } });

    fireEvent.click(submitButton);

    const errorMessage = await screen.findByRole('alert');
    expect(errorMessage).toHaveTextContent(
      'Something went wrong on our end. Please try again later.'
    );
  });

  it('6. Multiple rapid clicks result in only one login request', async () => {
    let resolveLogin: () => void = () => {};
    mockLogin.mockImplementation(
      () =>
        new Promise<void>((resolve) => {
          resolveLogin = resolve;
        })
    );

    renderLoginPage();

    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/^password$/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'user@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'Password123' } });

    // Click 3 times rapidly
    fireEvent.click(submitButton);
    fireEvent.click(submitButton);
    fireEvent.click(submitButton);

    expect(mockLogin).toHaveBeenCalledTimes(1);

    // Resolve login cleanly
    await act(async () => {
      resolveLogin();
    });
  });

  it('7. Loading indicator and "Signing in..." appear while processing and disappear in finally block', async () => {
    let rejectLogin: (reason: any) => void = () => {};
    mockLogin.mockImplementation(
      () =>
        new Promise<void>((_, reject) => {
          rejectLogin = reject;
        })
    );

    renderLoginPage();

    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/^password$/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'user@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'Password123' } });

    fireEvent.click(submitButton);

    // Button should now show "Signing in..." and be disabled
    expect(screen.getByRole('button')).toHaveTextContent('Signing in...');
    expect(screen.getByRole('button')).toBeDisabled();
    expect(emailInput).toBeDisabled();
    expect(passwordInput).toBeDisabled();

    // Reject login
    await act(async () => {
      rejectLogin({
        response: { status: 401 },
      });
    });

    // Loading should be reset, button label restored to "Sign In", inputs enabled
    await waitFor(() => {
      expect(screen.getByRole('button')).toHaveTextContent('Sign In');
      expect(screen.getByRole('button')).not.toBeDisabled();
      expect(emailInput).not.toBeDisabled();
      expect(passwordInput).not.toBeDisabled();
    });
  });

  it('8. Error disappears when the user modifies email or password to retry', async () => {
    mockLogin.mockRejectedValueOnce({
      response: { status: 401 },
    });

    renderLoginPage();

    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/^password$/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'wrong@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'wrongpass' } });

    fireEvent.click(submitButton);

    const errorMessage = await screen.findByRole('alert');
    expect(errorMessage).toBeInTheDocument();

    // Type in email input to correct it
    fireEvent.change(emailInput, { target: { value: 'correct@example.com' } });

    // Error should be cleared immediately
    expect(screen.queryByRole('alert')).not.toBeInTheDocument();
  });

  it('9. Error message has accessible ARIA attributes and links to inputs', async () => {
    mockLogin.mockRejectedValueOnce({
      response: { status: 401 },
    });

    renderLoginPage();

    const emailInput = screen.getByLabelText(/email address/i);
    const passwordInput = screen.getByLabelText(/^password$/i);
    const submitButton = screen.getByRole('button', { name: /sign in/i });

    fireEvent.change(emailInput, { target: { value: 'wrong@example.com' } });
    fireEvent.change(passwordInput, { target: { value: 'wrongpass' } });

    fireEvent.click(submitButton);

    const errorMessage = await screen.findByRole('alert');
    expect(errorMessage).toHaveAttribute('id', 'login-error-message');
    expect(errorMessage).toHaveAttribute('aria-live', 'polite');
    expect(emailInput).toHaveAttribute('aria-invalid', 'true');
    expect(emailInput).toHaveAttribute('aria-describedby', 'login-error-message');
    expect(passwordInput).toHaveAttribute('aria-invalid', 'true');
    expect(passwordInput).toHaveAttribute('aria-describedby', 'login-error-message');
  });

  it('10. Shows validation error when submitting with empty email or password', async () => {
    renderLoginPage();

    const submitButton = screen.getByRole('button', { name: /sign in/i });
    fireEvent.click(submitButton);

    const errorMessage = await screen.findByRole('alert');
    expect(errorMessage).toHaveTextContent('Please enter both email and password.');
    expect(mockLogin).not.toHaveBeenCalled();
  });
});
