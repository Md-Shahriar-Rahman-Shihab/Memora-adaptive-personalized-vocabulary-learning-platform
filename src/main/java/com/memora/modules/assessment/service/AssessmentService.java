package com.memora.modules.assessment.service;

import com.memora.modules.assessment.dto.*;

import java.util.List;

/**
 * Service contract orchestrating diagnostic placement assessment workflows, question presentation,
 * answer submission, and algorithmic CEFR proficiency estimation.
 */
public interface AssessmentService {

    /**
     * Initiates a new diagnostic placement assessment for the authenticated learner.
     */
    AssessmentStartResponse startAssessment(String userEmail);

    /**
     * Retrieves the details and question status of an assessment.
     */
    AssessmentDetailResponse getAssessment(String userEmail, Long assessmentId);

    /**
     * Submits and evaluates a learner's answer for an assessment question.
     */
    AssessmentAnswerResponse submitAnswer(String userEmail, Long assessmentId, Long questionId, AssessmentAnswerRequest request);

    /**
     * Concludes the assessment session, verifies completeness, and executes algorithmic CEFR placement.
     */
    PlacementResultResponse completeAssessment(String userEmail, Long assessmentId);

    /**
     * Retrieves the placement result for a completed assessment session.
     */
    PlacementResultResponse getAssessmentResult(String userEmail, Long assessmentId);

    /**
     * Retrieves historical placement assessments for the authenticated learner.
     */
    List<PlacementResultResponse> getAssessmentHistory(String userEmail);
}
