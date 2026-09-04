package com.memora.modules.gamification.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable value object holding contextual metadata required by {@link com.memora.modules.gamification.strategy.RewardStrategy}
 * implementations to calculate XP points accurately.
 */
public class RewardContext {

    private final Boolean isCorrect;
    private final Boolean isPerfect;
    private final Integer streakCount;
    private final Integer score;
    private final String referenceId;
    private final Map<String, Object> metadata;

    public RewardContext(Boolean isCorrect, Boolean isPerfect, Integer streakCount,
                         Integer score, String referenceId, Map<String, Object> metadata) {
        this.isCorrect = isCorrect;
        this.isPerfect = isPerfect;
        this.streakCount = streakCount;
        this.score = score;
        this.referenceId = referenceId;
        this.metadata = metadata != null ? Collections.unmodifiableMap(new HashMap<>(metadata)) : Collections.emptyMap();
    }

    public static RewardContext forQuizAnswer(Long questionAttemptId, boolean correct) {
        return new RewardContext(correct, false, null, null, "qa-" + questionAttemptId, null);
    }

    public static RewardContext forQuizCompletion(Long quizId, int correct, int total, boolean isPerfect) {
        Map<String, Object> meta = Map.of("correct", correct, "total", total);
        return new RewardContext(true, isPerfect, null, correct, "quiz-" + quizId, meta);
    }

    public static RewardContext forLesson(Long itemId) {
        return new RewardContext(true, false, null, null, "lesson-" + itemId, null);
    }

    public static RewardContext forReview(Long itemId) {
        return new RewardContext(true, false, null, null, "review-" + itemId, null);
    }

    public static RewardContext forDailyPath(Long pathId) {
        return new RewardContext(true, false, null, null, "path-" + pathId, null);
    }

    public static RewardContext forStreak(int streakCount) {
        return new RewardContext(true, false, streakCount, null, "streak-" + streakCount, null);
    }

    public static RewardContext forAssessment(Long assessmentId) {
        return new RewardContext(true, false, null, null, "assessment-" + assessmentId, null);
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Boolean getIsPerfect() {
        return isPerfect;
    }

    public Integer getStreakCount() {
        return streakCount;
    }

    public Integer getScore() {
        return score;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }
}
