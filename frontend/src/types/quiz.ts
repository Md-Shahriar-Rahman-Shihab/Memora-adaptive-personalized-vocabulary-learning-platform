import { DifficultyLevel, ForgettingRisk } from './vocabulary';

export type QuestionType = 'MULTIPLE_CHOICE' | 'TRANSLATION' | 'FILL_IN_THE_BLANK';

export interface QuestionResponse {
  id: number;
  questionType: QuestionType;
  word?: string;
  questionText: string;
  sentence?: string;
  options?: string[];
  points: number;
}

export interface QuizResponse {
  id: number;
  title: string;
  difficultyLevel: DifficultyLevel;
  questionCount: number;
  questions: QuestionResponse[];
}

export interface AnswerSubmissionRequest {
  questionId?: number;
  answer: string;
  responseTimeMs: number;
}

export interface AnswerResponse {
  correct: boolean;
  score: number;
  feedback: string;
  masteryScore: number;
  forgettingRisk: ForgettingRisk;
  nextReviewAt?: string;
}

export interface QuizResultResponse {
  quizId: number;
  totalQuestions: number;
  correctAnswers: number;
  totalScore: number;
  percentage: number;
}

export interface QuizGenerationRequest {
  targetLevel?: DifficultyLevel;
  questionCount?: number;
}
