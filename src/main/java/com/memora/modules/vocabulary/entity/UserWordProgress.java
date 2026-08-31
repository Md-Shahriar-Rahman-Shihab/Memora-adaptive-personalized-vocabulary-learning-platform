package com.memora.modules.vocabulary.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.user.entity.User;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Domain entity capturing granular learner performance, attempt statistics,
 * and review schedule for a specific vocabulary word.
 *
 * Demonstrates Many-to-One Relationships, Encapsulation, and BaseEntity Inheritance.
 */
@Entity
@Table(name = "user_word_progress",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_word_progress", columnNames = {"user_id", "vocabulary_word_id"})
        },
        indexes = {
                @Index(name = "idx_uwp_user_id", columnList = "user_id"),
                @Index(name = "idx_uwp_word_id", columnList = "vocabulary_word_id"),
                @Index(name = "idx_uwp_next_review", columnList = "next_review_at"),
                @Index(name = "idx_uwp_forgetting_risk", columnList = "forgetting_risk")
        })
public class UserWordProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vocabulary_word_id", nullable = false)
    private VocabularyWord vocabularyWord;

    @Column(name = "total_attempts", nullable = false)
    private int totalAttempts;

    @Column(name = "correct_attempts", nullable = false)
    private int correctAttempts;

    @Column(name = "incorrect_attempts", nullable = false)
    private int incorrectAttempts;

    @Column(name = "average_response_time", nullable = false)
    private double averageResponseTime;

    @Column(name = "consecutive_correct", nullable = false)
    private int consecutiveCorrect;

    @Column(name = "consecutive_incorrect", nullable = false)
    private int consecutiveIncorrect;

    @Column(name = "mastery_score", nullable = false)
    private double masteryScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "forgetting_risk", nullable = false)
    private ForgettingRisk forgettingRisk;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "next_review_at")
    private Instant nextReviewAt;

    @Column(name = "leitner_box", nullable = false)
    private int leitnerBox = 1;

    public UserWordProgress() {
    }

    public UserWordProgress(User user, VocabularyWord vocabularyWord) {
        this.user = user;
        this.vocabularyWord = vocabularyWord;
        this.totalAttempts = 0;
        this.correctAttempts = 0;
        this.incorrectAttempts = 0;
        this.averageResponseTime = 0.0;
        this.consecutiveCorrect = 0;
        this.consecutiveIncorrect = 0;
        this.masteryScore = 0.0;
        this.forgettingRisk = ForgettingRisk.LOW;
        this.lastReviewedAt = null;
        this.nextReviewAt = null;
        this.leitnerBox = 1;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VocabularyWord getVocabularyWord() {
        return vocabularyWord;
    }

    public void setVocabularyWord(VocabularyWord vocabularyWord) {
        this.vocabularyWord = vocabularyWord;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getCorrectAttempts() {
        return correctAttempts;
    }

    public void setCorrectAttempts(int correctAttempts) {
        this.correctAttempts = correctAttempts;
    }

    public int getIncorrectAttempts() {
        return incorrectAttempts;
    }

    public void setIncorrectAttempts(int incorrectAttempts) {
        this.incorrectAttempts = incorrectAttempts;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public int getConsecutiveCorrect() {
        return consecutiveCorrect;
    }

    public void setConsecutiveCorrect(int consecutiveCorrect) {
        this.consecutiveCorrect = consecutiveCorrect;
    }

    public int getConsecutiveIncorrect() {
        return consecutiveIncorrect;
    }

    public void setConsecutiveIncorrect(int consecutiveIncorrect) {
        this.consecutiveIncorrect = consecutiveIncorrect;
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

    public Instant getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(Instant lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public Instant getNextReviewAt() {
        return nextReviewAt;
    }

    public void setNextReviewAt(Instant nextReviewAt) {
        this.nextReviewAt = nextReviewAt;
    }

    public int getLeitnerBox() {
        return leitnerBox;
    }

    public void setLeitnerBox(int leitnerBox) {
        this.leitnerBox = leitnerBox;
    }
}
