package com.memora.modules.quiz.dto;

/**
 * Output payload summarizing final score, correctness, and percentage for a completed QuizAttempt.
 */
public class QuizResultResponse {

    private Long quizId;
    private int totalQuestions;
    private int correctAnswers;
    private int totalScore;
    private double percentage;
    private Integer xpEarned;
    private Integer currentStreak;
    private Integer totalXp;
    private java.util.List<String> newAchievements;
    private Boolean learningPathCompleted;

    public QuizResultResponse() {
    }

    public QuizResultResponse(Long quizId, int totalQuestions, int correctAnswers, int totalScore, double percentage) {
        this(quizId, totalQuestions, correctAnswers, totalScore, percentage, null, null, null, null, null);
    }

    public QuizResultResponse(Long quizId, int totalQuestions, int correctAnswers, int totalScore, double percentage,
                              Integer xpEarned, Integer currentStreak, Integer totalXp,
                              java.util.List<String> newAchievements, Boolean learningPathCompleted) {
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.totalScore = totalScore;
        this.percentage = percentage;
        this.xpEarned = xpEarned;
        this.currentStreak = currentStreak;
        this.totalXp = totalXp;
        this.newAchievements = newAchievements;
        this.learningPathCompleted = learningPathCompleted;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(Integer totalXp) {
        this.totalXp = totalXp;
    }

    public java.util.List<String> getNewAchievements() {
        return newAchievements;
    }

    public void setNewAchievements(java.util.List<String> newAchievements) {
        this.newAchievements = newAchievements;
    }

    public Boolean getLearningPathCompleted() {
        return learningPathCompleted;
    }

    public void setLearningPathCompleted(Boolean learningPathCompleted) {
        this.learningPathCompleted = learningPathCompleted;
    }
}
