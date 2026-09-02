package com.memora.modules.assessment.dto;

/**
 * Output payload returned immediately upon submitting an assessment answer.
 */
public class AssessmentAnswerResponse {

    private boolean correct;
    private String feedback;
    private Long responseTimeMs;

    public AssessmentAnswerResponse() {
    }

    public AssessmentAnswerResponse(boolean correct, String feedback, Long responseTimeMs) {
        this.correct = correct;
        this.feedback = feedback;
        this.responseTimeMs = responseTimeMs;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
}
