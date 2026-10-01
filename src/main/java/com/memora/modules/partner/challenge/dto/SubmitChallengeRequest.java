package com.memora.modules.partner.challenge.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

/**
 * Request payload containing all answered questions when submitting a completed challenge attempt.
 */
public class SubmitChallengeRequest {

    @NotEmpty(message = "Answers list must not be empty")
    @Valid
    private List<ChallengeAnswerRequest> answers = new ArrayList<>();

    public SubmitChallengeRequest() {
    }

    public SubmitChallengeRequest(List<ChallengeAnswerRequest> answers) {
        this.answers = answers != null ? answers : new ArrayList<>();
    }

    public List<ChallengeAnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<ChallengeAnswerRequest> answers) {
        this.answers = answers != null ? answers : new ArrayList<>();
    }
}
