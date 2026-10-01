import apiClient from './axios';
import { ApiResponse } from '../types/common';
import {
  PartnerUserSummary,
  PartnerRequest,
  PartnerRequestsSummary,
  PartnerProgress,
  PartnerLeaderboardEntry,
  PartnerActivity,
} from '../types/partner';

export const partnerApi = {
  searchUsers: async (query: string): Promise<ApiResponse<PartnerUserSummary[]>> => {
    const res = await apiClient.get<ApiResponse<PartnerUserSummary[]>>('/partners/search', {
      params: { query },
    });
    return res.data;
  },

  searchByEmail: async (email: string): Promise<ApiResponse<PartnerUserSummary>> => {
    const res = await apiClient.get<ApiResponse<PartnerUserSummary>>('/partners/search', {
      params: { email: email.trim() },
    });
    return res.data;
  },

  getPartnerLeaderboard: async (): Promise<ApiResponse<PartnerLeaderboardEntry[]>> => {
    const res = await apiClient.get<ApiResponse<PartnerLeaderboardEntry[]>>('/partners/leaderboard');
    return res.data;
  },

  getPartnerActivities: async (limit: number = 20): Promise<ApiResponse<PartnerActivity[]>> => {
    const res = await apiClient.get<ApiResponse<PartnerActivity[]>>('/partners/activity', {
      params: { limit },
    });
    return res.data;
  },

  getActivePartners: async (): Promise<ApiResponse<PartnerUserSummary[]>> => {
    const res = await apiClient.get<ApiResponse<PartnerUserSummary[]>>('/partners');
    return res.data;
  },

  getPartnerRequests: async (): Promise<ApiResponse<PartnerRequestsSummary>> => {
    const res = await apiClient.get<ApiResponse<PartnerRequestsSummary>>('/partners/requests');
    return res.data;
  },

  sendPartnerRequest: async (targetUserId: number): Promise<ApiResponse<PartnerRequest>> => {
    const res = await apiClient.post<ApiResponse<PartnerRequest>>('/partners/requests', {
      targetUserId,
    });
    return res.data;
  },

  acceptPartnerRequest: async (requestId: number): Promise<ApiResponse<PartnerUserSummary>> => {
    const res = await apiClient.post<ApiResponse<PartnerUserSummary>>(`/partners/requests/${requestId}/accept`);
    return res.data;
  },

  rejectPartnerRequest: async (requestId: number): Promise<ApiResponse<PartnerRequest>> => {
    const res = await apiClient.post<ApiResponse<PartnerRequest>>(`/partners/requests/${requestId}/reject`);
    return res.data;
  },

  cancelPartnerRequest: async (requestId: number): Promise<ApiResponse<PartnerRequest>> => {
    const res = await apiClient.post<ApiResponse<PartnerRequest>>(`/partners/requests/${requestId}/cancel`);
    return res.data;
  },

  getPartnerProgress: async (partnerId: number): Promise<ApiResponse<PartnerProgress>> => {
    const res = await apiClient.get<ApiResponse<PartnerProgress>>(`/partners/${partnerId}/progress`);
    return res.data;
  },

  createChallenge: async (
    partnerId: number,
    cefrLevel?: string,
    questionCount?: number
  ): Promise<ApiResponse<import('../types/partner').ChallengeSummary>> => {
    const res = await apiClient.post<ApiResponse<import('../types/partner').ChallengeSummary>>('/partners/challenges', {
      partnerId,
      cefrLevel,
      questionCount,
    });
    return res.data;
  },

  getMyChallenges: async (
    status?: string
  ): Promise<ApiResponse<import('../types/partner').ChallengeSummary[]>> => {
    const res = await apiClient.get<ApiResponse<import('../types/partner').ChallengeSummary[]>>('/partners/challenges', {
      params: status ? { status } : undefined,
    });
    return res.data;
  },

  getChallenge: async (
    challengeId: number
  ): Promise<ApiResponse<import('../types/partner').ChallengeSummary>> => {
    const res = await apiClient.get<ApiResponse<import('../types/partner').ChallengeSummary>>(`/partners/challenges/${challengeId}`);
    return res.data;
  },

  acceptChallenge: async (
    challengeId: number
  ): Promise<ApiResponse<import('../types/partner').ChallengeSummary>> => {
    const res = await apiClient.post<ApiResponse<import('../types/partner').ChallengeSummary>>(`/partners/challenges/${challengeId}/accept`);
    return res.data;
  },

  declineChallenge: async (
    challengeId: number
  ): Promise<ApiResponse<import('../types/partner').ChallengeSummary>> => {
    const res = await apiClient.post<ApiResponse<import('../types/partner').ChallengeSummary>>(`/partners/challenges/${challengeId}/decline`);
    return res.data;
  },

  cancelChallenge: async (
    challengeId: number
  ): Promise<ApiResponse<import('../types/partner').ChallengeSummary>> => {
    const res = await apiClient.post<ApiResponse<import('../types/partner').ChallengeSummary>>(`/partners/challenges/${challengeId}/cancel`);
    return res.data;
  },

  getChallengeQuestions: async (
    challengeId: number
  ): Promise<ApiResponse<import('../types/partner').ChallengeQuestion[]>> => {
    const res = await apiClient.get<ApiResponse<import('../types/partner').ChallengeQuestion[]>>(`/partners/challenges/${challengeId}/questions`);
    return res.data;
  },

  submitChallenge: async (
    challengeId: number,
    answers: { questionId: number; answer: string; responseTimeMs: number }[]
  ): Promise<ApiResponse<import('../types/partner').ChallengeResult>> => {
    const res = await apiClient.post<ApiResponse<import('../types/partner').ChallengeResult>>(`/partners/challenges/${challengeId}/submit`, {
      answers,
    });
    return res.data;
  },

  getChallengeResult: async (
    challengeId: number
  ): Promise<ApiResponse<import('../types/partner').ChallengeResult>> => {
    const res = await apiClient.get<ApiResponse<import('../types/partner').ChallengeResult>>(`/partners/challenges/${challengeId}/result`);
    return res.data;
  },
};

export default partnerApi;
