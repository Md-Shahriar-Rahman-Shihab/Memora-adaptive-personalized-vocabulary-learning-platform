package com.memora.modules.learningpath.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.learningpath.dto.*;
import com.memora.modules.learningpath.service.LearningPathService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing adaptive learning path curriculum endpoints.
 * User identity is strictly resolved from the Spring Security context.
 */
@RestController
@RequestMapping("/api/v1/learning-path")
public class LearningPathController {

    private final LearningPathService learningPathService;

    public LearningPathController(LearningPathService learningPathService) {
        this.learningPathService = learningPathService;
    }

    /**
     * Starts a new adaptive learning path or resumes the active learning path.
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<LearningPathResponse>> startPath(Authentication authentication) {
        String email = authentication.getName();
        LearningPathResponse response = learningPathService.startPath(email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Learning path initiated successfully", response));
    }

    /**
     * Retrieves the learner's active learning path summary and item status.
     */
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<LearningPathResponse>> getCurrentPath(Authentication authentication) {
        String email = authentication.getName();
        LearningPathResponse response = learningPathService.getCurrentPath(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves today's prioritized learning path tasks.
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayLearningPathResponse>> getTodayPath(Authentication authentication) {
        String email = authentication.getName();
        TodayLearningPathResponse response = learningPathService.getTodayPath(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Marks an individual learning path task as in-progress.
     */
    @PostMapping("/items/{itemId}/start")
    public ResponseEntity<ApiResponse<LearningPathItemResponse>> startItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        String email = authentication.getName();
        LearningPathItemResponse response = learningPathService.startItem(email, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item marked in-progress", response));
    }

    /**
     * Concludes an individual learning path item and applies retention metrics if applicable.
     */
    @PostMapping("/items/{itemId}/complete")
    public ResponseEntity<ApiResponse<LearningItemCompletionResponse>> completeItem(
            @PathVariable Long itemId,
            @RequestBody(required = false) LearningItemCompletionRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        LearningItemCompletionResponse response = learningPathService.completeItem(email, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Item completed successfully", response));
    }

    /**
     * Dynamically regenerates future pending items based on updated retention and performance metrics.
     */
    @PostMapping("/regenerate")
    public ResponseEntity<ApiResponse<LearningPathResponse>> regeneratePath(Authentication authentication) {
        String email = authentication.getName();
        LearningPathResponse response = learningPathService.regeneratePath(email);
        return ResponseEntity.ok(ApiResponse.success("Learning path regenerated successfully", response));
    }

    /**
     * Advances to the next day's new learning path curriculum.
     */
    @PostMapping("/advance")
    public ResponseEntity<ApiResponse<LearningPathResponse>> advanceToNextDay(Authentication authentication) {
        String email = authentication.getName();
        LearningPathResponse response = learningPathService.advanceToNextDay(email);
        return ResponseEntity.ok(ApiResponse.success("Advanced to next day's learning path successfully", response));
    }

    /**
     * Retrieves historical learning path records for the learner.
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<LearningPathResponse>>> getPathHistory(Authentication authentication) {
        String email = authentication.getName();
        List<LearningPathResponse> history = learningPathService.getPathHistory(email);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}
