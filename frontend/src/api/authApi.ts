import apiClient from './axios';
import { ApiResponse } from '../types/common';
import { AuthResponse, LoginRequest, RegistrationRequest, UserResponse } from '../types/auth';

export const authApi = {
  login: async (credentials: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
    const res = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', credentials);
    return res.data;
  },

  register: async (data: RegistrationRequest): Promise<ApiResponse<AuthResponse>> => {
    const res = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', data);
    return res.data;
  },

  getCurrentUser: async (): Promise<ApiResponse<UserResponse>> => {
    const res = await apiClient.get<ApiResponse<UserResponse>>('/users/me');
    return res.data;
  },
};
