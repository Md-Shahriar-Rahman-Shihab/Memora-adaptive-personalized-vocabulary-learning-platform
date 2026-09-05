import apiClient from './axios';
import { ApiResponse } from '../types/common';
import {
  AnswerResponse,
  AnswerSubmissionRequest,
  QuizGenerationRequest,
  QuizResponse,
  QuizResultResponse,
} from '../types/quiz';

export const quizApi = {
  generateQuiz: async (request?: QuizGenerationRequest): Promise<ApiResponse<QuizResponse>> => {
    const res = await apiClient.post<ApiResponse<QuizResponse>>('/quizzes/generate', request || {});
    return res.data;
  },

  startQuiz: async (quizId: number): Promise<ApiResponse<QuizResponse>> => {
    const res = await apiClient.post<ApiResponse<QuizResponse>>(`/quizzes/${quizId}/start`);
    return res.data;
  },

  submitAnswer: async (
    quizId: number,
    questionId: number,
    request: AnswerSubmissionRequest
  ): Promise<ApiResponse<AnswerResponse>> => {
    const res = await apiClient.post<ApiResponse<AnswerResponse>>(
      `/quizzes/${quizId}/questions/${questionId}/answer`,
      request
    );
    return res.data;
  },

  completeQuiz: async (quizId: number): Promise<ApiResponse<QuizResultResponse>> => {
    const res = await apiClient.post<ApiResponse<QuizResultResponse>>(`/quizzes/${quizId}/complete`);
    return res.data;
  },

  getQuiz: async (quizId: number): Promise<ApiResponse<QuizResponse>> => {
    const res = await apiClient.get<ApiResponse<QuizResponse>>(`/quizzes/${quizId}`);
    return res.data;
  },

  getQuizResult: async (quizId: number): Promise<ApiResponse<QuizResultResponse>> => {
    const res = await apiClient.get<ApiResponse<QuizResultResponse>>(`/quizzes/${quizId}/result`);
    return res.data;
  },
};
