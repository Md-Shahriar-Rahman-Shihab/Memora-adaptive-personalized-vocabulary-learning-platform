package com.memora.modules.gamification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Output DTO providing the comprehensive learner dashboard profile and statistics.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearnerProfileResponse {

    private String name;
    private String email;
    private String level;
    private int xp;
    private int currentStreak;
    private int longestStreak;
    private int wordsLearned;
    private int masteredWords;
    private int reviewsCompleted;
    private int quizzesCompleted;
    private int totalCorrectAnswers;
    private int totalAnsweredQuestions;
    private double accuracy;
    private double learningProgress;
    private Long rank;
    private List<UserAchievementResponse> achievements;

    public LearnerProfileResponse() {
    }

    public LearnerProfileResponse(String name, String email, String level, int xp,
                                  int currentStreak, int longestStreak, int wordsLearned,
                                  int masteredWords, int reviewsCompleted, int quizzesCompleted,
                                  int totalCorrectAnswers, int totalAnsweredQuestions,
                                  double accuracy, double learningProgress, Long rank,
                                  List<UserAchievementResponse> achievements) {
        this.name = name;
        this.email = email;
        this.level = level;
        this.xp = xp;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.wordsLearned = wordsLearned;
        this.masteredWords = masteredWords;
        this.reviewsCompleted = reviewsCompleted;
        this.quizzesCompleted = quizzesCompleted;
        this.totalCorrectAnswers = totalCorrectAnswers;
        this.totalAnsweredQuestions = totalAnsweredQuestions;
        this.accuracy = accuracy;
        this.learningProgress = learningProgress;
        this.rank = rank;
        this.achievements = achievements;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
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

    public int getReviewsCompleted() {
        return reviewsCompleted;
    }

    public void setReviewsCompleted(int reviewsCompleted) {
        this.reviewsCompleted = reviewsCompleted;
    }

    public int getQuizzesCompleted() {
        return quizzesCompleted;
    }

    public void setQuizzesCompleted(int quizzesCompleted) {
        this.quizzesCompleted = quizzesCompleted;
    }

    public int getTotalCorrectAnswers() {
        return totalCorrectAnswers;
    }

    public void setTotalCorrectAnswers(int totalCorrectAnswers) {
        this.totalCorrectAnswers = totalCorrectAnswers;
    }

    public int getTotalAnsweredQuestions() {
        return totalAnsweredQuestions;
    }

    public void setTotalAnsweredQuestions(int totalAnsweredQuestions) {
        this.totalAnsweredQuestions = totalAnsweredQuestions;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getLearningProgress() {
        return learningProgress;
    }

    public void setLearningProgress(double learningProgress) {
        this.learningProgress = learningProgress;
    }

    public Long getRank() {
        return rank;
    }

    public void setRank(Long rank) {
        this.rank = rank;
    }

    public List<UserAchievementResponse> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<UserAchievementResponse> achievements) {
        this.achievements = achievements;
    }
}
