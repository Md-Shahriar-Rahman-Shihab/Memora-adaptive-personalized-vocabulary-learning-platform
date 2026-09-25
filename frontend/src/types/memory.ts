import { DifficultyLevel, ForgettingRisk, WordCategory } from './vocabulary';

export type MemoryAlgorithmType = 'SM2' | 'LEITNER';

export interface MemoryWordResponse {
  wordId: number;
  word: string;
  meaning: string;
  definition?: string;
  pronunciation?: string;
  exampleSentence?: string;
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
  awardXp?: boolean;
}

export interface WordReviewResponse {
  wordId: number;
  word: string;
  correct: boolean;
  masteryScore: number;
  forgettingRisk: ForgettingRisk;
  reviewIntervalDays: number;
  nextReviewAt: string;
  algorithm: MemoryAlgorithmType;
  xpEarned?: number;
  currentStreak?: number;
  totalXp?: number;
  previousMasteryScore?: number;
  previousForgettingRisk?: ForgettingRisk;
}
