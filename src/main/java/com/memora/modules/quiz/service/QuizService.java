package com.memora.modules.quiz.service;

import com.memora.modules.quiz.dto.*;

/**
 * Service interface orchestrating Quiz creation, execution, evaluation, and memory integration.
 */
public interface QuizService {

    /**
     * Generates a new Quiz populated with polymorphic questions for the requested level and size.
     */
    QuizResponse generateQuiz(QuizGenerationRequest request);

    /**
     * Initiates a new QuizAttempt session for the authenticated learner.
     */
    QuizResponse startQuiz(String userEmail, Long quizId);

    /**
     * Retrieves quiz details and questions without leaking correct answers.
     */
    QuizResponse getQuiz(Long quizId);

    /**
     * Evaluates a learner's answer for an individual question, persists performance history,
     * and delegates retention updates to {@link com.memora.modules.memory.service.MemoryService}.
     */
    AnswerResponse submitAnswer(String userEmail, Long quizId, Long questionId, AnswerSubmissionRequest request);

    /**
     * Concludes a quiz attempt, calculates final score metrics, and returns summary results.
     */
    QuizResultResponse completeQuiz(String userEmail, Long quizId);

    /**
     * Retrieves the latest completion results for a user's quiz attempt.
     */
    QuizResultResponse getQuizResult(String userEmail, Long quizId);
}
