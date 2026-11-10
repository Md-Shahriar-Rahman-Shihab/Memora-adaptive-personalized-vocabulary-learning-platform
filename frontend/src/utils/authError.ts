/**
 * Normalizes backend, network, and HTTP errors into safe, user-friendly authentication error messages.
 * Protects against account enumeration (CWE-204) and internal error leakage.
 */
export const getLoginErrorMessage = (error: unknown): string => {
  if (!error || typeof error !== 'object') {
    return 'An unexpected error occurred. Please try again.';
  }

  const err = error as {
    response?: {
      status?: number;
      data?: {
        message?: string;
        error?: string;
      };
    };
    code?: string;
    message?: string;
  };

  // Network failures, offline state, timeouts, or unreached server
  if (!err.response) {
    if (err.code === 'ECONNABORTED' || err.message?.toLowerCase().includes('timeout')) {
      return 'Request timed out. Please check your internet connection and try again.';
    }
    return 'Unable to connect. Please check your internet connection and try again.';
  }

  const status = err.response.status;

  // Invalid credentials: HTTP 401 Unauthorized
  // Unified generic message prevents account enumeration
  if (status === 401) {
    return 'Invalid email or password. Please check your credentials and try again.';
  }

  // Client validation error / Bad Request: HTTP 400
  if (status === 400) {
    const data = err.response.data as {
      message?: string;
      validationErrors?: Array<{ message?: string }>;
    } | undefined;

    const firstValidationError = data?.validationErrors?.[0]?.message;
    if (firstValidationError && typeof firstValidationError === 'string') {
      return firstValidationError;
    }

    if (data?.message && typeof data.message === 'string' && data.message !== 'Validation failed for one or more fields') {
      return data.message;
    }

    return 'Please check your input and ensure all fields are valid.';
  }

  // Rate limiting / Too Many Requests: HTTP 429
  if (status === 429) {
    return 'Too many login attempts. Please try again later.';
  }

  // Server errors: HTTP 500 - 599
  if (status && status >= 500 && status < 600) {
    return 'Something went wrong on our end. Please try again later.';
  }

  // Fallback for any other unexpected HTTP status
  return 'An unexpected error occurred. Please try again.';
};

