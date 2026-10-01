package com.memora.modules.partner.service;

import com.memora.modules.partner.domain.PartnerActivityType;
import com.memora.modules.partner.dto.PartnerActivityResponse;
import com.memora.modules.partner.entity.PartnerActivity;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.user.entity.User;

import java.util.List;

/**
 * Service contract defining operations for partner activity logging and retrieval.
 */
public interface PartnerActivityService {

    /**
     * Records a privacy-safe activity event for an active partner relationship.
     * Enforces idempotency to avoid duplicate records.
     *
     * @param relationship The partner relationship
     * @param actor The user performing the action
     * @param type The activity type
     * @param title High-level description of the event
     * @param details Additional context
     * @param xpEarned Optional XP earned from this event
     * @return Saved or existing PartnerActivity
     */
    PartnerActivity logActivity(PartnerRelationship relationship,
                                User actor,
                                PartnerActivityType type,
                                String title,
                                String details,
                                Integer xpEarned,
                                Long sourceEntityId);

    PartnerActivity logActivity(PartnerRelationship relationship,
                                User actor,
                                PartnerActivityType type,
                                String title,
                                String details,
                                Integer xpEarned);

    /**
     * Retrieves the chronological partner activity feed for the authenticated user.
     * Only events belonging to active ACCEPTED relationships are returned.
     *
     * @param currentUserEmail Authenticated user's email
     * @param limit Maximum number of records to return (default 20)
     * @return List of privacy-safe partner activities ordered by newest first
     */
    List<PartnerActivityResponse> getPartnerActivities(String currentUserEmail, int limit);
}
