package com.memora.modules.assessment.dto;

import com.memora.modules.assessment.domain.AssessmentStatus;

import java.util.List;

/**
 * Output payload detailing the active status, completion progress, and questions of an assessment session.
 */
public class AssessmentDetailResponse {

    private Long assessmentId;
    private AssessmentStatus status;
    private int totalQuestions;
    private int answeredQuestions;
    private int remainingQuestions;
    private List<AssessmentQuestionResponse> questions;

    public AssessmentDetailResponse() {
    }

    public AssessmentDetailResponse(Long assessmentId, AssessmentStatus status, int totalQuestions,
                                    int answeredQuestions, int remainingQuestions,
                                    List<AssessmentQuestionResponse> questions) {
        this.assessmentId = assessmentId;
        this.status = status;
        this.totalQuestions = totalQuestions;
        this.answeredQuestions = answeredQuestions;
        this.remainingQuestions = remainingQuestions;
        this.questions = questions;
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

    public int getAnsweredQuestions() {
        return answeredQuestions;
    }

    public void setAnsweredQuestions(int answeredQuestions) {
        this.answeredQuestions = answeredQuestions;
    }

    public int getRemainingQuestions() {
        return remainingQuestions;
    }

    public void setRemainingQuestions(int remainingQuestions) {
        this.remainingQuestions = remainingQuestions;
    }

    public List<AssessmentQuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<AssessmentQuestionResponse> questions) {
        this.questions = questions;
    }
}
