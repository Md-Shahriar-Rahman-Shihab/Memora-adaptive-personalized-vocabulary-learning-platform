package com.memora.modules.ai.service;

import com.memora.modules.ai.dto.AdaptiveInsightResponse;
import com.memora.modules.ai.dto.AdaptiveInsightResponse.InsightItem;
import com.memora.modules.ai.dto.AdaptiveInsightResponse.InsightMetrics;
import com.memora.modules.learningpath.entity.LearningPath;
import com.memora.modules.learningpath.repository.LearningPathRepository;
import com.memora.modules.memory.dto.MemoryWordResponse;
import com.memora.modules.memory.service.MemoryService;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Deterministic service generating personalized learner insights from verified
 * server-side memory records, quiz history, spaced repetition intervals, and streak consistency.
 *
 * Does not invoke external LLMs — all insights and statistics are 100% accurate and verifiable.
 */
@Service
public class AdaptiveInsightService {

    private static final Logger log = LoggerFactory.getLogger(AdaptiveInsightService.class);

    private final MemoryService memoryService;
    private final UserRepository userRepository;
    private final UserWordProgressRepository progressRepository;
    private final LearningPathRepository learningPathRepository;

    public AdaptiveInsightService(
            MemoryService memoryService,
            UserRepository userRepository,
            UserWordProgressRepository progressRepository,
            LearningPathRepository learningPathRepository) {
        this.memoryService = memoryService;
        this.userRepository = userRepository;
        this.progressRepository = progressRepository;
        this.learningPathRepository = learningPathRepository;
    }

