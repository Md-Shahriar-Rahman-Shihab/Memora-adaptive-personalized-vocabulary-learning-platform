package com.memora.modules.assessment.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.assessment.domain.AssessmentStatus;
import com.memora.modules.user.entity.User;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a learner's diagnostic placement assessment session.
 * Stores historical diagnostic performance without overwriting previous sessions.
 */
@Entity
@Table(name = "assessments",
        indexes = {
                @Index(name = "idx_assessment_user", columnList = "user_id"),
                @Index(name = "idx_assessment_status", columnList = "status"),
                @Index(name = "idx_assessment_user_status", columnList = "user_id, status")
        })
public class Assessment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AssessmentStatus status = AssessmentStatus.NOT_STARTED;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "estimated_level")
    private DifficultyLevel estimatedLevel;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "total_questions", nullable = false)
    private int totalQuestions = 0;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers = 0;

    @Column(name = "score", nullable = false)
    private int score = 0;

    @Column(name = "accuracy")
    private Double accuracy;

    @Column(name = "level_performance_json", columnDefinition = "TEXT")
    private String levelPerformanceJson;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<AssessmentQuestion> assessmentQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AssessmentAnswer> assessmentAnswers = new ArrayList<>();

    public Assessment() {
    }

    public Assessment(User user, int totalQuestions) {
        this.user = user;
        this.totalQuestions = totalQuestions;
        this.status = AssessmentStatus.IN_PROGRESS;
        this.startedAt = Instant.now();
        this.correctAnswers = 0;
        this.score = 0;
    }

    public void addAssessmentQuestion(AssessmentQuestion question) {
        if (question != null) {
            assessmentQuestions.add(question);
            question.setAssessment(this);
        }
    }

    public void addAssessmentAnswer(AssessmentAnswer answer) {
        if (answer != null) {
            assessmentAnswers.add(answer);
            answer.setAssessment(this);
            if (answer.isCorrect()) {
                this.correctAnswers++;
                this.score += 10;
            }
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
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

    public DifficultyLevel getEstimatedLevel() {
        return estimatedLevel;
    }

    public void setEstimatedLevel(DifficultyLevel estimatedLevel) {
        this.estimatedLevel = estimatedLevel;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public String getLevelPerformanceJson() {
        return levelPerformanceJson;
    }

    public void setLevelPerformanceJson(String levelPerformanceJson) {
        this.levelPerformanceJson = levelPerformanceJson;
    }

    public List<AssessmentQuestion> getAssessmentQuestions() {
        return assessmentQuestions;
    }

    public void setAssessmentQuestions(List<AssessmentQuestion> assessmentQuestions) {
        this.assessmentQuestions = assessmentQuestions != null ? assessmentQuestions : new ArrayList<>();
    }

    public List<AssessmentAnswer> getAssessmentAnswers() {
        return assessmentAnswers;
    }

    public void setAssessmentAnswers(List<AssessmentAnswer> assessmentAnswers) {
        this.assessmentAnswers = assessmentAnswers != null ? assessmentAnswers : new ArrayList<>();
    }
}
