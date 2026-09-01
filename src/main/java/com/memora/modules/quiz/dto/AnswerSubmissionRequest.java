package com.memora.modules.quiz.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request payload capturing a learner's answer submission for an individual quiz question.
 */
public class AnswerSubmissionRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "Answer is required")
    private String answer;

    @NotNull(message = "Response time is required")
    @PositiveOrZero(message = "Response time must be greater than or equal to 0 ms")
    private Long responseTimeMs;

    public AnswerSubmissionRequest() {
    }

    public AnswerSubmissionRequest(Long questionId, String answer, Long responseTimeMs) {
        this.questionId = questionId;
        this.answer = answer;
        this.responseTimeMs = responseTimeMs;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
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
