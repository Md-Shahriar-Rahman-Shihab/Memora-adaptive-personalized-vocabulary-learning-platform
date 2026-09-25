package com.memora.modules.gamification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Output DTO representing an achievement badge earned by a learner.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAchievementResponse {

    private String code;
    private String name;
    private String description;
    private String icon;
    private Instant earnedAt;

    public UserAchievementResponse() {
    }

    public UserAchievementResponse(String code, String name, String description, String icon, Instant earnedAt) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.earnedAt = earnedAt;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Instant getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(Instant earnedAt) {
        this.earnedAt = earnedAt;
    }

    public String getTitle() {
        return name;
    }

    public boolean isUnlocked() {
        return true;
    }

    public Instant getUnlockedAt() {
        return earnedAt;
    }

    public int getXpBonus() {
        if (code == null) return 25;
        return switch (code) {
            case "FIRST_LESSON", "FIRST_QUIZ" -> 25;
            case "WORD_STARTER", "PERFECT_SCORE" -> 50;
            case "STREAK_7" -> 75;
            case "VOCABULARY_EXPLORER", "QUIZ_MASTER" -> 100;
            case "MEMORY_MASTER" -> 150;
            case "CENTURY" -> 200;
            case "STREAK_30" -> 250;
            default -> 25;
        };
    }

    public String getBadgeCategory() {
        if (code == null) return "MILESTONE";
        return switch (code) {
            case "STREAK_7", "STREAK_30" -> "STREAK";
            case "MEMORY_MASTER", "WORD_STARTER" -> "MASTERY";
            case "FIRST_QUIZ", "PERFECT_SCORE", "QUIZ_MASTER" -> "QUIZ";
            case "VOCABULARY_EXPLORER", "CENTURY" -> "EXPLORATION";
            default -> "MILESTONE";
        };
    }
}
