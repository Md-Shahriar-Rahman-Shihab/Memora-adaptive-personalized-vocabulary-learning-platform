import { QuestionType } from './quiz';
import { DifficultyLevel } from './vocabulary';

export type AssessmentStatus = 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED';

export interface AssessmentQuestionResponse {
  questionId: number;
  questionType: QuestionType;
  questionText: string;
  sentence?: string;
  options?: string[];
  difficultyLevel: DifficultyLevel;
}

export interface AssessmentDetailResponse {
  assessmentId: number;
  status: AssessmentStatus;
  totalQuestions: number;
  answeredQuestions: number;
  remainingQuestions: number;
  questions: AssessmentQuestionResponse[];
}

export interface AssessmentStartResponse {
  assessmentId: number;
  totalQuestions: number;
  answeredQuestions?: number;
  questions?: AssessmentQuestionResponse[];
  firstQuestion?: AssessmentQuestionResponse;
}

export interface AssessmentAnswerRequest {
  answer: string;
  responseTimeMs: number;
}

export interface AssessmentAnswerResponse {
  correct: boolean;
  feedback?: string;
  responseTimeMs: number;
}

export interface PlacementResultResponse {
  assessmentId: number;
  estimatedLevel: DifficultyLevel;
  confidenceScore: number;
  totalQuestions: number;
  correctAnswers: number;
  accuracy: number;
  levelPerformance: Record<DifficultyLevel, number>;
}
