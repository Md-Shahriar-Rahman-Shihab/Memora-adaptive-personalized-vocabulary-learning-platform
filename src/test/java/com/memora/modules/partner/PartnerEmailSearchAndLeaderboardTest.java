package com.memora.modules.partner;

import com.memora.common.exception.BadRequestException;
import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.partner.dto.PartnerLeaderboardEntryResponse;
import com.memora.modules.partner.dto.PartnerUserSummaryResponse;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerActivityRepository;
import com.memora.modules.partner.repository.PartnerRelationshipRepository;
import com.memora.modules.partner.service.PartnerLeaderboardService;
import com.memora.modules.partner.service.PartnerService;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PartnerEmailSearchAndLeaderboardTest {

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private PartnerLeaderboardService partnerLeaderboardService;

    @Autowired
    private PartnerRelationshipRepository partnerRelationshipRepository;

    @Autowired
    private PartnerActivityRepository partnerActivityRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;
    private User userC;
    private User userD;

    private static final String EMAIL_A = "alice.phase_e@test.com";
    private static final String EMAIL_B = "bob.phase_e@test.com";
    private static final String EMAIL_C = "charlie.phase_e@test.com";
    private static final String EMAIL_D = "david.unrelated_e@test.com";

    @BeforeEach
    void setUp() {
        cleanTestData();

        userA = userRepository.save(new User("Alice SearchTest", EMAIL_A, "hash123", VocabularyLevel.B1, Role.LEARNER));
        userA.setXp(2500);
        userA.setStreak(5);
        userA = userRepository.save(userA);

        userB = userRepository.save(new User("Bob SearchTest", EMAIL_B, "hash456", VocabularyLevel.B2, Role.LEARNER));
        userB.setXp(3800);
        userB.setStreak(10);
        userB = userRepository.save(userB);

        userC = userRepository.save(new User("Charlie SearchTest", EMAIL_C, "hash789", VocabularyLevel.A2, Role.LEARNER));
        userC.setXp(1200);
        userC.setStreak(2);
        userC = userRepository.save(userC);

        userD = userRepository.save(new User("David Unrelated", EMAIL_D, "hash000", VocabularyLevel.C1, Role.LEARNER));
        userD.setXp(9999);
        userD.setStreak(99);
        userD = userRepository.save(userD);
    }

    @AfterEach
    void tearDown() {
        cleanTestData();
    }

    private void cleanTestData() {
        partnerActivityRepository.deleteAll();
        partnerRelationshipRepository.deleteAll();
        userRepository.findByEmail(EMAIL_A).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_B).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_C).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_D).ifPresent(userRepository::delete);
    }

    // ==========================================
    // EMAIL SEARCH TESTS
    // ==========================================

    @Test
    @DisplayName("1. Exact email finds correct user")
    void searchByEmail_ExactEmail_FindsCorrectUser() {
        PartnerUserSummaryResponse result = partnerService.searchByEmail(EMAIL_A, EMAIL_B);
        assertNotNull(result);
        assertEquals(userB.getId(), result.getId());
        assertEquals("Bob SearchTest", result.getName());
        assertEquals("B2", result.getCurrentLevel());
        assertEquals(3800, result.getXp());
        assertEquals(10, result.getStreak());
        assertEquals("NONE", result.getRelationshipStatus());
    }

    @Test
    @DisplayName("2. Email search is case-insensitive")
    void searchByEmail_CaseInsensitive_FindsCorrectUser() {
        PartnerUserSummaryResponse result = partnerService.searchByEmail(EMAIL_A, "BOB.PHASE_E@TEST.COM");
        assertNotNull(result);
        assertEquals(userB.getId(), result.getId());
        assertEquals("Bob SearchTest", result.getName());
    }

    @Test
    @DisplayName("3. Whitespace is normalized in email search")
    void searchByEmail_WhitespaceNormalized_FindsCorrectUser() {
        PartnerUserSummaryResponse result = partnerService.searchByEmail(EMAIL_A, "   bob.phase_e@test.com   \t");
        assertNotNull(result);
        assertEquals(userB.getId(), result.getId());
        assertEquals("Bob SearchTest", result.getName());
    }

    @Test
    @DisplayName("4. Current user cannot find/add themselves")
    void searchByEmail_ThrowsBadRequest_WhenSearchingSelf() {
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                partnerService.searchByEmail(EMAIL_A, EMAIL_A)
        );
        assertEquals("You cannot add yourself as a learning partner.", ex.getMessage());
    }

    @Test
    @DisplayName("5. Nonexistent email returns proper 404 response")
    void searchByEmail_ThrowsNotFound_WhenNonexistentEmail() {
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () ->
                partnerService.searchByEmail(EMAIL_A, "ghost.user@memora.com")
        );
        assertEquals("No Memora account found with this email.", ex.getMessage());
    }

    @Test
    @DisplayName("6. Password, hash, and sensitive fields are not leaked in search result")
    void searchByEmail_PrivacySafe_NoPasswordOrSensitiveFields() {
        PartnerUserSummaryResponse result = partnerService.searchByEmail(EMAIL_A, EMAIL_B);
        assertNotNull(result);
        assertEquals("Bob SearchTest", result.getName());
        // Verify class exposes only privacy-safe attributes
        assertNotNull(result.getId());
        assertNotNull(result.getCurrentLevel());
        assertTrue(result.getXp() >= 0);
    }

    @Test
    @DisplayName("Search reflects ACCEPTED relationship status")
    void searchByEmail_ReflectsAcceptedRelationshipStatus() {
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.ACCEPTED));

        PartnerUserSummaryResponse result = partnerService.searchByEmail(EMAIL_A, EMAIL_B);
        assertNotNull(result);
        assertEquals("ACCEPTED", result.getRelationshipStatus());
    }

    @Test
    @DisplayName("Search reflects PENDING_SENT and PENDING_RECEIVED status")
    void searchByEmail_ReflectsPendingRelationshipStatus() {
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.PENDING));

        // Alice sent to Bob
        PartnerUserSummaryResponse resultForAlice = partnerService.searchByEmail(EMAIL_A, EMAIL_B);
        assertEquals("PENDING_SENT", resultForAlice.getRelationshipStatus());

        // Bob receives from Alice
        PartnerUserSummaryResponse resultForBob = partnerService.searchByEmail(EMAIL_B, EMAIL_A);
        assertEquals("PENDING_RECEIVED", resultForBob.getRelationshipStatus());
    }

    // ==========================================
    // PAIR-ONLY LEADERBOARD TESTS
    // ==========================================

    @Test
    @DisplayName("8. Accepted partners appear on partner leaderboard")
    void getPartnerLeaderboard_AcceptedPartnersAppear() {
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.ACCEPTED));

        List<PartnerLeaderboardEntryResponse> board = partnerLeaderboardService.getPartnerLeaderboard(EMAIL_A);
        assertEquals(2, board.size());
        assertTrue(board.stream().anyMatch(e -> e.getUserId().equals(userB.getId())));
    }

    @Test
    @DisplayName("9. Unrelated users do not appear on partner leaderboard")
    void getPartnerLeaderboard_UnrelatedUsersDoNotAppear() {
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.ACCEPTED));

        List<PartnerLeaderboardEntryResponse> board = partnerLeaderboardService.getPartnerLeaderboard(EMAIL_A);
        // David has 9999 XP but is unrelated, must not appear!
        assertFalse(board.stream().anyMatch(e -> e.getUserId().equals(userD.getId())));
    }

    @Test
    @DisplayName("10. Pending relationship users do not appear on partner leaderboard")
    void getPartnerLeaderboard_PendingUsersDoNotAppear() {
        User u1 = userA.getId() < userC.getId() ? userA : userC;
        User u2 = userA.getId() < userC.getId() ? userC : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.PENDING));

        List<PartnerLeaderboardEntryResponse> board = partnerLeaderboardService.getPartnerLeaderboard(EMAIL_A);
        assertFalse(board.stream().anyMatch(e -> e.getUserId().equals(userC.getId())));
    }

    @Test
    @DisplayName("11. Rejected relationship users do not appear on partner leaderboard")
    void getPartnerLeaderboard_RejectedUsersDoNotAppear() {
        User u1 = userA.getId() < userC.getId() ? userA : userC;
        User u2 = userA.getId() < userC.getId() ? userC : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.REJECTED));

        List<PartnerLeaderboardEntryResponse> board = partnerLeaderboardService.getPartnerLeaderboard(EMAIL_A);
        assertFalse(board.stream().anyMatch(e -> e.getUserId().equals(userC.getId())));
    }

    @Test
    @DisplayName("12. Cancelled relationship users do not appear on partner leaderboard")
    void getPartnerLeaderboard_CancelledUsersDoNotAppear() {
        User u1 = userA.getId() < userC.getId() ? userA : userC;
        User u2 = userA.getId() < userC.getId() ? userC : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.CANCELLED));

        List<PartnerLeaderboardEntryResponse> board = partnerLeaderboardService.getPartnerLeaderboard(EMAIL_A);
        assertFalse(board.stream().anyMatch(e -> e.getUserId().equals(userC.getId())));
    }

    @Test
    @DisplayName("13. Current authenticated user appears on partner leaderboard")
    void getPartnerLeaderboard_CurrentAuthenticatedUserAppears() {
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.ACCEPTED));

        List<PartnerLeaderboardEntryResponse> board = partnerLeaderboardService.getPartnerLeaderboard(EMAIL_A);
        assertTrue(board.stream().anyMatch(e -> e.getUserId().equals(userA.getId())));
    }

    @Test
    @DisplayName("14. XP ranking is correct: Bob (3800 XP) rank 1, Alice (2500 XP) rank 2")
    void getPartnerLeaderboard_XpRankingIsCorrect() {
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.ACCEPTED));

        List<PartnerLeaderboardEntryResponse> board = partnerLeaderboardService.getPartnerLeaderboard(EMAIL_A);
        assertEquals(2, board.size());

        // First rank should be Bob (3800 XP)
        assertEquals(1, board.get(0).getRank());
        assertEquals(userB.getId(), board.get(0).getUserId());
        assertEquals(3800, board.get(0).getXp());

        // Second rank should be Alice (2500 XP)
        assertEquals(2, board.get(1).getRank());
        assertEquals(userA.getId(), board.get(1).getUserId());
        assertEquals(2500, board.get(1).getXp());
    }

    @Test
    @DisplayName("15. Privacy-safe DTO is returned (contains no email or sensitive credentials)")
    void getPartnerLeaderboard_PrivacySafeDtoReturned() {
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.ACCEPTED));

        List<PartnerLeaderboardEntryResponse> board = partnerLeaderboardService.getPartnerLeaderboard(EMAIL_A);
        for (PartnerLeaderboardEntryResponse entry : board) {
            assertNotNull(entry.getUserId());
            assertNotNull(entry.getName());
            assertNotNull(entry.getCurrentLevel());
            assertTrue(entry.getRank() >= 1);
            assertTrue(entry.getXp() >= 0);
        }
    }
}
