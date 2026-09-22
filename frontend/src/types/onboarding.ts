export type OnboardingState =
  | 'ONBOARDING_REQUIRED'
  | 'ASSESSMENT_IN_PROGRESS'
  | 'LEARNING_PATH_REQUIRED'
  | 'LEARNING_ACTIVE';

export interface OnboardingStateResponse {
  state: OnboardingState;
  assessmentId?: number | null;
  learningPathId?: number | null;
}
