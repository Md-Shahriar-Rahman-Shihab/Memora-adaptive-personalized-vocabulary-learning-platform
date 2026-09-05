import apiClient from './axios';
import { ApiResponse } from '../types/common';
import {
  DifficultyLevel,
  UserWordProgressResponse,
  VocabularyWordResponse,
  WordCategory,
} from '../types/vocabulary';

export const vocabularyApi = {
  getAllVocabulary: async (): Promise<ApiResponse<VocabularyWordResponse[]>> => {
    const res = await apiClient.get<ApiResponse<VocabularyWordResponse[]>>('/vocabulary');
    return res.data;
  },

  getWordById: async (id: number): Promise<ApiResponse<VocabularyWordResponse>> => {
    const res = await apiClient.get<ApiResponse<VocabularyWordResponse>>(`/vocabulary/${id}`);
    return res.data;
  },

  searchVocabulary: async (query: string): Promise<ApiResponse<VocabularyWordResponse[]>> => {
    const res = await apiClient.get<ApiResponse<VocabularyWordResponse[]>>('/vocabulary/search', {
      params: { query },
    });
    return res.data;
  },

  getWordsByLevel: async (level: DifficultyLevel): Promise<ApiResponse<VocabularyWordResponse[]>> => {
    const res = await apiClient.get<ApiResponse<VocabularyWordResponse[]>>(`/vocabulary/level/${level}`);
    return res.data;
  },

  getWordsByCategory: async (category: WordCategory): Promise<ApiResponse<VocabularyWordResponse[]>> => {
    const res = await apiClient.get<ApiResponse<VocabularyWordResponse[]>>(`/vocabulary/category/${category}`);
    return res.data;
  },

  // Progress endpoints
  getUserProgress: async (): Promise<ApiResponse<UserWordProgressResponse[]>> => {
    const res = await apiClient.get<ApiResponse<UserWordProgressResponse[]>>('/progress/words');
    return res.data;
  },

  getWordProgress: async (wordId: number): Promise<ApiResponse<UserWordProgressResponse>> => {
    const res = await apiClient.get<ApiResponse<UserWordProgressResponse>>(`/progress/words/${wordId}`);
    return res.data;
  },

  initWordProgress: async (wordId: number): Promise<ApiResponse<UserWordProgressResponse>> => {
    const res = await apiClient.post<ApiResponse<UserWordProgressResponse>>(`/progress/words/${wordId}/init`);
    return res.data;
  },

  getWeakWords: async (): Promise<ApiResponse<UserWordProgressResponse[]>> => {
    const res = await apiClient.get<ApiResponse<UserWordProgressResponse[]>>('/progress/weak');
    return res.data;
  },

  getDueProgressReviews: async (): Promise<ApiResponse<UserWordProgressResponse[]>> => {
    const res = await apiClient.get<ApiResponse<UserWordProgressResponse[]>>('/progress/review');
    return res.data;
  },
};
