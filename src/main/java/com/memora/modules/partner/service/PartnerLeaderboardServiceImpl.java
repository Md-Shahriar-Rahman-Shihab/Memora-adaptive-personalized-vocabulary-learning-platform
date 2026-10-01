package com.memora.modules.partner.service;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.gamification.dto.LearnerStatsResponse;
import com.memora.modules.gamification.service.GamificationService;
import com.memora.modules.partner.dto.PartnerLeaderboardEntryResponse;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerRelationshipRepository;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Production implementation of {@link PartnerLeaderboardService}.
 * Composes a pair-only leaderboard strictly consisting of the authenticated user
 * and their active ACCEPTED learning partners.
 */
@Service
@Transactional(readOnly = true)
public class PartnerLeaderboardServiceImpl implements PartnerLeaderboardService {

    private final PartnerRelationshipRepository partnerRelationshipRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    @Autowired
    public PartnerLeaderboardServiceImpl(PartnerRelationshipRepository partnerRelationshipRepository,
                                        UserRepository userRepository,
                                        @Autowired(required = false) GamificationService gamificationService) {
        this.partnerRelationshipRepository = partnerRelationshipRepository;
        this.userRepository = userRepository;
        this.gamificationService = gamificationService;
    }

    @Override
    public List<PartnerLeaderboardEntryResponse> getPartnerLeaderboard(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + currentUserEmail));

        // 1. Retrieve all active accepted relationships for current user
        List<PartnerRelationship> activeRels = partnerRelationshipRepository
                .findActivePartnersForUser(currentUser.getId());

        // 2. Build participant set: currentUser + accepted partners
        Map<Long, User> participants = new LinkedHashMap<>();
        participants.put(currentUser.getId(), currentUser);

        for (PartnerRelationship rel : activeRels) {
            User partner = rel.getOtherUser(currentUser.getId());
            if (partner != null && !participants.containsKey(partner.getId())) {
                participants.put(partner.getId(), partner);
            }
        }

        // 3. Collect statistics for each participant
        List<ParticipantRecord> records = new ArrayList<>();
        for (User u : participants.values()) {
            int xp = u.getXp();
            int streak = u.getStreak();
            int wordsLearned = 0;

            if (gamificationService != null) {
                try {
                    LearnerStatsResponse stats = gamificationService.getStats(u.getEmail());
                    if (stats != null) {
                        xp = stats.getTotalXp();
                        streak = stats.getCurrentStreak();
                        wordsLearned = stats.getWordsLearned();
                    }
                } catch (Exception ignored) {
                    // Fallback to user entity state
                }
            }

            records.add(new ParticipantRecord(u, xp, streak, wordsLearned));
        }

        // 4. Sort descending by Total XP, tie-breaking by streak, then name
        records.sort((a, b) -> {
            int cmp = Integer.compare(b.xp, a.xp);
            if (cmp != 0) return cmp;
            int streakCmp = Integer.compare(b.streak, a.streak);
            if (streakCmp != 0) return streakCmp;
            return a.user.getName().compareToIgnoreCase(b.user.getName());
        });

        // 5. Assign ranks and build privacy-safe DTOs
        List<PartnerLeaderboardEntryResponse> entries = new ArrayList<>();
        int currentRank = 1;
        for (ParticipantRecord record : records) {
            String level = record.user.getCurrentLevel() != null
                    ? record.user.getCurrentLevel().name()
                    : "A1";

            entries.add(new PartnerLeaderboardEntryResponse(
                    currentRank++,
                    record.user.getId(),
                    record.user.getName(),
                    level,
                    record.xp,
                    record.streak,
                    record.wordsLearned
            ));
        }

        return entries;
    }

    private static class ParticipantRecord {
        final User user;
        final int xp;
        final int streak;
        final int wordsLearned;

        ParticipantRecord(User user, int xp, int streak, int wordsLearned) {
            this.user = user;
            this.xp = xp;
            this.streak = streak;
            this.wordsLearned = wordsLearned;
        }
    }
}
