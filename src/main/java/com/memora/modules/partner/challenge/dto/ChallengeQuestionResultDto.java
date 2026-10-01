package com.memora.modules.partner.challenge.dto;

/**
 * Breakdown of an individual question result for a participant after submission.
 */
public record ChallengeQuestionResultDto(
    Long questionId,
    String questionText,
    String userAnswer,
    String correctAnswer,
    boolean isCorrect,
    int pointsEarned
) {}
