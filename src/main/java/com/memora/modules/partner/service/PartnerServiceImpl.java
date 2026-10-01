package com.memora.modules.partner.service;

import com.memora.common.exception.BadRequestException;
import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.gamification.dto.LearnerStatsResponse;
import com.memora.modules.gamification.service.GamificationService;
import com.memora.modules.partner.domain.PartnerActivityType;
import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.partner.dto.PartnerProgressResponse;
import com.memora.modules.partner.dto.PartnerRequestResponse;
import com.memora.modules.partner.dto.PartnerRequestsSummaryResponse;
import com.memora.modules.partner.dto.PartnerUserSummaryResponse;
import com.memora.modules.partner.dto.SendPartnerRequestDto;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerRelationshipRepository;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Production implementation of {@link PartnerService}.
 * Manages the lifecycle of learning partner requests, canonical pair constraints,
 * enforces strict participant authorization rules, and provides privacy-safe progress sharing.
 */
@Service
@Transactional(readOnly = true)
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRelationshipRepository partnerRelationshipRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;
    private final PartnerActivityService partnerActivityService;

    @Autowired
    public PartnerServiceImpl(PartnerRelationshipRepository partnerRelationshipRepository,
                              UserRepository userRepository,
                              GamificationService gamificationService,
                              @Autowired(required = false) PartnerActivityService partnerActivityService) {
        this.partnerRelationshipRepository = partnerRelationshipRepository;
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
        this.partnerActivityService = partnerActivityService;
    }

    public PartnerServiceImpl(PartnerRelationshipRepository partnerRelationshipRepository,
                              UserRepository userRepository,
                              GamificationService gamificationService) {
        this(partnerRelationshipRepository, userRepository, gamificationService, null);
    }

    @Override
    public List<PartnerUserSummaryResponse> searchUsers(String currentUserEmail, String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        User currentUser = getUserByEmail(currentUserEmail);
        List<User> foundUsers = userRepository.searchByNameExcludingUser(query.trim(), currentUser.getId());

        return foundUsers.stream()
                .limit(20)
                .map(u -> toSummaryResponse(u, null))
                .toList();
    }

    @Override
    public PartnerUserSummaryResponse searchByEmail(String currentUserEmail, String targetEmail) {
        if (targetEmail == null || targetEmail.trim().isEmpty()) {
            throw new BadRequestException("Email address is required");
        }

        String normalizedEmail = targetEmail.trim();
        User currentUser = getUserByEmail(currentUserEmail);

        if (currentUser.getEmail().equalsIgnoreCase(normalizedEmail)) {
            throw new BadRequestException("You cannot add yourself as a learning partner.");
        }

        User targetUser = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No Memora account found with this email."));

        // Determine existing relationship status between current user and target user
        String relationshipStatus = "NONE";
        Optional<PartnerRelationship> relOpt = partnerRelationshipRepository.findRelationshipBetween(
                currentUser.getId(), targetUser.getId()
        );

        if (relOpt.isPresent()) {
            PartnerRelationship rel = relOpt.get();
            if (rel.getStatus() == PartnerRelationshipStatus.ACCEPTED) {
                relationshipStatus = "ACCEPTED";
            } else if (rel.getStatus() == PartnerRelationshipStatus.PENDING) {
                if (rel.isSender(currentUser.getId())) {
                    relationshipStatus = "PENDING_SENT";
                } else {
                    relationshipStatus = "PENDING_RECEIVED";
                }
            }
        }

        return toSummaryResponse(targetUser, relationshipStatus);
    }

    @Override
    @Transactional
    public PartnerRequestResponse sendPartnerRequest(String currentUserEmail, SendPartnerRequestDto dto) {
        User sender = getUserByEmail(currentUserEmail);

        if (dto == null || dto.getTargetUserId() == null) {
            throw new IllegalArgumentException("Target user ID is required");
        }

        Long targetUserId = dto.getTargetUserId();
        if (sender.getId().equals(targetUserId)) {
            throw new IllegalArgumentException("Cannot send partner request to yourself");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found with ID: " + targetUserId));

        // Enforce canonical pair ordering: userOne.id < userTwo.id
        User userOne = sender.getId() < targetUser.getId() ? sender : targetUser;
        User userTwo = sender.getId() < targetUser.getId() ? targetUser : sender;

        Optional<PartnerRelationship> existingOpt = partnerRelationshipRepository
                .findByUserOneIdAndUserTwoId(userOne.getId(), userTwo.getId());

        if (existingOpt.isPresent()) {
            PartnerRelationship existing = existingOpt.get();

            if (existing.getStatus() == PartnerRelationshipStatus.ACCEPTED) {
                throw new IllegalArgumentException("You are already learning partners with this user");
            }

            if (existing.getStatus() == PartnerRelationshipStatus.PENDING) {
                if (existing.isSender(sender.getId())) {
                    throw new IllegalArgumentException("You have already sent a pending partner request to this user");
                } else {
                    throw new IllegalArgumentException("This user has already sent you a partner request. Please accept it instead.");
                }
            }

            // Reuse existing row for REJECTED or CANCELLED relationships to avoid unique constraint violations
            existing.setStatus(PartnerRelationshipStatus.PENDING);
            existing.setRequestedBy(sender);
            existing.setUpdatedAt(Instant.now());
            PartnerRelationship saved = partnerRelationshipRepository.save(existing);
            return toPartnerRequestResponse(saved, sender.getId());
        }

        // Create new relationship
        PartnerRelationship newRelationship = new PartnerRelationship(
                userOne,
                userTwo,
                sender,
                PartnerRelationshipStatus.PENDING
        );
        PartnerRelationship saved = partnerRelationshipRepository.save(newRelationship);
        return toPartnerRequestResponse(saved, sender.getId());
    }

    @Override
    @Transactional
    public PartnerUserSummaryResponse acceptRequest(String currentUserEmail, Long requestId) {
        User currentUser = getUserByEmail(currentUserEmail);

        PartnerRelationship relationship = partnerRelationshipRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner request not found with ID: " + requestId));

        if (!relationship.isInvolved(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to accept this partner request");
        }

        if (relationship.isSender(currentUser.getId())) {
            throw new AccessDeniedException("Only the recipient can accept a partner request");
        }

        if (relationship.getStatus() != PartnerRelationshipStatus.PENDING) {
            throw new IllegalArgumentException("Cannot accept request with status: " + relationship.getStatus());
        }

        relationship.setStatus(PartnerRelationshipStatus.ACCEPTED);
        relationship.setUpdatedAt(Instant.now());
        PartnerRelationship saved = partnerRelationshipRepository.save(relationship);

        User partner = saved.getOtherUser(currentUser.getId());

        if (partnerActivityService != null) {
            partnerActivityService.logActivity(
                    saved,
                    currentUser,
                    PartnerActivityType.PARTNER_CONNECTED,
                    currentUser.getName() + " became learning partners with " + partner.getName(),
                    "Learning partnership established",
                    0,
                    saved.getId()
            );
        }

        return toSummaryResponse(partner, PartnerRelationshipStatus.ACCEPTED.name());
    }

    @Override
    @Transactional
    public PartnerRequestResponse rejectRequest(String currentUserEmail, Long requestId) {
        User currentUser = getUserByEmail(currentUserEmail);

        PartnerRelationship relationship = partnerRelationshipRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner request not found with ID: " + requestId));

        if (!relationship.isInvolved(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to reject this partner request");
        }

        if (relationship.isSender(currentUser.getId())) {
            throw new AccessDeniedException("Only the recipient can reject a partner request");
        }

        if (relationship.getStatus() != PartnerRelationshipStatus.PENDING) {
            throw new IllegalArgumentException("Cannot reject request with status: " + relationship.getStatus());
        }

        relationship.setStatus(PartnerRelationshipStatus.REJECTED);
        relationship.setUpdatedAt(Instant.now());
        PartnerRelationship saved = partnerRelationshipRepository.save(relationship);

        return toPartnerRequestResponse(saved, currentUser.getId());
    }

    @Override
    @Transactional
    public PartnerRequestResponse cancelRequest(String currentUserEmail, Long requestId) {
        User currentUser = getUserByEmail(currentUserEmail);

        PartnerRelationship relationship = partnerRelationshipRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner request not found with ID: " + requestId));

        if (!relationship.isInvolved(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to cancel this partner request");
        }

        if (!relationship.isSender(currentUser.getId())) {
            throw new AccessDeniedException("Only the sender can cancel this partner request");
        }

        if (relationship.getStatus() != PartnerRelationshipStatus.PENDING) {
            throw new IllegalArgumentException("Cannot cancel request with status: " + relationship.getStatus());
        }

        relationship.setStatus(PartnerRelationshipStatus.CANCELLED);
        relationship.setUpdatedAt(Instant.now());
        PartnerRelationship saved = partnerRelationshipRepository.save(relationship);

        return toPartnerRequestResponse(saved, currentUser.getId());
    }

    @Override
    public List<PartnerUserSummaryResponse> getActivePartners(String currentUserEmail) {
        User currentUser = getUserByEmail(currentUserEmail);
        List<PartnerRelationship> activeRelationships = partnerRelationshipRepository
                .findActivePartnersForUser(currentUser.getId());

        return activeRelationships.stream()
                .map(rel -> toSummaryResponse(rel.getOtherUser(currentUser.getId()), PartnerRelationshipStatus.ACCEPTED.name()))
                .toList();
    }

    @Override
    public PartnerRequestsSummaryResponse getPartnerRequests(String currentUserEmail) {
        User currentUser = getUserByEmail(currentUserEmail);
        List<PartnerRelationship> pendingRelationships = partnerRelationshipRepository
                .findPendingRequestsForUser(currentUser.getId());

        List<PartnerRequestResponse> incoming = new ArrayList<>();
        List<PartnerRequestResponse> outgoing = new ArrayList<>();

        for (PartnerRelationship rel : pendingRelationships) {
            boolean isIncoming = !rel.isSender(currentUser.getId());
            PartnerRequestResponse response = toPartnerRequestResponse(rel, currentUser.getId());
            if (isIncoming) {
                incoming.add(response);
            } else {
                outgoing.add(response);
            }
        }

        return new PartnerRequestsSummaryResponse(incoming, outgoing);
    }

    @Override
    public PartnerProgressResponse getPartnerProgress(String currentUserEmail, Long partnerId) {
        User currentUser = getUserByEmail(currentUserEmail);

        if (partnerId == null) {
            throw new IllegalArgumentException("Partner ID cannot be null");
        }

        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found with ID: " + partnerId));

        // Strict authorization: Only active ACCEPTED partners can view progress
        partnerRelationshipRepository.findActiveRelationshipBetweenUsers(currentUser.getId(), partner.getId())
                .orElseThrow(() -> new AccessDeniedException("This progress is only available to your accepted learning partner"));

        // Single source of truth for gamification metrics
        LearnerStatsResponse stats = gamificationService.getStats(partner.getEmail());

        return new PartnerProgressResponse(
                partner.getId(),
                partner.getName(),
                partner.getCurrentLevel() != null ? partner.getCurrentLevel().name() : "A1",
                stats.getTotalXp(),
                stats.getCurrentStreak(),
                stats.getWordsLearned(),
                stats.getMasteredWords(),
                stats.getAccuracy()
        );
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private PartnerUserSummaryResponse toSummaryResponse(User user, String relationshipStatus) {
        return new PartnerUserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getCurrentLevel() != null ? user.getCurrentLevel().name() : "A1",
                user.getXp(),
                user.getStreak(),
                relationshipStatus
        );
    }

    private PartnerRequestResponse toPartnerRequestResponse(PartnerRelationship rel, Long currentUserId) {
        User sender = rel.getRequestedBy();
        User receiver = rel.getOtherUser(sender.getId());
        boolean incoming = !sender.getId().equals(currentUserId);

        return new PartnerRequestResponse(
                rel.getId(),
                toSummaryResponse(sender, null),
                toSummaryResponse(receiver, null),
                rel.getStatus().name(),
                rel.getCreatedAt() != null ? rel.getCreatedAt() : Instant.now(),
                incoming
        );
    }
}
