package com.memora.modules.partner.challenge.dto;

import java.util.List;

/**
 * Privacy-safe, anti-cheating question representation for challenges.
 * Deliberately excludes correct answers, expected answers, and answer keys.
 */
public record ChallengeQuestionResponse(
    Long id,
    Long wordId,
    String word,
    String questionType,
    int points,
    String questionText,
    List<String> options,
    String sentence
) {}
