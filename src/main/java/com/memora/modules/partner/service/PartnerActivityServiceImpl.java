package com.memora.modules.partner.service;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.partner.domain.PartnerActivityType;
import com.memora.modules.partner.dto.PartnerActivityResponse;
import com.memora.modules.partner.entity.PartnerActivity;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerActivityRepository;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Production implementation of {@link PartnerActivityService}.
 * Manages the creation, deduplication, and privacy-safe retrieval of partner activities.
 */
@Service
@Transactional(readOnly = true)
public class PartnerActivityServiceImpl implements PartnerActivityService {

    private static final Logger log = LoggerFactory.getLogger(PartnerActivityServiceImpl.class);

    private final PartnerActivityRepository partnerActivityRepository;
    private final UserRepository userRepository;

    public PartnerActivityServiceImpl(PartnerActivityRepository partnerActivityRepository,
                                      UserRepository userRepository) {
        this.partnerActivityRepository = partnerActivityRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public PartnerActivity logActivity(PartnerRelationship relationship,
                                       User actor,
                                       PartnerActivityType type,
                                       String title,
                                       String details,
                                       Integer xpEarned) {
        return logActivity(relationship, actor, type, title, details, xpEarned, null);
    }

    @Override
    @Transactional
    public PartnerActivity logActivity(PartnerRelationship relationship,
                                       User actor,
                                       PartnerActivityType type,
                                       String title,
                                       String details,
                                       Integer xpEarned,
                                       Long sourceEntityId) {
        if (relationship == null || actor == null || type == null || title == null) {
            log.warn("Skipping activity log: missing mandatory arguments");
            return null;
        }

        // Deterministic event identity deduplication:
        // When sourceEntityId is provided (e.g. challenge ID or relationship ID),
        // check if this specific event for (relationship, activityType, actor, sourceEntityId) has already been logged.
        if (relationship.getId() != null && sourceEntityId != null && actor.getId() != null) {
            if (partnerActivityRepository.existsByRelationshipIdAndActivityTypeAndActorIdAndSourceEntityId(
                    relationship.getId(), type, actor.getId(), sourceEntityId)) {
                log.debug("Activity already logged for relationshipId={}, type={}, actorId={}, sourceEntityId={}",
                        relationship.getId(), type, actor.getId(), sourceEntityId);
                return null;
            }
        } else if (relationship.getId() != null) {
            // Fallback for events without a sourceEntityId: deduplicate by relationship, type, and title
            if (partnerActivityRepository.existsByRelationshipIdAndActivityTypeAndTitle(relationship.getId(), type, title)) {
                log.debug("Activity already logged for relationshipId={}, type={}, title='{}'", relationship.getId(), type, title);
                return null;
            }
        }

        PartnerActivity activity = new PartnerActivity(
                relationship,
                actor,
                type,
                title,
                details,
                xpEarned != null ? xpEarned : 0
        );
        activity.setSourceEntityId(sourceEntityId);

        return partnerActivityRepository.save(activity);
    }

    @Override
    public List<PartnerActivityResponse> getPartnerActivities(String currentUserEmail, int limit) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        int safeLimit = Math.min(Math.max(1, limit), 50);
        List<PartnerActivity> activities = partnerActivityRepository.findRecentActivitiesForUser(
                currentUser.getId(),
                PageRequest.of(0, safeLimit)
        );

        if (activities == null || activities.isEmpty()) {
            return Collections.emptyList();
        }

        return activities.stream()
                .map(this::toResponse)
                .toList();
    }

    private PartnerActivityResponse toResponse(PartnerActivity activity) {
        PartnerActivityResponse.ActorSummary actorSummary = null;
        if (activity.getActor() != null) {
            actorSummary = new PartnerActivityResponse.ActorSummary(
                    activity.getActor().getId(),
                    activity.getActor().getName()
            );
        }

        return new PartnerActivityResponse(
                activity.getId(),
                actorSummary,
                activity.getActivityType() != null ? activity.getActivityType().name() : null,
                activity.getTitle(),
                activity.getDetails(),
                activity.getXpEarned(),
                activity.getCreatedAt()
        );
    }
}
