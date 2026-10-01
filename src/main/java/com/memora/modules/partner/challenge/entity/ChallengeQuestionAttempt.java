package com.memora.modules.partner.challenge.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.quiz.entity.Question;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Domain entity recording a participant's submitted answer to an individual question in a challenge.
 */
@Entity
@Table(name = "challenge_question_attempts",
        indexes = {
                @Index(name = "idx_ch_qattempt_attempt", columnList = "challenge_attempt_id"),
                @Index(name = "idx_ch_qattempt_question", columnList = "question_id")
        })
public class ChallengeQuestionAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_attempt_id", nullable = false)
    private ChallengeAttempt challengeAttempt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "user_answer", nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "response_time_ms", nullable = false)
    private Long responseTimeMs;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt = Instant.now();

    public ChallengeQuestionAttempt() {
    }

    public ChallengeQuestionAttempt(ChallengeAttempt challengeAttempt,
                                  Question question,
                                  String userAnswer,
                                  boolean correct,
                                  int score,
                                  Long responseTimeMs) {
        this.challengeAttempt = challengeAttempt;
        this.question = question;
        this.userAnswer = userAnswer;
        this.correct = correct;
        this.score = score;
        this.responseTimeMs = responseTimeMs;
        this.answeredAt = Instant.now();
    }

    public ChallengeAttempt getChallengeAttempt() {
        return challengeAttempt;
    }

    public void setChallengeAttempt(ChallengeAttempt challengeAttempt) {
        this.challengeAttempt = challengeAttempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(Instant answeredAt) {
        this.answeredAt = answeredAt;
    }
}
