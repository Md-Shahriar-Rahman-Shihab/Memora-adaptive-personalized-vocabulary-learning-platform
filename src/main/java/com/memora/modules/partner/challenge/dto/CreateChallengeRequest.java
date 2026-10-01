package com.memora.modules.partner.challenge.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request payload for creating a new vocabulary challenge with an accepted learning partner.
 */
public class CreateChallengeRequest {

    @NotNull(message = "Partner ID is required")
    private Long partnerId;

    private String cefrLevel;

    private Integer questionCount;

    public CreateChallengeRequest() {
    }

    public CreateChallengeRequest(Long partnerId, String cefrLevel, Integer questionCount) {
        this.partnerId = partnerId;
        this.cefrLevel = cefrLevel;
        this.questionCount = questionCount;
    }

    public Long getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(Long partnerId) {
        this.partnerId = partnerId;
    }

    public String getCefrLevel() {
        return cefrLevel;
    }

    public void setCefrLevel(String cefrLevel) {
        this.cefrLevel = cefrLevel;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }
}
