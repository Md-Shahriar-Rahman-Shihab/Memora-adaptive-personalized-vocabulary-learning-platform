export type BadgeCategory = 'STREAK' | 'MASTERY' | 'QUIZ' | 'EXPLORATION' | 'MILESTONE';

export interface UserAchievementResponse {
  code: string;
  name?: string;
  title?: string;
  description: string;
  icon?: string;
  iconUrl?: string;
  xpBonus?: number;
  badgeCategory?: BadgeCategory;
  unlocked?: boolean;
  earnedAt?: string;
  unlockedAt?: string;
}

export interface Achievement {
  id: number;
  code: string;
  name?: string;
  title?: string;
  description: string;
  icon?: string;
  iconUrl?: string;
  xpBonus?: number;
  badgeCategory?: BadgeCategory;
  active?: boolean;
  orderIndex?: number;
}

export interface LearnerProfileResponse {
  name: string;
  email: string;
  level?: string;
  xp: number;
  currentStreak: number;
  longestStreak: number;
  wordsLearned: number;
  masteredWords: number;
  reviewsCompleted: number;
  quizzesCompleted: number;
  totalCorrectAnswers: number;
  totalAnsweredQuestions: number;
  accuracy: number;
  learningProgress: number;
  rank?: number;
  achievements?: UserAchievementResponse[];
}

export interface LearnerStatsResponse {
  totalXp: number;
  currentStreak: number;
  longestStreak: number;
  totalQuizzes: number;
  totalReviews: number;
  totalLessons: number;
  totalAssessments: number;
  perfectQuizzes: number;
  wordsLearned: number;
  wordsMastered: number;
  wordsReviewDue: number;
  overallAccuracy: number;
}

export interface XpTransactionResponse {
  id: number;
  amount?: number;
  xpEarned?: number;
  resultingTotalXp?: number;
  balanceAfter?: number;
  activityType?: string;
  sourceActivity?: string;
  sourceId?: string;
  referenceId?: string;
  description: string;
  createdAt: string;
}

export interface LeaderboardEntryResponse {
  rank: number;
  userId?: number;
  displayName: string;
  xp?: number;
  totalXp?: number;
  currentStreak: number;
  level?: string;
}

export interface GamificationActivityResultResponse {
  xpEarned: number;
  totalXp: number;
  currentStreak: number;
  streakIncremented: boolean;
  unlockedAchievements: UserAchievementResponse[];
}
