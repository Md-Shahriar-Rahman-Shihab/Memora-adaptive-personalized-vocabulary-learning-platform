package com.memora.modules.gamification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Output DTO returned whenever an activity triggers XP calculation, streak updates,
 * or achievement unlocks.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GamificationActivityResultResponse {

    private int xpEarned;
    private int newTotalXp;
    private int currentStreak;
    private int longestStreak;
    private List<UserAchievementResponse> newAchievements;
    private String message;

    public GamificationActivityResultResponse() {
    }

    public GamificationActivityResultResponse(int xpEarned, int newTotalXp, int currentStreak,
                                              int longestStreak, List<UserAchievementResponse> newAchievements,
                                              String message) {
        this.xpEarned = xpEarned;
        this.newTotalXp = newTotalXp;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.newAchievements = newAchievements;
        this.message = message;
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(int xpEarned) {
        this.xpEarned = xpEarned;
    }

    public int getNewTotalXp() {
        return newTotalXp;
    }

    public void setNewTotalXp(int newTotalXp) {
        this.newTotalXp = newTotalXp;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public List<UserAchievementResponse> getNewAchievements() {
        return newAchievements;
    }

    public void setNewAchievements(List<UserAchievementResponse> newAchievements) {
        this.newAchievements = newAchievements;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
