package com.memora.modules.assessment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request payload capturing a learner's answer and response latency for an assessment question.
 */
public class AssessmentAnswerRequest {

    @NotNull(message = "Answer is required")
    private String answer;

    @NotNull(message = "Response time is required")
    @PositiveOrZero(message = "Response time must be greater than or equal to 0 ms")
    private Long responseTimeMs;

    public AssessmentAnswerRequest() {
    }

    public AssessmentAnswerRequest(String answer, Long responseTimeMs) {
        this.answer = answer;
        this.responseTimeMs = responseTimeMs;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }
}
