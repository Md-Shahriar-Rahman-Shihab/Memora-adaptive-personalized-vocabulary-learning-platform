import apiClient from './axios';
import { ApiResponse } from '../types/common';
import {
  LearningItemCompletionRequest,
  LearningItemCompletionResponse,
  LearningPathItemResponse,
  LearningPathResponse,
  TodayLearningPathResponse,
} from '../types/learningPath';

export const learningPathApi = {
  startPath: async (): Promise<ApiResponse<LearningPathResponse>> => {
    const res = await apiClient.post<ApiResponse<LearningPathResponse>>('/learning-path/start');
    return res.data;
  },

  getCurrentPath: async (): Promise<ApiResponse<LearningPathResponse>> => {
    const res = await apiClient.get<ApiResponse<LearningPathResponse>>('/learning-path/current');
    return res.data;
  },

  getTodayPath: async (): Promise<ApiResponse<TodayLearningPathResponse>> => {
    const res = await apiClient.get<ApiResponse<TodayLearningPathResponse>>('/learning-path/today');
    return res.data;
  },

  startItem: async (itemId: number): Promise<ApiResponse<LearningPathItemResponse>> => {
    const res = await apiClient.post<ApiResponse<LearningPathItemResponse>>(`/learning-path/items/${itemId}/start`);
    return res.data;
  },

  completeItem: async (
    itemId: number,
    request?: LearningItemCompletionRequest
  ): Promise<ApiResponse<LearningItemCompletionResponse>> => {
    const res = await apiClient.post<ApiResponse<LearningItemCompletionResponse>>(
      `/learning-path/items/${itemId}/complete`,
      request || {}
    );
    return res.data;
  },

  regeneratePath: async (): Promise<ApiResponse<LearningPathResponse>> => {
    const res = await apiClient.post<ApiResponse<LearningPathResponse>>('/learning-path/regenerate');
    return res.data;
  },

  getPathHistory: async (): Promise<ApiResponse<LearningPathResponse[]>> => {
    const res = await apiClient.get<ApiResponse<LearningPathResponse[]>>('/learning-path/history');
    return res.data;
  },
};
