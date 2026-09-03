package com.memora.modules.learningpath.service;

import com.memora.modules.learningpath.dto.*;

import java.util.List;

/**
 * Service contract orchestrating adaptive personalized learning path generation,
 * item execution, progress tracking, and dynamic curriculum regeneration.
 */
public interface LearningPathService {

    /**
     * Starts or resumes an active personalized learning path for the authenticated learner.
     */
    LearningPathResponse startPath(String userEmail);

    /**
     * Retrieves the active learning path summary for the learner.
     */
    LearningPathResponse getCurrentPath(String userEmail);

    /**
     * Retrieves today's prioritized learning items for the learner.
     */
    TodayLearningPathResponse getTodayPath(String userEmail);

    /**
     * Marks an individual learning task as in-progress.
     */
    LearningPathItemResponse startItem(String userEmail, Long itemId);

    /**
     * Concludes a learning task, triggers Memory Engine or Quiz Engine updates if applicable,
     * and updates overall path progress.
     */
    LearningItemCompletionResponse completeItem(String userEmail, Long itemId, LearningItemCompletionRequest request);

    /**
     * Dynamically regenerates remaining pending items based on latest memory retention and quiz performance.
     */
    LearningPathResponse regeneratePath(String userEmail);

    /**
     * Retrieves historical learning path records for the learner.
     */
    List<LearningPathResponse> getPathHistory(String userEmail);
}
