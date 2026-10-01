package com.memora.modules.partner.challenge.dto;

import java.time.Instant;

/**
 * Dedicated summary response DTO for a {@link com.memora.modules.partner.challenge.entity.VocabularyChallenge}.
 * Strictly enforces anti-cheating by suppressing partner score, answers, and winner details until both users finish.
 */
public record ChallengeResponse(
    Long id,
    Long relationshipId,
    Long challengerId,
    String challengerName,
    Long challengedUserId,
    String challengedUserName,
    String status,
    String cefrLevel,
    int questionCount,
    boolean currentUserCompleted,
    boolean partnerCompleted,
    Integer myScore,
    Integer partnerScore,
    Instant myCompletedAt,
    Instant partnerCompletedAt,
    Long winnerId,
    String winnerName,
    boolean isDraw,
    Instant createdAt
) {}
