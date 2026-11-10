import { describe, it, expect } from 'vitest';
import { getLoginErrorMessage } from '../authError';

describe('getLoginErrorMessage', () => {
  it('returns generic invalid credentials message on 401 Unauthorized', () => {
    const error = {
      response: {
        status: 401,
        data: { message: 'Invalid email or password' },
      },
    };
    expect(getLoginErrorMessage(error)).toBe(
      'Invalid email or password. Please check your credentials and try again.'
    );
  });

  it('returns specific validation message on 400 Bad Request with validationErrors', () => {
    const error = {
      response: {
        status: 400,
        data: {
          status: 400,
          error: 'Bad Request',
          message: 'Validation failed for one or more fields',
          validationErrors: [
            { field: 'email', message: 'Email must be a valid email address' },
          ],
        },
      },
    };
    expect(getLoginErrorMessage(error)).toBe('Email must be a valid email address');
  });

  it('returns custom message on 400 Bad Request when validationErrors is omitted', () => {
    const error = {
      response: {
        status: 400,
        data: {
          status: 400,
          message: 'Malformed request payload',
        },
      },
    };
    expect(getLoginErrorMessage(error)).toBe('Malformed request payload');
  });

  it('returns fallback input validation message on 400 Bad Request when no specific message is provided', () => {
    const error = {
      response: {
        status: 400,
        data: {},
      },
    };
    expect(getLoginErrorMessage(error)).toBe(
      'Please check your input and ensure all fields are valid.'
    );
  });

  it('returns network error message when there is no response (offline / connection lost)', () => {
    const error = {
      message: 'Network Error',
    };
    expect(getLoginErrorMessage(error)).toBe(
      'Unable to connect. Please check your internet connection and try again.'
    );
  });

  it('returns timeout message on request abort or timeout', () => {
    const error = {
      code: 'ECONNABORTED',
      message: 'timeout of 25000ms exceeded',
    };
    expect(getLoginErrorMessage(error)).toBe(
      'Request timed out. Please check your internet connection and try again.'
    );
  });

  it('returns server error message on HTTP 500', () => {
    const error = {
      response: {
        status: 500,
        data: { message: 'Internal Server Error' },
      },
    };
    expect(getLoginErrorMessage(error)).toBe(
      'Something went wrong on our end. Please try again later.'
    );
  });

  it('returns server error message on HTTP 502 Bad Gateway', () => {
    const error = {
      response: {
        status: 502,
      },
    };
    expect(getLoginErrorMessage(error)).toBe(
      'Something went wrong on our end. Please try again later.'
    );
  });

  it('returns rate limit message on HTTP 429', () => {
    const error = {
      response: {
        status: 429,
        data: { message: 'Too many requests' },
      },
    };
    expect(getLoginErrorMessage(error)).toBe(
      'Too many login attempts. Please try again later.'
    );
  });

  it('returns unexpected error fallback for unknown status codes', () => {
    const error = {
      response: {
        status: 418,
      },
    };
    expect(getLoginErrorMessage(error)).toBe(
      'An unexpected error occurred. Please try again.'
    );
  });

  it('handles null or non-object errors gracefully', () => {
    expect(getLoginErrorMessage(null)).toBe(
      'An unexpected error occurred. Please try again.'
    );
    expect(getLoginErrorMessage('string error')).toBe(
      'An unexpected error occurred. Please try again.'
    );
  });
});

