package com.memora.modules.memory.dto;

import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.vocabulary.domain.ForgettingRisk;

import java.time.Instant;

/**
 * Output payload returned after recording and recalculating memory retention for a vocabulary word.
 */
public class WordReviewResponse {

    private Long wordId;
    private String word;
    private boolean correct;
    private double masteryScore;
    private ForgettingRisk forgettingRisk;
    private Instant nextReviewAt;
    private int reviewIntervalDays;
    private MemoryAlgorithmType algorithm;
    private Integer xpEarned;
    private Integer currentStreak;
    private Integer totalXp;
    private Double previousMasteryScore;
    private ForgettingRisk previousForgettingRisk;

    public WordReviewResponse() {
    }

    public WordReviewResponse(Long wordId,
                              String word,
                              boolean correct,
                              double masteryScore,
                              ForgettingRisk forgettingRisk,
                              Instant nextReviewAt,
                              int reviewIntervalDays,
                              MemoryAlgorithmType algorithm) {
        this(wordId, word, correct, masteryScore, forgettingRisk, nextReviewAt, reviewIntervalDays, algorithm,
                null, null, null, null, null);
    }

    public WordReviewResponse(Long wordId,
                              String word,
                              boolean correct,
                              double masteryScore,
                              ForgettingRisk forgettingRisk,
                              Instant nextReviewAt,
                              int reviewIntervalDays,
                              MemoryAlgorithmType algorithm,
                              Integer xpEarned,
                              Integer currentStreak,
                              Integer totalXp,
                              Double previousMasteryScore,
                              ForgettingRisk previousForgettingRisk) {
        this.wordId = wordId;
        this.word = word;
        this.correct = correct;
        this.masteryScore = masteryScore;
        this.forgettingRisk = forgettingRisk;
        this.nextReviewAt = nextReviewAt;
        this.reviewIntervalDays = reviewIntervalDays;
        this.algorithm = algorithm;
        this.xpEarned = xpEarned;
        this.currentStreak = currentStreak;
        this.totalXp = totalXp;
        this.previousMasteryScore = previousMasteryScore;
        this.previousForgettingRisk = previousForgettingRisk;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public double getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(double masteryScore) {
        this.masteryScore = masteryScore;
    }

    public ForgettingRisk getForgettingRisk() {
        return forgettingRisk;
    }

    public void setForgettingRisk(ForgettingRisk forgettingRisk) {
        this.forgettingRisk = forgettingRisk;
    }

    public Instant getNextReviewAt() {
        return nextReviewAt;
    }

    public void setNextReviewAt(Instant nextReviewAt) {
        this.nextReviewAt = nextReviewAt;
    }

    public int getReviewIntervalDays() {
        return reviewIntervalDays;
    }

    public void setReviewIntervalDays(int reviewIntervalDays) {
        this.reviewIntervalDays = reviewIntervalDays;
    }

    public MemoryAlgorithmType getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(MemoryAlgorithmType algorithm) {
        this.algorithm = algorithm;
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

    public Double getPreviousMasteryScore() {
        return previousMasteryScore;
    }

    public void setPreviousMasteryScore(Double previousMasteryScore) {
        this.previousMasteryScore = previousMasteryScore;
    }

    public ForgettingRisk getPreviousForgettingRisk() {
        return previousForgettingRisk;
    }

    public void setPreviousForgettingRisk(ForgettingRisk previousForgettingRisk) {
        this.previousForgettingRisk = previousForgettingRisk;
    }
}
