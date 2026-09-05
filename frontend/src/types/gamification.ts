export type BadgeCategory = 'STREAK' | 'MASTERY' | 'QUIZ' | 'EXPLORATION' | 'MILESTONE';

export interface UserAchievementResponse {
  code: string;
  title: string;
  description: string;
  iconUrl?: string;
  xpBonus: number;
  badgeCategory: BadgeCategory;
  unlocked: boolean;
  unlockedAt?: string;
}

export interface Achievement {
  id: number;
  code: string;
  title: string;
  description: string;
  iconUrl?: string;
  xpBonus: number;
  badgeCategory: BadgeCategory;
  orderIndex: number;
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
  xpEarned: number;
  resultingTotalXp: number;
  sourceActivity: string;
  sourceId?: string;
  description: string;
  createdAt: string;
}

export interface LeaderboardEntryResponse {
  rank: number;
  userId: number;
  displayName: string;
  totalXp: number;
  currentStreak: number;
}

export interface GamificationActivityResultResponse {
  xpEarned: number;
  totalXp: number;
  currentStreak: number;
  streakIncremented: boolean;
  unlockedAchievements: UserAchievementResponse[];
}
