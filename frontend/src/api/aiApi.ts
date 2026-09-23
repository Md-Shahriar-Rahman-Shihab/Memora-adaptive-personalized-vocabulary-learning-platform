import apiClient from './axios';
import { ApiResponse } from '../types/common';
import {
  AiExplanationRequest,
  AiExplanationResponse,
  AiExampleRequest,
  AiExampleResponse,
  AiMemoryTipRequest,
  AiMemoryTipResponse,
  AiUsageRequest,
  AiUsageResponse,
} from '../types/ai';

export const aiApi = {
  getWordExplanation: async (
    request: AiExplanationRequest
  ): Promise<ApiResponse<AiExplanationResponse>> => {
    const res = await apiClient.post<ApiResponse<AiExplanationResponse>>(
      '/ai/word-explanation',
      request,
      { timeout: 25000 }
    );
    return res.data;
  },

  getWordExample: async (
    request: AiExampleRequest
  ): Promise<ApiResponse<AiExampleResponse>> => {
    const res = await apiClient.post<ApiResponse<AiExampleResponse>>(
      '/ai/example',
      request,
      { timeout: 25000 }
    );
    return res.data;
  },

  getMemoryTip: async (
    request: AiMemoryTipRequest
  ): Promise<ApiResponse<AiMemoryTipResponse>> => {
    const res = await apiClient.post<ApiResponse<AiMemoryTipResponse>>(
      '/ai/memory-tip',
      request,
      { timeout: 25000 }
    );
    return res.data;
  },

  getContextualUsage: async (
    request: AiUsageRequest
  ): Promise<ApiResponse<AiUsageResponse>> => {
    const res = await apiClient.post<ApiResponse<AiUsageResponse>>(
      '/ai/contextual-usage',
      request,
      { timeout: 25000 }
    );
    return res.data;
  },
};
