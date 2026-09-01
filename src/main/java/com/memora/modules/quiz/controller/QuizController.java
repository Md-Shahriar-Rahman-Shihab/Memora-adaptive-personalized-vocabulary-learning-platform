package com.memora.modules.quiz.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.quiz.dto.*;
import com.memora.modules.quiz.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing Quiz and Question Engine endpoints.
 * All learner operations authenticate and resolve user identity via Spring Security.
 */
@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * Deterministically generates a new vocabulary quiz.
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<QuizResponse>> generateQuiz(
            @Valid @RequestBody(required = false) QuizGenerationRequest request) {
        QuizResponse response = quizService.generateQuiz(request != null ? request : new QuizGenerationRequest());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz generated successfully", response));
    }

    /**
     * Starts or resumes a quiz attempt for the authenticated user.
     */
    @PostMapping("/{quizId}/start")
    public ResponseEntity<ApiResponse<QuizResponse>> startQuiz(
            @PathVariable Long quizId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        QuizResponse response = quizService.startQuiz(userEmail, quizId);
        return ResponseEntity.ok(ApiResponse.success("Quiz started successfully", response));
    }

    /**
     * Submits and evaluates a learner's answer for a specific question within a quiz.
     */
    @PostMapping("/{quizId}/questions/{questionId}/answer")
    public ResponseEntity<ApiResponse<AnswerResponse>> submitAnswer(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @Valid @RequestBody AnswerSubmissionRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        request.setQuestionId(questionId);
        AnswerResponse response = quizService.submitAnswer(userEmail, quizId, questionId, request);
        return ResponseEntity.ok(ApiResponse.success("Answer evaluated and progress updated", response));
    }

    /**
     * Completes a quiz attempt and computes final summary scores.
     */
    @PostMapping("/{quizId}/complete")
    public ResponseEntity<ApiResponse<QuizResultResponse>> completeQuiz(
            @PathVariable Long quizId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        QuizResultResponse response = quizService.completeQuiz(userEmail, quizId);
        return ResponseEntity.ok(ApiResponse.success("Quiz completed successfully", response));
    }

    /**
     * Retrieves quiz details and questions without leaking answers.
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuiz(@PathVariable Long quizId) {
        QuizResponse response = quizService.getQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Retrieves the latest quiz attempt results for the authenticated user.
     */
    @GetMapping("/{quizId}/result")
    public ResponseEntity<ApiResponse<QuizResultResponse>> getQuizResult(
            @PathVariable Long quizId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        QuizResultResponse response = quizService.getQuizResult(userEmail, quizId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
