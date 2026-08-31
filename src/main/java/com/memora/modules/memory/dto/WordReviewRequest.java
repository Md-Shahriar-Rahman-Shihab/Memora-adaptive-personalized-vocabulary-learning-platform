package com.memora.modules.memory.dto;

import com.memora.modules.memory.domain.MemoryAlgorithmType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request payload capturing a single word review response submitted by a learner.
 */
public class WordReviewRequest {

    @NotNull(message = "Vocabulary word ID is required")
    @Positive(message = "Vocabulary word ID must be a positive integer")
    private Long vocabularyWordId;

    @NotNull(message = "Correct flag is required")
    private Boolean correct;

    @NotNull(message = "Response time is required")
    @Positive(message = "Response time must be greater than 0 ms")
    private Long responseTimeMs;

    @NotNull(message = "Algorithm type is required")
    private MemoryAlgorithmType algorithm;

    public WordReviewRequest() {
    }

    public WordReviewRequest(Long vocabularyWordId, Boolean correct, Long responseTimeMs, MemoryAlgorithmType algorithm) {
        this.vocabularyWordId = vocabularyWordId;
        this.correct = correct;
        this.responseTimeMs = responseTimeMs;
        this.algorithm = algorithm;
    }

    public Long getVocabularyWordId() {
        return vocabularyWordId;
    }

    public void setVocabularyWordId(Long vocabularyWordId) {
        this.vocabularyWordId = vocabularyWordId;
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
}
