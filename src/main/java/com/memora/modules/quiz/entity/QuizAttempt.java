package com.memora.modules.quiz.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a learner's attempt at taking a quiz.
 * Maintains historical performance metrics without overwriting previous sessions.
 */
@Entity
@Table(name = "quiz_attempts",
        indexes = {
                @Index(name = "idx_attempt_user", columnList = "user_id"),
                @Index(name = "idx_attempt_quiz", columnList = "quiz_id")
        })
public class QuizAttempt extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "total_score", nullable = false)
    private int totalScore = 0;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers = 0;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions = 0;

    @OneToMany(mappedBy = "quizAttempt", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<QuestionAttempt> questionAttempts = new ArrayList<>();

    public QuizAttempt() {
    }

    public QuizAttempt(User user, Quiz quiz, int totalQuestions) {
        this.user = user;
        this.quiz = quiz;
        this.totalQuestions = totalQuestions;
        this.startedAt = Instant.now();
        this.totalScore = 0;
        this.correctAnswers = 0;
    }

    public void addQuestionAttempt(QuestionAttempt questionAttempt) {
        if (questionAttempt != null) {
            questionAttempts.add(questionAttempt);
            questionAttempt.setQuizAttempt(this);
            if (questionAttempt.isCorrect()) {
                this.correctAnswers++;
            }
            this.totalScore += questionAttempt.getScore();
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
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

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
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

    public List<QuestionAttempt> getQuestionAttempts() {
        return questionAttempts;
    }

    public void setQuestionAttempts(List<QuestionAttempt> questionAttempts) {
        this.questionAttempts = questionAttempts != null ? questionAttempts : new ArrayList<>();
    }
}
