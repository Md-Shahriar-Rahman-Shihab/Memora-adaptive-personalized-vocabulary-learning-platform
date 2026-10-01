package com.memora.modules.partner.challenge.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity recording an individual participant's attempt at a {@link VocabularyChallenge}.
 * Isolates user attempt state, answers, and timestamps so neither participant mutates shared state.
 */
@Entity
@Table(name = "challenge_attempts",
        indexes = {
                @Index(name = "idx_ch_attempt_challenge", columnList = "challenge_id"),
                @Index(name = "idx_ch_attempt_user", columnList = "user_id")
        })
public class ChallengeAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private VocabularyChallenge challenge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "score", nullable = false)
    private int score = 0;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers = 0;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions = 0;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @OneToMany(mappedBy = "challengeAttempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChallengeQuestionAttempt> questionAttempts = new ArrayList<>();

    public ChallengeAttempt() {
    }

    public ChallengeAttempt(VocabularyChallenge challenge, User user, int totalQuestions) {
        this.challenge = challenge;
        this.user = user;
        this.totalQuestions = totalQuestions;
        this.startedAt = Instant.now();
        this.score = 0;
        this.correctAnswers = 0;
    }

    public void addQuestionAttempt(ChallengeQuestionAttempt questionAttempt) {
        if (questionAttempt != null) {
            questionAttempts.add(questionAttempt);
            questionAttempt.setChallengeAttempt(this);
            if (questionAttempt.isCorrect()) {
                this.correctAnswers++;
            }
            this.score += questionAttempt.getScore();
        }
    }

    public VocabularyChallenge getChallenge() {
        return challenge;
    }

    public void setChallenge(VocabularyChallenge challenge) {
        this.challenge = challenge;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public List<ChallengeQuestionAttempt> getQuestionAttempts() {
        return questionAttempts;
    }

    public void setQuestionAttempts(List<ChallengeQuestionAttempt> questionAttempts) {
        this.questionAttempts = questionAttempts != null ? questionAttempts : new ArrayList<>();
    }
}
