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
        this.wordId = wordId;
        this.word = word;
        this.correct = correct;
        this.masteryScore = masteryScore;
        this.forgettingRisk = forgettingRisk;
        this.nextReviewAt = nextReviewAt;
        this.reviewIntervalDays = reviewIntervalDays;
        this.algorithm = algorithm;
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
}
