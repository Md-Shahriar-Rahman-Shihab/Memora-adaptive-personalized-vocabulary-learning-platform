export type DifficultyLevel = 'A1' | 'A2' | 'B1' | 'B2' | 'C1';

export type WordCategory =
  | 'GENERAL'
  | 'ACADEMIC'
  | 'BUSINESS'
  | 'TECHNOLOGY'
  | 'DAILY_LIFE'
  | 'TRAVEL'
  | 'EMOTIONS'
  | 'NATURE'
  | 'SCIENCE'
  | 'ARTS';

export type ForgettingRisk = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface VocabularyWordResponse {
  id: number;
  word: string;
  meaning: string;
  definition: string;
  pronunciation?: string;
  exampleSentence?: string;
  difficultyLevel: DifficultyLevel;
  category: WordCategory;
}

export interface UserWordProgressResponse {
  vocabularyWordId: number;
  word: string;
  totalAttempts: number;
  correctAttempts: number;
  incorrectAttempts: number;
  accuracy: number;
  averageResponseTime: number;
  masteryScore: number;
  forgettingRisk: ForgettingRisk;
  lastReviewedAt?: string;
  nextReviewAt?: string;
}
