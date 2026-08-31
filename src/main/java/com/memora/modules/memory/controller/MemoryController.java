package com.memora.modules.memory.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.memory.dto.MemoryWordResponse;
import com.memora.modules.memory.dto.WordReviewRequest;
import com.memora.modules.memory.dto.WordReviewResponse;
import com.memora.modules.memory.service.MemoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing the Memory Engine and spaced repetition endpoints.
 *
 * All endpoints resolve the active learner identity strictly from Spring Security context.
 */
@RestController
@RequestMapping("/api/v1/memory")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    /**
     * Records a learner's single word review attempt and computes the new retention schedule.
     *
     * @param request Validated {@link WordReviewRequest}
     * @param authentication Authenticated security principal
     * @return Updated memory state wrapped in {@link ApiResponse}
     */
    @PostMapping("/review")
    public ResponseEntity<ApiResponse<WordReviewResponse>> recordReview(
            @Valid @RequestBody WordReviewRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        WordReviewResponse response = memoryService.recordReview(email, request);
        return ResponseEntity.ok(ApiResponse.success("Review recorded and memory schedule updated successfully", response));
    }

    /**
     * Retrieves all vocabulary words scheduled for spaced repetition review at or before the current time.
     *
     * @param authentication Authenticated security principal
     * @return List of due words wrapped in {@link ApiResponse}
     */
    @GetMapping("/due")
    public ResponseEntity<ApiResponse<List<MemoryWordResponse>>> getDueReviews(Authentication authentication) {
        String email = authentication.getName();
        List<MemoryWordResponse> dueWords = memoryService.getDueReviews(email);
        return ResponseEntity.ok(ApiResponse.success(dueWords));
    }

    /**
     * Retrieves the learner's weakest vocabulary words sorted with highest forgetting risk first.
     *
     * @param authentication Authenticated security principal
     * @return List of weak words wrapped in {@link ApiResponse}
     */
    @GetMapping("/weak")
    public ResponseEntity<ApiResponse<List<MemoryWordResponse>>> getWeakWords(Authentication authentication) {
        String email = authentication.getName();
        List<MemoryWordResponse> weakWords = memoryService.getWeakWords(email);
        return ResponseEntity.ok(ApiResponse.success(weakWords));
    }
}
