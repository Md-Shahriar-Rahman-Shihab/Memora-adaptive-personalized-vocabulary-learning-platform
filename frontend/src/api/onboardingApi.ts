import apiClient from './axios';
import { ApiResponse } from '../types/common';
import { OnboardingStateResponse } from '../types/onboarding';

export const onboardingApi = {
  getState: async (): Promise<ApiResponse<OnboardingStateResponse>> => {
    const res = await apiClient.get<ApiResponse<OnboardingStateResponse>>('/onboarding/state');
    return res.data;
  },
};

/**
 * Maps the server-derived onboarding state to the appropriate learner destination.
 */
export function getDestinationForOnboardingState(data: OnboardingStateResponse): string {
  switch (data.state) {
    case 'ASSESSMENT_IN_PROGRESS':
      return data.assessmentId ? `/assessment/${data.assessmentId}` : '/assessment';
    case 'LEARNING_PATH_REQUIRED':
      return data.assessmentId ? `/assessment/result?assessmentId=${data.assessmentId}` : '/assessment/result';
    case 'LEARNING_ACTIVE':
      return '/dashboard';
    case 'ONBOARDING_REQUIRED':
    default:
      return '/dashboard';
  }
}
