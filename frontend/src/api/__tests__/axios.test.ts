import { describe, it, expect, vi, beforeEach } from 'vitest';
import apiClient from '../axios';
import { AxiosError } from 'axios';

describe('apiClient 401 interceptor behavior', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('does not trigger window.location redirect or clear tokens when /auth/login fails with 401', async () => {
    localStorage.setItem('memora_token', 'temp_token');

    // Simulate calling the interceptor rejected handler directly
    const interceptors = (apiClient.interceptors.response as any).handlers;
    const errorHandler = interceptors[0].rejected;

    const error = {
      response: { status: 401 },
      config: { url: '/auth/login' },
    } as AxiosError;

    await expect(errorHandler(error)).rejects.toEqual(error);

    // memora_token should NOT have been cleared
    expect(localStorage.getItem('memora_token')).toBe('temp_token');
  });

  it('does not trigger window.location redirect when /auth/register fails with 401', async () => {
    const interceptors = (apiClient.interceptors.response as any).handlers;
    const errorHandler = interceptors[0].rejected;

    const error = {
      response: { status: 401 },
      config: { url: '/auth/register' },
    } as AxiosError;

    await expect(errorHandler(error)).rejects.toEqual(error);
  });

  it('clears token and redirects to /login on genuine 401 from protected endpoint when on dashboard', async () => {
    localStorage.setItem('memora_token', 'expired_token');

    // Mock window.location
    const originalLocation = window.location;
    delete (window as any).location;
    window.location = {
      ...originalLocation,
      pathname: '/dashboard',
      href: 'http://localhost/dashboard',
    } as any;

    const interceptors = (apiClient.interceptors.response as any).handlers;
    const errorHandler = interceptors[0].rejected;

    const error = {
      response: { status: 401 },
      config: { url: '/users/me' },
    } as AxiosError;

    await expect(errorHandler(error)).rejects.toEqual(error);

    // Token should be removed
    expect(localStorage.getItem('memora_token')).toBeNull();
    // Redirect should happen
    expect(window.location.href).toBe('/login');

    // Restore window.location
    (window as any).location = originalLocation;
  });

  it('clears token but avoids redirect loop when already on /login or /login/ on genuine 401', async () => {
    localStorage.setItem('memora_token', 'expired_token');

    const originalLocation = window.location;
    delete (window as any).location;
    window.location = {
      ...originalLocation,
      pathname: '/login/',
      href: 'http://localhost/login/',
    } as any;

    const interceptors = (apiClient.interceptors.response as any).handlers;
    const errorHandler = interceptors[0].rejected;

    const error = {
      response: { status: 401 },
      config: { url: '/users/me' },
    } as AxiosError;

    await expect(errorHandler(error)).rejects.toEqual(error);

    expect(localStorage.getItem('memora_token')).toBeNull();
    // href should not have been reassigned to /login
    expect(window.location.href).toBe('http://localhost/login/');

    (window as any).location = originalLocation;
  });
});

