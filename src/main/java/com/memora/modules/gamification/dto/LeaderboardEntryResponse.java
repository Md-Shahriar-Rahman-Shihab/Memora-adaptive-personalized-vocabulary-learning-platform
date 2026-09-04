package com.memora.modules.gamification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Public, privacy-preserving leaderboard representation.
 * Excludes email, passwords, authentication data, and sensitive internal details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaderboardEntryResponse {

    private long rank;
    private String displayName;
    private int xp;
    private int currentStreak;
    private String level;

    public LeaderboardEntryResponse() {
    }

    public LeaderboardEntryResponse(long rank, String displayName, int xp, int currentStreak, String level) {
        this.rank = rank;
        this.displayName = displayName;
        this.xp = xp;
        this.currentStreak = currentStreak;
        this.level = level;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
