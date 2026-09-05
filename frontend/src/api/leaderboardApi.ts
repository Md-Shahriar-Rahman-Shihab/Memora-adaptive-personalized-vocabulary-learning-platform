import apiClient from './axios';
import { ApiResponse } from '../types/common';
import { LeaderboardEntryResponse } from '../types/gamification';

export const leaderboardApi = {
  getLeaderboard: async (limit = 20): Promise<ApiResponse<LeaderboardEntryResponse[]>> => {
    const res = await apiClient.get<ApiResponse<LeaderboardEntryResponse[]>>('/leaderboard', {
      params: { limit },
    });
    return res.data;
  },
};
