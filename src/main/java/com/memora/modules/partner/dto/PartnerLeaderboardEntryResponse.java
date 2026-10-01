package com.memora.modules.partner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Privacy-safe representation of an entry in the pair-only partner leaderboard.
 * Excludes email, password, authentication data, and sensitive learning records.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartnerLeaderboardEntryResponse {

    private int rank;
    private Long userId;
    private String name;
    private String currentLevel;
    private int xp;
    private int streak;
    private int wordsLearned;

    public PartnerLeaderboardEntryResponse() {
    }

    public PartnerLeaderboardEntryResponse(int rank,
                                           Long userId,
                                           String name,
                                           String currentLevel,
                                           int xp,
                                           int streak,
                                           int wordsLearned) {
        this.rank = rank;
        this.userId = userId;
        this.name = name;
        this.currentLevel = currentLevel;
        this.xp = xp;
        this.streak = streak;
        this.wordsLearned = wordsLearned;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(String currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public int getWordsLearned() {
        return wordsLearned;
    }

    public void setWordsLearned(int wordsLearned) {
        this.wordsLearned = wordsLearned;
    }
}
