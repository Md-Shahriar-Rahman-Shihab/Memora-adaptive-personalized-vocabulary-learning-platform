package com.memora.modules.onboarding.dto;

import com.memora.modules.onboarding.domain.OnboardingState;

/**
 * Safe DTO exposing the learner's current onboarding status and contextual identifiers.
 */
public class OnboardingStateResponse {

    private OnboardingState state;
    private Long assessmentId;
    private Long learningPathId;

    public OnboardingStateResponse() {
    }

    public OnboardingStateResponse(OnboardingState state, Long assessmentId, Long learningPathId) {
        this.state = state;
        this.assessmentId = assessmentId;
        this.learningPathId = learningPathId;
    }

    public OnboardingState getState() {
        return state;
    }

    public void setState(OnboardingState state) {
        this.state = state;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public Long getLearningPathId() {
        return learningPathId;
    }

    public void setLearningPathId(Long learningPathId) {
        this.learningPathId = learningPathId;
    }
}
