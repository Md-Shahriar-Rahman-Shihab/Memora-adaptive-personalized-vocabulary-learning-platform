import { DifficultyLevel } from './vocabulary';

export type LearningItemType = 'NEW_WORD' | 'REVIEW' | 'QUIZ';
export type LearningItemPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type LearningItemStatus = 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'SKIPPED';
export type LearningPathStatus = 'ACTIVE' | 'PAUSED' | 'COMPLETED';

export interface LearningPathItemResponse {
  id: number;
  type: LearningItemType;
  wordId?: number;
  word?: string;
  meaning?: string;
  definition?: string;
  pronunciation?: string;
  exampleSentence?: string;
  quizId?: number;
  priority: LearningItemPriority;
  status: LearningItemStatus;
  orderIndex: number;
  notes?: string;
  scheduledAt?: string;
  completedAt?: string;
}

export interface TodayLearningPathResponse {
  date: string;
  learningPathId: number;
  targetLevel: DifficultyLevel;
  currentDay: number;
  totalItems: number;
  completedItems: number;
  items: LearningPathItemResponse[];
}

export interface LearningPathResponse {
  id: number;
  status: LearningPathStatus;
  targetLevel: DifficultyLevel;
  currentDay: number;
  totalItems: number;
  completedItems: number;
  items: LearningPathItemResponse[];
}

export interface LearningItemCompletionRequest {
  correct?: boolean;
  responseTimeMs?: number;
  algorithm?: 'SM2' | 'LEITNER';
}

export interface LearningItemCompletionResponse {
  itemId: number;
  status: LearningItemStatus;
  itemType: LearningItemType;
  completedAt: string;
  pathCompleted: boolean;
  completedItems: number;
  totalItems: number;
  message: string;
  xpEarned?: number;
}
