import apiClient from './axios';
import { ApiResponse } from '../types/common';
import { AdaptiveInsightResponse } from '../types/insight';

export const insightApi = {
  getTodayInsights: async (): Promise<ApiResponse<AdaptiveInsightResponse>> => {
    const res = await apiClient.get<ApiResponse<AdaptiveInsightResponse>>('/insights/today');
    return res.data;
  },
};
