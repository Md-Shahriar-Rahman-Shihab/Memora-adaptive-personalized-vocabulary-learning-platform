import apiClient from './axios';
import { ApiResponse } from '../types/common';
import { MemoryWordResponse, WordReviewRequest, WordReviewResponse } from '../types/memory';

export const memoryApi = {
  recordReview: async (request: WordReviewRequest): Promise<ApiResponse<WordReviewResponse>> => {
    const res = await apiClient.post<ApiResponse<WordReviewResponse>>('/memory/review', request);
    return res.data;
  },

  getDueWords: async (): Promise<ApiResponse<MemoryWordResponse[]>> => {
    const res = await apiClient.get<ApiResponse<MemoryWordResponse[]>>('/memory/due');
    return res.data;
  },

  getWeakWords: async (): Promise<ApiResponse<MemoryWordResponse[]>> => {
    const res = await apiClient.get<ApiResponse<MemoryWordResponse[]>>('/memory/weak');
    return res.data;
  },
};