    /**
     * Computes deterministic adaptive insights for the authenticated user.
     *
     * @param userEmail Email of the active user
     * @return {@link AdaptiveInsightResponse}
     */
    public AdaptiveInsightResponse getTodayInsights(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userEmail));

        List<MemoryWordResponse> dueWords = memoryService.getDueReviews(userEmail);
        List<MemoryWordResponse> weakWords = memoryService.getWeakWords(userEmail);
        List<UserWordProgress> progressList = progressRepository.findByUser(user);
        Optional<LearningPath> activePathOpt = learningPathRepository.findActivePathByUserId(user.getId());

        int dueCount = dueWords != null ? dueWords.size() : 0;
        int weakCount = weakWords != null ? weakWords.size() : 0;
        int streak = user.getStreak();
        int xp = user.getXp();
        String level = user.getCurrentLevel() != null ? user.getCurrentLevel().name() : "A1";

        // Compute overall retention rate and avg response time
        int totalAttempts = 0;
        int correctAttempts = 0;
        long sumResponseTime = 0;
        int responseTimeCount = 0;

        for (UserWordProgress p : progressList) {
            totalAttempts += p.getTotalAttempts();
            correctAttempts += p.getCorrectAttempts();
            if (p.getAverageResponseTime() > 0) {
                sumResponseTime += (long) p.getAverageResponseTime();
                responseTimeCount++;
            }
        }

        double retentionRate = totalAttempts > 0
                ? Math.round(((double) correctAttempts / totalAttempts) * 1000.0) / 10.0
                : 0.0;

        long avgResponseTimeMs = responseTimeCount > 0
                ? sumResponseTime / responseTimeCount
                : 0L;

        List<InsightItem> candidates = new ArrayList<>();

        // 1. Spaced Repetition Due Review Rule
        if (dueCount > 0) {
            candidates.add(new InsightItem(
                    "ins-due-reviews",
                    "REVIEW_PRIORITY",
                    "Spaced Repetition Reviews Ready",
                    String.format("You have %d vocabulary word%s scheduled for review right now. Reinforcing before memory decay prevents forgetting.",
                            dueCount, dueCount == 1 ? "" : "s"),
                    "HIGH",
                    "Review " + dueCount + " Words",
                    "/review"
            ));
        }

        // 2. High Forgetting Risk / Weak Words Rule
        if (weakCount > 0) {
            candidates.add(new InsightItem(
                    "ins-weak-words",
                    "REINFORCEMENT",
                    "Forgetting Risk Detected",
                    String.format("%d vocabulary word%s showed lower accuracy or elevated forgetting risk. Strengthening them today builds durable recall.",
                            weakCount, weakCount == 1 ? "" : "s"),
                    weakCount >= 3 ? "HIGH" : "MEDIUM",
                    "Practice Weak Words",
                    "/review"
            ));
        }

        // 3. Challenge Ready Rule (high accuracy + no weak words)
        if (retentionRate >= 80.0 && progressList.size() >= 5 && weakCount == 0) {
            candidates.add(new InsightItem(
                    "ins-challenge-ready",
                    "CHALLENGE_READY",
                    "Ready for Advanced Vocabulary",
                    String.format("Outstanding retention accuracy of %.1f%% across your active vocabulary! You are demonstrating solid mastery and ready for more challenging words.",
                            retentionRate),
                    "MEDIUM",
                    "Continue Learning",
                    "/learn-path"
            ));
        }

        // 4. Response Time / Automaticity Rule
        if (avgResponseTimeMs > 4500) {
            candidates.add(new InsightItem(
                    "ins-speed-coaching",
                    "SPEED_IMPROVEMENT",
                    "Recall Latency Coaching",
                    String.format("Average recall response time is %.1fs. Focus on instantaneous word association in quizzes to build automatic conversational fluency.",
                            avgResponseTimeMs / 1000.0),
                    "LOW",
                    "Take Speed Quiz",
                    "/quiz"
            ));
        }

        // 5. Active Learning Path Rule
        if (activePathOpt.isPresent()) {
            LearningPath path = activePathOpt.get();
            int remaining = Math.max(0, path.getTotalItems() - path.getCompletedItems());
            if (remaining > 0) {
                candidates.add(new InsightItem(
                        "ins-path-progress",
                        "GENERAL",
                        "Today's Curriculum in Progress",
                        String.format("You have completed %d of %d items on Day %d of your personalized learning path.",
                                path.getCompletedItems(), path.getTotalItems(), path.getCurrentDay()),
                        "MEDIUM",
                        "Continue Day " + path.getCurrentDay(),
                        "/learn-path"
                ));
            }
        }

        // 6. Streak Momentum Rule
        if (streak >= 3) {
            candidates.add(new InsightItem(
                    "ins-streak-momentum",
                    "STREAK_MOMENTUM",
                    String.format("%d-Day Consistency Streak", streak),
                    "Consistent daily exposure significantly stabilizes neural recall pathways. Keep up the great momentum!",
                    "LOW",
                    "View Badges",
                    "/achievements"
            ));
        } else if (streak == 0) {
            candidates.add(new InsightItem(
                    "ins-streak-start",
                    "STREAK_MOMENTUM",
                    "Ignite Your Daily Streak",
                    "Even a quick 5-minute vocabulary session today activates your retention streak and earns bonus XP.",
                    "MEDIUM",
                    "Start Session",
                    "/learn-path"
            ));
        }

        // Default general insight if candidates are empty
        if (candidates.isEmpty()) {
            candidates.add(new InsightItem(
                    "ins-welcome",
                    "GENERAL",
                    "Adaptive Memory Engine Active",
                    "Welcome to Memora! Complete your placement assessment or first lesson to receive personalized spaced repetition insights.",
                    "MEDIUM",
                    "Start Learning",
                    "/learn-path"
            ));
        }

        InsightItem primary = candidates.get(0);
        List<InsightItem> secondary = candidates.size() > 1
                ? candidates.subList(1, Math.min(candidates.size(), 4))
                : List.of();

        InsightMetrics metrics = new InsightMetrics(
                dueCount,
                weakCount,
                streak,
                xp,
                retentionRate,
                avgResponseTimeMs,
                level
        );

        return new AdaptiveInsightResponse(
                primary,
                secondary,
                metrics,
                Instant.now().toString()
        );
    }
}
