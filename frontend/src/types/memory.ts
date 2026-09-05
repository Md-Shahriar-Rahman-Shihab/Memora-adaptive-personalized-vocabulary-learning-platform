import { DifficultyLevel, ForgettingRisk, WordCategory } from './vocabulary';

export type MemoryAlgorithmType = 'SM2' | 'LEITNER';

export interface MemoryWordResponse {
  wordId: number;
  word: string;
  meaning: string;
  difficultyLevel: DifficultyLevel;
  category: WordCategory;
  masteryScore: number;
  forgettingRisk: ForgettingRisk;
  nextReviewAt?: string;
  lastReviewedAt?: string;
  totalAttempts: number;
  correctAttempts: number;
  incorrectAttempts: number;
  consecutiveCorrect: number;
  averageResponseTime: number;
  leitnerBox: number;
}

export interface WordReviewRequest {
  vocabularyWordId: number;
  correct: boolean;
  responseTimeMs: number;
  algorithm: MemoryAlgorithmType;
}

export interface WordReviewResponse {
  vocabularyWordId: number;
  word: string;
  masteryScore: number;
  forgettingRisk: ForgettingRisk;
  easeFactor: number;
  reviewIntervalDays: number;
  leitnerBox: number;
  nextReviewAt: string;
  lastReviewedAt: string;
}
