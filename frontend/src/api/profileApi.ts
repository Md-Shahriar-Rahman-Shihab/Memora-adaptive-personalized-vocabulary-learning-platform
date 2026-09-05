import apiClient from './axios';
import { ApiResponse } from '../types/common';
import {
  LearnerProfileResponse,
  LearnerStatsResponse,
  UserAchievementResponse,
  XpTransactionResponse,
} from '../types/gamification';

export const profileApi = {
  getProfile: async (): Promise<ApiResponse<LearnerProfileResponse>> => {
    const res = await apiClient.get<ApiResponse<LearnerProfileResponse>>('/profile');
    return res.data;
  },

  getXpHistory: async (page = 0, size = 20): Promise<ApiResponse<XpTransactionResponse[]>> => {
    const res = await apiClient.get<ApiResponse<XpTransactionResponse[]>>('/profile/xp-history', {
      params: { page, size },
    });
    return res.data;
  },

  getAchievements: async (): Promise<ApiResponse<UserAchievementResponse[]>> => {
    const res = await apiClient.get<ApiResponse<UserAchievementResponse[]>>('/profile/achievements');
    return res.data;
  },

  getStats: async (): Promise<ApiResponse<LearnerStatsResponse>> => {
    const res = await apiClient.get<ApiResponse<LearnerStatsResponse>>('/profile/stats');
    return res.data;
  },
};
