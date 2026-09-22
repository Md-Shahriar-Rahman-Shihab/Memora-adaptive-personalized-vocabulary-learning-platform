package com.memora.modules.onboarding.domain;

/**
 * Enumeration representing the learner's onboarding state machine.
 */
public enum OnboardingState {
    /**
     * New authenticated user with no assessment history or active assessment.
     */
    ONBOARDING_REQUIRED,

    /**
     * Diagnostic placement assessment started but not yet completed.
     */
    ASSESSMENT_IN_PROGRESS,

    /**
     * Assessment completed, but personalized learning path not yet created.
     */
    LEARNING_PATH_REQUIRED,

    /**
     * Active learning path exists for the learner.
     */
    LEARNING_ACTIVE
}
