package com.memora.modules.partner.domain;

/**
 * Domain enumeration defining the types of learning partner activity events.
 */
public enum PartnerActivityType {
    PARTNER_CONNECTED,
    CHALLENGE_CREATED,
    CHALLENGE_ACCEPTED,
    CHALLENGE_COMPLETED,
    CHALLENGE_WON,
    CHALLENGE_DRAW,
    XP_MILESTONE,
    STREAK_MILESTONE,
    WORDS_MILESTONE
}
