package com.memora.modules.partner;

import com.memora.modules.partner.challenge.dto.ChallengeAnswerRequest;
import com.memora.modules.partner.challenge.dto.ChallengeQuestionResponse;
import com.memora.modules.partner.challenge.dto.CreateChallengeRequest;
import com.memora.modules.partner.challenge.dto.SubmitChallengeRequest;
import com.memora.modules.partner.challenge.entity.VocabularyChallenge;
import com.memora.modules.partner.challenge.repository.ChallengeAttemptRepository;
import com.memora.modules.partner.challenge.repository.VocabularyChallengeRepository;
import com.memora.modules.partner.challenge.service.VocabularyChallengeService;
import com.memora.modules.partner.domain.PartnerActivityType;
import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.partner.dto.PartnerActivityResponse;
import com.memora.modules.partner.entity.PartnerActivity;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerActivityRepository;
import com.memora.modules.partner.repository.PartnerRelationshipRepository;
import com.memora.modules.partner.service.PartnerActivityService;
import com.memora.modules.quiz.repository.QuizRepository;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PartnerActivityServiceTest {

    @Autowired
    private PartnerActivityService partnerActivityService;

    @Autowired
    private VocabularyChallengeService vocabularyChallengeService;

    @Autowired
    private PartnerActivityRepository partnerActivityRepository;

    @Autowired
    private VocabularyChallengeRepository vocabularyChallengeRepository;

    @Autowired
    private ChallengeAttemptRepository challengeAttemptRepository;

    @Autowired
    private PartnerRelationshipRepository partnerRelationshipRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;
    private User userC;

    private static final String EMAIL_A = "alice.act_phase_e@test.com";
    private static final String EMAIL_B = "bob.act_phase_e@test.com";
    private static final String EMAIL_C = "charlie.act_phase_e@test.com";

    @BeforeEach
    void setUp() {
        cleanTestData();

        userA = userRepository.save(new User("Alice ActivityTest", EMAIL_A, "hash123", VocabularyLevel.B1, Role.LEARNER));
        userB = userRepository.save(new User("Bob ActivityTest", EMAIL_B, "hash456", VocabularyLevel.B2, Role.LEARNER));
        userC = userRepository.save(new User("Charlie UnrelatedActivity", EMAIL_C, "hash789", VocabularyLevel.A1, Role.LEARNER));

        for (int i = 1; i <= 5; i++) {
            String wordName = "phaseeword" + i;
            if (!vocabularyWordRepository.existsByWordIgnoreCase(wordName)) {
                vocabularyWordRepository.save(new VocabularyWord(
                        wordName, "meaning " + i, "definition " + i, "/w/", "example sentence " + i, DifficultyLevel.B1, WordCategory.ACADEMIC
                ));
            }
        }
    }

    @AfterEach
    void tearDown() {
        cleanTestData();
    }

    private void cleanTestData() {
        partnerActivityRepository.deleteAll();
        challengeAttemptRepository.deleteAll();
        vocabularyChallengeRepository.deleteAll();
        partnerRelationshipRepository.deleteAll();
        userRepository.findByEmail(EMAIL_A).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_B).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_C).ifPresent(userRepository::delete);
    }

    private PartnerRelationship createRelationship(User a, User b, PartnerRelationshipStatus status) {
        User u1 = a.getId() < b.getId() ? a : b;
        User u2 = a.getId() < b.getId() ? b : a;
        return partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, a, status));
    }

    // ==========================================
    // ACTIVITY RETRIEVAL & PRIVACY TESTS
    // ==========================================

    @Test
    @DisplayName("16. Accepted partners can view activity")
    void getPartnerActivities_AcceptedPartnersCanView() {
        PartnerRelationship rel = createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);
        partnerActivityService.logActivity(rel, userA, PartnerActivityType.PARTNER_CONNECTED, "Connected with partner", null, 0);

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_B, 20);
        assertFalse(activities.isEmpty());
        assertEquals("Connected with partner", activities.get(0).getTitle());
        assertEquals("PARTNER_CONNECTED", activities.get(0).getActivityType());
    }

    @Test
    @DisplayName("17. Unrelated users cannot view private partner activity")
    void getPartnerActivities_UnrelatedUsersCannotView() {
        PartnerRelationship rel = createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);
        partnerActivityService.logActivity(rel, userA, PartnerActivityType.PARTNER_CONNECTED, "Secret connection", null, 0);

        // Charlie is unrelated to A and B
        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_C, 20);
        assertTrue(activities.isEmpty());
    }

    @Test
    @DisplayName("18. Pending relationship users cannot view activity")
    void getPartnerActivities_PendingUsersCannotView() {
        PartnerRelationship rel = createRelationship(userA, userB, PartnerRelationshipStatus.PENDING);
        partnerActivityService.logActivity(rel, userA, PartnerActivityType.CHALLENGE_CREATED, "Pending test", null, 0);

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_B, 20);
        assertTrue(activities.isEmpty());
    }

    @Test
    @DisplayName("19. Rejected relationship users cannot view activity")
    void getPartnerActivities_RejectedUsersCannotView() {
        PartnerRelationship rel = createRelationship(userA, userB, PartnerRelationshipStatus.REJECTED);
        partnerActivityService.logActivity(rel, userA, PartnerActivityType.CHALLENGE_CREATED, "Rejected test", null, 0);

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_B, 20);
        assertTrue(activities.isEmpty());
    }

    @Test
    @DisplayName("20. Cancelled relationship users cannot view activity")
    void getPartnerActivities_CancelledUsersCannotView() {
        PartnerRelationship rel = createRelationship(userA, userB, PartnerRelationshipStatus.CANCELLED);
        partnerActivityService.logActivity(rel, userA, PartnerActivityType.CHALLENGE_CREATED, "Cancelled test", null, 0);

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_B, 20);
        assertTrue(activities.isEmpty());
    }

    @Test
    @DisplayName("21. Newest activity appears first (createdAt DESC)")
    void getPartnerActivities_NewestActivityAppearsFirst() throws InterruptedException {
        PartnerRelationship rel = createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);

        partnerActivityService.logActivity(rel, userA, PartnerActivityType.CHALLENGE_CREATED, "First Event", null, 0);
        Thread.sleep(20);
        partnerActivityService.logActivity(rel, userB, PartnerActivityType.CHALLENGE_ACCEPTED, "Second Event", null, 0);

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_A, 20);
        assertEquals(2, activities.size());
        assertEquals("Second Event", activities.get(0).getTitle());
        assertEquals("First Event", activities.get(1).getTitle());
    }

    @Test
    @DisplayName("22. Activity does not expose sensitive fields (email, password)")
    void getPartnerActivities_DoesNotExposeSensitiveFields() {
        PartnerRelationship rel = createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);
        partnerActivityService.logActivity(rel, userA, PartnerActivityType.CHALLENGE_WON, "Alice won", "Won 10 pts", 10);

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_B, 20);
        assertEquals(1, activities.size());
        PartnerActivityResponse res = activities.get(0);

        assertNotNull(res.getActor());
        assertEquals(userA.getId(), res.getActor().getId());
        assertEquals("Alice ActivityTest", res.getActor().getName());
        assertEquals("CHALLENGE_WON", res.getActivityType());
        assertEquals(10, res.getXpEarned());
    }

    // ==========================================
    // CHALLENGE → ACTIVITY INTEGRATION TESTS
    // ==========================================

    @Test
    @DisplayName("23. Challenge creation generates correct activity")
    void challengeCreation_GeneratesCorrectActivity() {
        createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);

        CreateChallengeRequest request = new CreateChallengeRequest(userB.getId(), "B1", 3);
        vocabularyChallengeService.createChallenge(EMAIL_A, request);

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_A, 20);
        assertFalse(activities.isEmpty());
        assertTrue(activities.stream().anyMatch(a ->
                "CHALLENGE_CREATED".equals(a.getActivityType()) && a.getTitle().contains("challenged")
        ));
    }

    @Test
    @DisplayName("24. Challenge acceptance generates correct activity")
    void challengeAcceptance_GeneratesCorrectActivity() {
        createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);

        CreateChallengeRequest request = new CreateChallengeRequest(userB.getId(), "B1", 3);
        var created = vocabularyChallengeService.createChallenge(EMAIL_A, request);

        vocabularyChallengeService.acceptChallenge(EMAIL_B, created.id());

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_A, 20);
        assertTrue(activities.stream().anyMatch(a ->
                "CHALLENGE_ACCEPTED".equals(a.getActivityType()) && a.getTitle().contains("accepted")
        ));
    }

    @Test
    @DisplayName("25. Challenge completion generates correct activity")
    void challengeCompletion_GeneratesCorrectActivity() {
        createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);

        CreateChallengeRequest request = new CreateChallengeRequest(userB.getId(), "B1", 3);
        var created = vocabularyChallengeService.createChallenge(EMAIL_A, request);
        vocabularyChallengeService.acceptChallenge(EMAIL_B, created.id());

        var questions = vocabularyChallengeService.getChallengeQuestions(EMAIL_A, created.id());
        SubmitChallengeRequest submitReq = new SubmitChallengeRequest(
                List.of(new ChallengeAnswerRequest(questions.get(0).id(), "any_ans", 100L))
        );
        vocabularyChallengeService.submitChallenge(EMAIL_A, created.id(), submitReq);

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_B, 20);
        assertTrue(activities.stream().anyMatch(a ->
                "CHALLENGE_COMPLETED".equals(a.getActivityType()) && a.getTitle().contains("completed")
        ));
    }

    @Test
    @DisplayName("26. Winner or Draw activity is generated when duel finishes")
    void challengeOutcome_GeneratesOutcomeActivity() {
        createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);

        CreateChallengeRequest request = new CreateChallengeRequest(userB.getId(), "B1", 3);
        var created = vocabularyChallengeService.createChallenge(EMAIL_A, request);
        vocabularyChallengeService.acceptChallenge(EMAIL_B, created.id());

        var questions = vocabularyChallengeService.getChallengeQuestions(EMAIL_A, created.id());

        // Alice submits answers
        vocabularyChallengeService.submitChallenge(EMAIL_A, created.id(), new SubmitChallengeRequest(
                List.of(new ChallengeAnswerRequest(questions.get(0).id(), "ans_alice", 100L))
        ));

        // Bob submits answers
        vocabularyChallengeService.submitChallenge(EMAIL_B, created.id(), new SubmitChallengeRequest(
                List.of(new ChallengeAnswerRequest(questions.get(0).id(), "ans_bob", 100L))
        ));

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_B, 20);
        assertTrue(activities.stream().anyMatch(a ->
                "CHALLENGE_WON".equals(a.getActivityType()) || "CHALLENGE_DRAW".equals(a.getActivityType())
        ));
    }

    @Test
    @DisplayName("27. Draw activity is generated when duel finishes with equal scores")
    void challengeDraw_GeneratesDrawActivity() {
        createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);

        CreateChallengeRequest request = new CreateChallengeRequest(userB.getId(), "B1", 3);
        var created = vocabularyChallengeService.createChallenge(EMAIL_A, request);
        vocabularyChallengeService.acceptChallenge(EMAIL_B, created.id());

        var questions = vocabularyChallengeService.getChallengeQuestions(EMAIL_A, created.id());

        // Both answer wrong -> 0 vs 0 (Draw)
        vocabularyChallengeService.submitChallenge(EMAIL_A, created.id(), new SubmitChallengeRequest(
                List.of(new ChallengeAnswerRequest(questions.get(0).id(), "WRONG_A", 100L))
        ));
        vocabularyChallengeService.submitChallenge(EMAIL_B, created.id(), new SubmitChallengeRequest(
                List.of(new ChallengeAnswerRequest(questions.get(0).id(), "WRONG_B", 100L))
        ));

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_A, 20);
        assertTrue(activities.stream().anyMatch(a ->
                "CHALLENGE_DRAW".equals(a.getActivityType()) && a.getTitle().contains("drew their vocabulary duel")
        ));
    }

    @Test
    @DisplayName("28. Duplicate submission does not create duplicate activity")
    void duplicateSubmission_DoesNotCreateDuplicateActivity() {
        PartnerRelationship rel = createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);

        partnerActivityService.logActivity(rel, userA, PartnerActivityType.CHALLENGE_CREATED, "Alice vs Bob", null, 0);
        // Repeated log attempt with same title and event
        PartnerActivity second = partnerActivityService.logActivity(rel, userA, PartnerActivityType.CHALLENGE_CREATED, "Alice vs Bob", null, 0);

        assertNull(second); // Deduplicated!
        assertEquals(1, partnerActivityRepository.count());
    }

    @Test
    @DisplayName("29. Deterministic deduplication preserves two distinct challenges completed in succession")
    void twoDistinctChallengesCompleted_BothActivitiesPreserved() {
        PartnerRelationship rel = createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);

        // Challenge A completed
        PartnerActivity actA = partnerActivityService.logActivity(
                rel, userA, PartnerActivityType.CHALLENGE_COMPLETED,
                "Alice completed their challenge attempt", "3/3 correct (30 pts)", 15, 101L
        );
        assertNotNull(actA);

        // Challenge B completed (same user, same relationship, same title, but distinct sourceEntityId = 102L)
        PartnerActivity actB = partnerActivityService.logActivity(
                rel, userA, PartnerActivityType.CHALLENGE_COMPLETED,
                "Alice completed their challenge attempt", "3/3 correct (30 pts)", 15, 102L
        );
        assertNotNull(actB, "Distinct challenge completion must not be suppressed by deduplication");
        assertEquals(2, partnerActivityRepository.count());

        List<PartnerActivityResponse> activities = partnerActivityService.getPartnerActivities(EMAIL_A, 20);
        assertEquals(2, activities.size());
    }

    @Test
    @DisplayName("30. Deterministic deduplication blocks duplicate event with same sourceEntityId")
    void duplicateEventWithSameSourceEntityId_IsDeduplicated() {
        PartnerRelationship rel = createRelationship(userA, userB, PartnerRelationshipStatus.ACCEPTED);

        // First attempt for challenge 201L
        PartnerActivity act1 = partnerActivityService.logActivity(
                rel, userA, PartnerActivityType.CHALLENGE_COMPLETED,
                "Alice completed their challenge attempt", "3/3 correct (30 pts)", 15, 201L
        );
        assertNotNull(act1);

        // Duplicate submission retry for the exact same challenge 201L
        PartnerActivity act2 = partnerActivityService.logActivity(
                rel, userA, PartnerActivityType.CHALLENGE_COMPLETED,
                "Alice completed their challenge attempt", "3/3 correct (30 pts)", 15, 201L
        );
        assertNull(act2, "Retry/duplicate of same source entity must be deduplicated");
        assertEquals(1, partnerActivityRepository.count());
    }
}
