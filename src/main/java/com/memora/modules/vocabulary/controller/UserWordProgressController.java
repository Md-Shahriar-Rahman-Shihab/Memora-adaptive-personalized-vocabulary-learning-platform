package com.memora.modules.vocabulary.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.vocabulary.dto.UserWordProgressResponse;
import com.memora.modules.vocabulary.service.UserWordProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller exposing REST API endpoints for tracking and querying a learner's vocabulary progress.
 * All endpoints resolve identity purely from the authenticated Spring Security context.
 */
@RestController
@RequestMapping("/api/v1/progress")
public class UserWordProgressController {

    private final UserWordProgressService userWordProgressService;

    public UserWordProgressController(UserWordProgressService userWordProgressService) {
        this.userWordProgressService = userWordProgressService;
    }

    @GetMapping("/words")
    public ResponseEntity<ApiResponse<List<UserWordProgressResponse>>> getUserProgress(Authentication authentication) {
        String email = authentication.getName();
        List<UserWordProgressResponse> progressList = userWordProgressService.getProgressForUser(email);
        return ResponseEntity.ok(ApiResponse.success(progressList));
    }

    @GetMapping("/words/{wordId}")
    public ResponseEntity<ApiResponse<UserWordProgressResponse>> getWordProgress(
            @PathVariable Long wordId,
            Authentication authentication) {
        String email = authentication.getName();
        UserWordProgressResponse response = userWordProgressService.getProgressForWord(email, wordId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/words/{wordId}/init")
    public ResponseEntity<ApiResponse<UserWordProgressResponse>> initializeWordProgress(
            @PathVariable Long wordId,
            Authentication authentication) {
        String email = authentication.getName();
        UserWordProgressResponse response = userWordProgressService.initializeProgress(email, wordId);
        return ResponseEntity.ok(ApiResponse.success("Progress initialized successfully", response));
    }

    @GetMapping("/weak")
    public ResponseEntity<ApiResponse<List<UserWordProgressResponse>>> getWeakWords(Authentication authentication) {
        String email = authentication.getName();
        List<UserWordProgressResponse> weakWords = userWordProgressService.getWeakWords(email);
        return ResponseEntity.ok(ApiResponse.success(weakWords));
    }

    @GetMapping("/review")
    public ResponseEntity<ApiResponse<List<UserWordProgressResponse>>> getDueReviews(Authentication authentication) {
        String email = authentication.getName();
        List<UserWordProgressResponse> dueReviews = userWordProgressService.getDueReviews(email);
        return ResponseEntity.ok(ApiResponse.success(dueReviews));
    }
}
