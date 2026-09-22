package com.memora.modules.onboarding.service;

import com.memora.modules.onboarding.dto.OnboardingStateResponse;

/**
 * Service orchestrating onboarding state detection for authenticated learners.
 */
public interface OnboardingService {

    /**
     * Determines the learner's onboarding state machine status strictly based on their domain records.
     *
     * @param userEmail the authenticated user's email
     * @return current onboarding state response
     */
    OnboardingStateResponse getOnboardingState(String userEmail);
}
