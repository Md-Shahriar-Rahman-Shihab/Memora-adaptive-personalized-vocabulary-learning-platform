export type InsightPriority = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW';

export interface InsightItem {
  type: string;
  title: string;
  description: string;
  priority: InsightPriority | string;
  actionLabel: string;
  actionRoute: string;
}

export interface InsightMetrics {
  dueReviewsCount: number;
  weakWordsCount: number;
  currentStreak: number;
  totalXp: number;
  retentionRate: number;
  avgResponseTimeMs: number;
  activeLevel: string;
}

export interface AdaptiveInsightResponse {
  primaryInsight: InsightItem;
  secondaryInsights: InsightItem[];
  metrics: InsightMetrics;
}
