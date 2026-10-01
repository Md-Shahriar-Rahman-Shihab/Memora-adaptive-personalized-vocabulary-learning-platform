package com.memora.modules.partner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Privacy-safe learning progress summary of an accepted learning partner.
 * Excludes email, password hash, tokens, AI chat, assessment answer histories, and internal entities.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartnerProgressResponse {

    private Long id;
    private String name;
    private String currentLevel;
    private int xp;
    private int streak;
    private int wordsLearned;
    private int masteredWords;
    private double accuracy;

    public PartnerProgressResponse() {
    }

    public PartnerProgressResponse(Long id, String name, String currentLevel, int xp,
                                   int streak, int wordsLearned, int masteredWords, double accuracy) {
        this.id = id;
        this.name = name;
        this.currentLevel = currentLevel;
        this.xp = xp;
        this.streak = streak;
        this.wordsLearned = wordsLearned;
        this.masteredWords = masteredWords;
        this.accuracy = accuracy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getMasteredWords() {
        return masteredWords;
    }

    public void setMasteredWords(int masteredWords) {
        this.masteredWords = masteredWords;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
}
