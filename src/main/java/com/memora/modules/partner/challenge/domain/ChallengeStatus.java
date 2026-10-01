package com.memora.modules.partner.challenge.domain;

/**
 * Enumeration representing the formal lifecycle states of a {@link com.memora.modules.partner.challenge.entity.VocabularyChallenge}.
 */
public enum ChallengeStatus {
    PENDING,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    DECLINED,
    CANCELLED
}
