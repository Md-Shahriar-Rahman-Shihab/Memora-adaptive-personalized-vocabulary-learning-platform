import apiClient from './axios';
import { ApiResponse } from '../types/common';
import {
  AssessmentAnswerRequest,
  AssessmentAnswerResponse,
  AssessmentDetailResponse,
  AssessmentStartResponse,
  PlacementResultResponse,
} from '../types/assessment';

export const assessmentApi = {
  startAssessment: async (): Promise<ApiResponse<AssessmentStartResponse>> => {
    const res = await apiClient.post<ApiResponse<AssessmentStartResponse>>('/assessments/start');
    return res.data;
  },

  getAssessment: async (assessmentId: number): Promise<ApiResponse<AssessmentDetailResponse>> => {
    const res = await apiClient.get<ApiResponse<AssessmentDetailResponse>>(`/assessments/${assessmentId}`);
    return res.data;
  },

  submitAnswer: async (
    assessmentId: number,
    questionId: number,
    request: AssessmentAnswerRequest
  ): Promise<ApiResponse<AssessmentAnswerResponse>> => {
    const res = await apiClient.post<ApiResponse<AssessmentAnswerResponse>>(
      `/assessments/${assessmentId}/questions/${questionId}/answer`,
      request
    );
    return res.data;
  },

  completeAssessment: async (assessmentId: number): Promise<ApiResponse<PlacementResultResponse>> => {
    const res = await apiClient.post<ApiResponse<PlacementResultResponse>>(
      `/assessments/${assessmentId}/complete`
    );
    return res.data;
  },

  getResult: async (assessmentId: number): Promise<ApiResponse<PlacementResultResponse>> => {
    const res = await apiClient.get<ApiResponse<PlacementResultResponse>>(
      `/assessments/${assessmentId}/result`
    );
    return res.data;
  },

  getHistory: async (): Promise<ApiResponse<PlacementResultResponse[]>> => {
    const res = await apiClient.get<ApiResponse<PlacementResultResponse[]>>('/assessments/history');
    return res.data;
  },
};
