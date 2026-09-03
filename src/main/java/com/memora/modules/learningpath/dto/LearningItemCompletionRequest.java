package com.memora.modules.learningpath.dto;

import com.memora.modules.memory.domain.MemoryAlgorithmType;

/**
 * Request payload for completing a learning path item.
 * Optionally carries review evaluation parameters if item is of type REVIEW.
 */
public class LearningItemCompletionRequest {

    private Boolean correct;
    private Long responseTimeMs;
    private MemoryAlgorithmType algorithm;
    private String notes;

    public LearningItemCompletionRequest() {
    }

    public LearningItemCompletionRequest(Boolean correct, Long responseTimeMs, MemoryAlgorithmType algorithm, String notes) {
        this.correct = correct;
        this.responseTimeMs = responseTimeMs;
        this.algorithm = algorithm;
        this.notes = notes;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public MemoryAlgorithmType getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(MemoryAlgorithmType algorithm) {
        this.algorithm = algorithm;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
