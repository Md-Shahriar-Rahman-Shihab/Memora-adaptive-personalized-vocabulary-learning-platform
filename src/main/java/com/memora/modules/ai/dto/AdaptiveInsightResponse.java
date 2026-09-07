package com.memora.modules.ai.dto;

import java.util.List;

/**
 * Payload containing deterministic adaptive learning insights derived strictly
 * from server-side memory metrics, review history, and progression state.
 */
public record AdaptiveInsightResponse(
        InsightItem primaryInsight,
        List<InsightItem> secondaryInsights,
        InsightMetrics metrics,
        String generatedAt
) {
    public record InsightItem(
            String id,
            String type, // REVIEW_PRIORITY, CHALLENGE_READY, SPEED_IMPROVEMENT, REINFORCEMENT, STREAK_MOMENTUM, GENERAL
            String title,
            String message,
            String priority, // HIGH, MEDIUM, LOW
            String actionLabel,
            String actionRoute
    ) {}

    public record InsightMetrics(
            int dueReviewsCount,
            int weakWordsCount,
            int currentStreak,
            int totalXp,
            double retentionRate,
            long avgResponseTimeMs,
            String activeLevel
    ) {}
}
