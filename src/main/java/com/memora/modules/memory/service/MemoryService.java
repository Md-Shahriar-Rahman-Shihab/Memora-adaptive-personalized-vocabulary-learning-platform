package com.memora.modules.memory.service;

import com.memora.modules.memory.dto.MemoryWordResponse;
import com.memora.modules.memory.dto.WordReviewRequest;
import com.memora.modules.memory.dto.WordReviewResponse;

import java.util.List;

/**
 * Service contract orchestrating adaptive memory retention, spaced repetition scheduling,
 * and learner memory analytics.
 */
public interface MemoryService {

    /**
     * Records a learner's review response, calculates new retention metrics via the requested strategy,
     * updates user word progress, and returns the calculation result.
     *
     * @param userEmail Email of the authenticated user
     * @param request {@link WordReviewRequest} review payload
     * @return {@link WordReviewResponse}
     */
    WordReviewResponse recordReview(String userEmail, WordReviewRequest request);

    /**
     * Retrieves all vocabulary words currently due for spaced repetition review for the authenticated user.
     *
     * @param userEmail Email of the authenticated user
     * @return List of {@link MemoryWordResponse}
     */
    List<MemoryWordResponse> getDueReviews(String userEmail);

    /**
     * Retrieves all vocabulary words flagged with elevated forgetting risk or low mastery scores.
     *
     * @param userEmail Email of the authenticated user
     * @return List of {@link MemoryWordResponse} sorted with weakest words first
     */
    List<MemoryWordResponse> getWeakWords(String userEmail);
}
