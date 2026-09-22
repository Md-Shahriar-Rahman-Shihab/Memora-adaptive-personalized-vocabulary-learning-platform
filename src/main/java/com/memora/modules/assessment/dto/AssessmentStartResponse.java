package com.memora.modules.assessment.dto;

import com.memora.modules.assessment.domain.AssessmentStatus;

import java.util.List;

/**
 * Output payload returned when a learner initiates a diagnostic placement assessment.
 */
public class AssessmentStartResponse {

    private Long assessmentId;
    private AssessmentStatus status;
    private int totalQuestions;
    private int answeredQuestions = 0;
    private List<AssessmentQuestionResponse> questions;

    public AssessmentStartResponse() {
    }

    public AssessmentStartResponse(Long assessmentId, AssessmentStatus status, int totalQuestions,
                                   List<AssessmentQuestionResponse> questions) {
        this(assessmentId, status, totalQuestions, 0, questions);
    }

    public AssessmentStartResponse(Long assessmentId, AssessmentStatus status, int totalQuestions,
                                   int answeredQuestions, List<AssessmentQuestionResponse> questions) {
        this.assessmentId = assessmentId;
        this.status = status;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.questions = questions;
    }

    public int getAnsweredQuestions() {
        return answeredQuestions;
    }

    public void setAnsweredQuestions(int answeredQuestions) {
        this.answeredQuestions = answeredQuestions;
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public AssessmentStatus getStatus() {
        return status;
    }

    public void setStatus(AssessmentStatus status) {
        this.status = status;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public List<AssessmentQuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<AssessmentQuestionResponse> questions) {
        this.questions = questions;
    }
}
