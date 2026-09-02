package com.memora.modules.assessment.entity;

import com.memora.common.domain.BaseEntity;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Domain entity recording a learner's submitted answer, evaluation result, and response time
 * for an individual AssessmentQuestion.
 */
@Entity
@Table(name = "assessment_answers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_assessment_question_answer", columnNames = {"assessment_id", "assessment_question_id"})
        },
        indexes = {
                @Index(name = "idx_aa_assessment", columnList = "assessment_id"),
                @Index(name = "idx_aa_question", columnList = "assessment_question_id")
        })
public class AssessmentAnswer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_question_id", nullable = false)
    private AssessmentQuestion assessmentQuestion;

    @Column(name = "user_answer", nullable = false, columnDefinition = "TEXT")
    private String userAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean correct;

    @Column(name = "response_time_ms", nullable = false)
    private Long responseTimeMs;

    @Column(name = "answered_at", nullable = false)
    private Instant answeredAt;

    public AssessmentAnswer() {
    }

    public AssessmentAnswer(Assessment assessment, AssessmentQuestion assessmentQuestion,
                            String userAnswer, boolean correct, Long responseTimeMs) {
        this.assessment = assessment;
        this.assessmentQuestion = assessmentQuestion;
        this.userAnswer = userAnswer;
        this.correct = correct;
        this.responseTimeMs = responseTimeMs;
        this.answeredAt = Instant.now();
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public AssessmentQuestion getAssessmentQuestion() {
        return assessmentQuestion;
    }

    public void setAssessmentQuestion(AssessmentQuestion assessmentQuestion) {
        this.assessmentQuestion = assessmentQuestion;
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
