package com.memora.modules.assessment.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.assessment.dto.*;
import com.memora.modules.assessment.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing diagnostic placement assessment endpoints.
 * All operations resolve learner identity strictly from the Spring Security context.
 */
@RestController
@RequestMapping("/api/v1/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    /**
     * Starts a new diagnostic placement assessment for the authenticated learner.
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<AssessmentStartResponse>> startAssessment(Authentication authentication) {
        String email = authentication.getName();
        AssessmentStartResponse response = assessmentService.startAssessment(email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Assessment started successfully", response));
    }

    /**
     * Retrieves the active details and questions of an assessment session.
     */
    @GetMapping("/{assessmentId}")
    public ResponseEntity<ApiResponse<AssessmentDetailResponse>> getAssessment(
            @PathVariable Long assessmentId,
            Authentication authentication) {
        String email = authentication.getName();
        AssessmentDetailResponse response = assessmentService.getAssessment(email, assessmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Submits an answer and latency for a specific question within an assessment.
     */
    @PostMapping("/{assessmentId}/questions/{questionId}/answer")
    public ResponseEntity<ApiResponse<AssessmentAnswerResponse>> submitAnswer(
            @PathVariable Long assessmentId,
            @PathVariable Long questionId,
            @Valid @RequestBody AssessmentAnswerRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        AssessmentAnswerResponse response = assessmentService.submitAnswer(email, assessmentId, questionId, request);
        return ResponseEntity.ok(ApiResponse.success("Answer evaluated successfully", response));
    }

    /**
     * Finalizes the assessment, verifies completeness, and executes algorithmic CEFR placement.
     */
    @PostMapping("/{assessmentId}/complete")
    public ResponseEntity<ApiResponse<PlacementResultResponse>> completeAssessment(
            @PathVariable Long assessmentId,
            Authentication authentication) {
        String email = authentication.getName();
        PlacementResultResponse response = assessmentService.completeAssessment(email, assessmentId);
        return ResponseEntity.ok(ApiResponse.success("Assessment completed successfully", response));
    }

    /**
     * Retrieves the placement result for a completed assessment session.
     */
    @GetMapping("/{assessmentId}/result")
    public ResponseEntity<ApiResponse<PlacementResultResponse>> getAssessmentResult(
            @PathVariable Long assessmentId,
            Authentication authentication) {
        String email = authentication.getName();
        PlacementResultResponse response = assessmentService.getAssessmentResult(email, assessmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves all historical placement assessments completed by the learner.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PlacementResultResponse>>> getAssessmentHistory(Authentication authentication) {
        String email = authentication.getName();
        List<PlacementResultResponse> history = assessmentService.getAssessmentHistory(email);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
