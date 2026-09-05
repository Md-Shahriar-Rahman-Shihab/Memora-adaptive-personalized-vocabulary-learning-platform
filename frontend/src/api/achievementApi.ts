import apiClient from './axios';
import { ApiResponse } from '../types/common';
import { Achievement } from '../types/gamification';

export const achievementApi = {
  getAllAchievements: async (): Promise<ApiResponse<Achievement[]>> => {
    const res = await apiClient.get<ApiResponse<Achievement[]>>('/achievements');
    return res.data;
  },
};
