package com.memora.modules.gamification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Lightweight statistics summary DTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearnerStatsResponse {

    private int totalXp;
    private int currentStreak;
    private int longestStreak;
    private int wordsLearned;
    private int masteredWords;
    private int quizzesCompleted;
    private int reviewsCompleted;
    private double accuracy;
    private Long rank;

    public LearnerStatsResponse() {
    }

    public LearnerStatsResponse(int totalXp, int currentStreak, int longestStreak,
                                int wordsLearned, int masteredWords, int quizzesCompleted,
                                int reviewsCompleted, double accuracy, Long rank) {
        this.totalXp = totalXp;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.wordsLearned = wordsLearned;
        this.masteredWords = masteredWords;
        this.quizzesCompleted = quizzesCompleted;
        this.reviewsCompleted = reviewsCompleted;
        this.accuracy = accuracy;
        this.rank = rank;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
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

    public int getQuizzesCompleted() {
        return quizzesCompleted;
    }

    public void setQuizzesCompleted(int quizzesCompleted) {
        this.quizzesCompleted = quizzesCompleted;
    }

    public int getReviewsCompleted() {
        return reviewsCompleted;
    }

    public void setReviewsCompleted(int reviewsCompleted) {
        this.reviewsCompleted = reviewsCompleted;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public Long getRank() {
        return rank;
    }

    public void setRank(Long rank) {
        this.rank = rank;
    }
}
