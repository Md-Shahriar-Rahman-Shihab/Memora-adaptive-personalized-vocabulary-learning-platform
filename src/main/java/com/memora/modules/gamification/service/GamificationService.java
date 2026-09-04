package com.memora.modules.gamification.service;

import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import com.memora.modules.gamification.dto.*;
import com.memora.modules.user.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Central service contract orchestrating gamification mechanics:
 * XP rewards, streak tracking, achievement evaluations, profile aggregation, and leaderboards.
 */
public interface GamificationService {

    /**
     * Records a learner activity, calculates XP, advances daily streak, and awards eligible achievements.
     */
    GamificationActivityResultResponse recordActivity(User user, RewardActivityType activityType, RewardContext context);

    /**
     * Aggregates the learner's complete gamification profile, statistics, and earned badges.
     */
    LearnerProfileResponse getProfile(String userEmail);

    /**
     * Retrieves lightweight learner statistics.
     */
    LearnerStatsResponse getStats(String userEmail);

    /**
     * Retrieves historical XP award transactions.
     */
    List<XpTransactionResponse> getXpHistory(String userEmail, Pageable pageable);

    /**
     * Retrieves all achievements earned by the authenticated learner.
     */
    List<UserAchievementResponse> getAchievements(String userEmail);

    /**
     * Retrieves the top global learners ranked deterministically by XP and streak.
     */
    List<LeaderboardEntryResponse> getLeaderboard(int limit);
}
