import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

const rawBase = import.meta.env.VITE_API_BASE_URL;
const API_BASE_URL = rawBase
  ? (rawBase.endsWith('/api/v1') ? rawBase : `${rawBase.replace(/\/$/, '')}/api/v1`)
  : '/api/v1';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 25000,
});

// Request interceptor attaching JWT token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('memora_token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// Response interceptor handling unauthenticated or server errors
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      const requestUrl = error.config?.url || '';
      const isAuthEndpoint =
        requestUrl.includes('/auth/login') || requestUrl.includes('/auth/register');

      // Do not clear tokens or redirect on login/register failures
      if (!isAuthEndpoint) {
        localStorage.removeItem('memora_token');
        if (typeof window !== 'undefined') {
          const path = window.location.pathname.replace(/\/+$/, '') || '/';
          if (path !== '/login' && path !== '/register' && path !== '') {
            window.location.href = '/login';
          }
        }
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
