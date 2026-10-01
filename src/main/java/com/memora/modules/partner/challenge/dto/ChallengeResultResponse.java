package com.memora.modules.partner.challenge.dto;

import java.util.List;

/**
 * Result payload returned upon challenge submission or retrieval of challenge results.
 * Respects anti-cheating by masking partner metrics if the partner has not finished yet.
 */
public record ChallengeResultResponse(
    Long challengeId,
    String status,
    boolean completed,
    boolean waitingForPartner,
    Integer myScore,
    Integer myCorrectCount,
    Integer totalQuestions,
    Integer partnerScore,
    Integer partnerCorrectCount,
    Long winnerId,
    String winnerName,
    boolean isDraw,
    int xpEarned,
    List<ChallengeQuestionResultDto> myQuestionBreakdown
) {}
