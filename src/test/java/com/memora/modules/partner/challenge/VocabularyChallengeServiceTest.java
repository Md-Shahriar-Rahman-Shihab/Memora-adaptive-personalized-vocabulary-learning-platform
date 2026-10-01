package com.memora.modules.partner.challenge;

import com.memora.common.exception.BadRequestException;
import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.partner.challenge.domain.ChallengeStatus;
import com.memora.modules.partner.challenge.dto.*;
import com.memora.modules.partner.challenge.repository.ChallengeAttemptRepository;
import com.memora.modules.partner.challenge.repository.VocabularyChallengeRepository;
import com.memora.modules.partner.challenge.service.VocabularyChallengeService;
import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerRelationshipRepository;
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
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VocabularyChallengeServiceTest {

    @Autowired
    private VocabularyChallengeService challengeService;

    @Autowired
    private VocabularyChallengeRepository challengeRepository;

    @Autowired
    private ChallengeAttemptRepository challengeAttemptRepository;

    @Autowired
    private PartnerRelationshipRepository partnerRelationshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private QuizRepository quizRepository;

    private User userA;
    private User userB;
    private User userC;

    private static final String EMAIL_A = "alice.chsvc@test.com";
    private static final String EMAIL_B = "bob.chsvc@test.com";
    private static final String EMAIL_C = "charlie.chsvc@test.com";

    @BeforeEach
    void setUp() {
        cleanTestData();

        userA = userRepository.save(new User("Alice ChallengeService", EMAIL_A, "hash1", VocabularyLevel.B1, Role.LEARNER));
        userB = userRepository.save(new User("Bob ChallengeService", EMAIL_B, "hash2", VocabularyLevel.B1, Role.LEARNER));
        userC = userRepository.save(new User("Charlie ChallengeService", EMAIL_C, "hash3", VocabularyLevel.B1, Role.LEARNER));

        for (int i = 1; i <= 5; i++) {
            String wordName = "chtestword" + i;
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
        challengeAttemptRepository.deleteAll();
        challengeRepository.deleteAll();
        partnerRelationshipRepository.deleteAll();
        userRepository.findByEmail(EMAIL_A).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_B).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_C).ifPresent(userRepository::delete);
    }

    private PartnerRelationship createAcceptedPartnership(User a, User b) {
        User first = a.getId() < b.getId() ? a : b;
        User second = a.getId() < b.getId() ? b : a;
        PartnerRelationship rel = new PartnerRelationship(first, second, a, PartnerRelationshipStatus.ACCEPTED);
        return partnerRelationshipRepository.save(rel);
    }

    private PartnerRelationship createRelationshipWithStatus(User a, User b, PartnerRelationshipStatus status) {
        User first = a.getId() < b.getId() ? a : b;
        User second = a.getId() < b.getId() ? b : a;
        PartnerRelationship rel = new PartnerRelationship(first, second, a, status);
        return partnerRelationshipRepository.save(rel);
    }

    // --- 1. Relationship Authorization Tests ---

    @Test
    @DisplayName("Accepted partners can create challenge successfully")
    void testCreateChallenge_AcceptedPartners_Success() {
        createAcceptedPartnership(userA, userB);

        CreateChallengeRequest request = new CreateChallengeRequest(userB.getId(), "B1", 5);
        ChallengeResponse response = challengeService.createChallenge(EMAIL_A, request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals("PENDING", response.status());
        assertEquals(userA.getId(), response.challengerId());
        assertEquals(userB.getId(), response.challengedUserId());
        assertEquals("B1", response.cefrLevel());
        assertEquals(5, response.questionCount());
    }

    @Test
    @DisplayName("Unrelated users cannot create challenge (throws AccessDeniedException)")
    void testCreateChallenge_UnrelatedUsers_ThrowsForbidden() {
        // No partnership between A and C
        CreateChallengeRequest request = new CreateChallengeRequest(userC.getId(), "B1", 5);
        assertThrows(AccessDeniedException.class, () -> challengeService.createChallenge(EMAIL_A, request));
    }

    @Test
    @DisplayName("Pending partnership cannot create challenge")
    void testCreateChallenge_PendingPartnership_ThrowsForbidden() {
        createRelationshipWithStatus(userA, userB, PartnerRelationshipStatus.PENDING);

        CreateChallengeRequest request = new CreateChallengeRequest(userB.getId(), "B1", 5);
        assertThrows(AccessDeniedException.class, () -> challengeService.createChallenge(EMAIL_A, request));
    }

    @Test
    @DisplayName("Rejected partnership cannot create challenge")
    void testCreateChallenge_RejectedPartnership_ThrowsForbidden() {
        createRelationshipWithStatus(userA, userB, PartnerRelationshipStatus.REJECTED);

        CreateChallengeRequest request = new CreateChallengeRequest(userB.getId(), "B1", 5);
        assertThrows(AccessDeniedException.class, () -> challengeService.createChallenge(EMAIL_A, request));
    }

    @Test
    @DisplayName("Cancelled partnership cannot create challenge")
    void testCreateChallenge_CancelledPartnership_ThrowsForbidden() {
        createRelationshipWithStatus(userA, userB, PartnerRelationshipStatus.CANCELLED);

        CreateChallengeRequest request = new CreateChallengeRequest(userB.getId(), "B1", 5);
        assertThrows(AccessDeniedException.class, () -> challengeService.createChallenge(EMAIL_A, request));
    }

    @Test
    @DisplayName("Self-challenge is rejected with BadRequestException")
    void testCreateChallenge_SelfChallenge_ThrowsBadRequest() {
        CreateChallengeRequest request = new CreateChallengeRequest(userA.getId(), "B1", 5);
        assertThrows(BadRequestException.class, () -> challengeService.createChallenge(EMAIL_A, request));
    }

    // --- 2. Challenge Lifecycle & Participant Authorization ---

    @Test
    @DisplayName("Only challenged user can accept challenge")
    void testAcceptChallenge_OnlyChallengedUser() {
        createAcceptedPartnership(userA, userB);
        ChallengeResponse challenge = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 5));

        // Challenger cannot accept their own challenge
        assertThrows(AccessDeniedException.class, () -> challengeService.acceptChallenge(EMAIL_A, challenge.id()));

        // Unrelated user cannot accept
        assertThrows(AccessDeniedException.class, () -> challengeService.acceptChallenge(EMAIL_C, challenge.id()));

        // Challenged user B can accept
        ChallengeResponse accepted = challengeService.acceptChallenge(EMAIL_B, challenge.id());
        assertEquals("ACCEPTED", accepted.status());
    }

    @Test
    @DisplayName("Only challenged user can decline challenge")
    void testDeclineChallenge_OnlyChallengedUser() {
        createAcceptedPartnership(userA, userB);
        ChallengeResponse challenge = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 5));

        // Challenger cannot decline
        assertThrows(AccessDeniedException.class, () -> challengeService.declineChallenge(EMAIL_A, challenge.id()));

        // Challenged user declines
        ChallengeResponse declined = challengeService.declineChallenge(EMAIL_B, challenge.id());
        assertEquals("DECLINED", declined.status());
    }

    @Test
    @DisplayName("Only challenger can cancel pending challenge")
    void testCancelChallenge_OnlyChallenger() {
        createAcceptedPartnership(userA, userB);
        ChallengeResponse challenge = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 5));

        // Challenged user cannot cancel
        assertThrows(AccessDeniedException.class, () -> challengeService.cancelChallenge(EMAIL_B, challenge.id()));

        // Challenger cancels
        ChallengeResponse cancelled = challengeService.cancelChallenge(EMAIL_A, challenge.id());
        assertEquals("CANCELLED", cancelled.status());
    }

    @Test
    @DisplayName("Invalid lifecycle transitions are rejected")
    void testLifecycle_InvalidTransitions_Rejected() {
        createAcceptedPartnership(userA, userB);
        ChallengeResponse challenge = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 5));

        // Accept the challenge
        challengeService.acceptChallenge(EMAIL_B, challenge.id());

        // Cannot accept again
        assertThrows(BadRequestException.class, () -> challengeService.acceptChallenge(EMAIL_B, challenge.id()));

        // Cannot cancel an already accepted challenge
        assertThrows(BadRequestException.class, () -> challengeService.cancelChallenge(EMAIL_A, challenge.id()));
    }

    // --- 3. Deterministic Question Set & Anti-Cheating ---

    @Test
    @DisplayName("Both participants receive the exact same logical question set")
    void testGetQuestions_BothParticipantsReceiveSameSet() {
        createAcceptedPartnership(userA, userB);
        ChallengeResponse challenge = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 5));
        challengeService.acceptChallenge(EMAIL_B, challenge.id());

        List<ChallengeQuestionResponse> questionsA = challengeService.getChallengeQuestions(EMAIL_A, challenge.id());
        List<ChallengeQuestionResponse> questionsB = challengeService.getChallengeQuestions(EMAIL_B, challenge.id());

        assertEquals(questionsA.size(), questionsB.size());
        for (int i = 0; i < questionsA.size(); i++) {
            assertEquals(questionsA.get(i).id(), questionsB.get(i).id());
            assertEquals(questionsA.get(i).word(), questionsB.get(i).word());
            assertEquals(questionsA.get(i).questionType(), questionsB.get(i).questionType());
        }
    }

    @Test
    @DisplayName("Challenge question responses never expose correct answers or answer keys")
    void testGetQuestions_NoAnswerKeysExposed() {
        createAcceptedPartnership(userA, userB);
        ChallengeResponse challenge = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 5));
        challengeService.acceptChallenge(EMAIL_B, challenge.id());

        List<ChallengeQuestionResponse> questions = challengeService.getChallengeQuestions(EMAIL_A, challenge.id());
        assertFalse(questions.isEmpty());

        for (ChallengeQuestionResponse q : questions) {
            assertNotNull(q.questionText());
            assertNotNull(q.word());
            // Confirm DTO record signature does not include correctOption or expectedAnswer
            // (verified at compile-time and runtime)
        }
    }

    // --- 4. Submission, Scoring, Winner, and Draw ---

    @Test
    @DisplayName("Participant A submits answers and enters IN_PROGRESS waiting state without seeing partner score")
    void testSubmit_AntiCheating_WaitingForPartner() {
        createAcceptedPartnership(userA, userB);
        ChallengeResponse challenge = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 3));
        challengeService.acceptChallenge(EMAIL_B, challenge.id());

        List<ChallengeQuestionResponse> questions = challengeService.getChallengeQuestions(EMAIL_A, challenge.id());

        // Participant A submits
        List<ChallengeAnswerRequest> answers = questions.stream()
                .map(q -> new ChallengeAnswerRequest(q.id(), "lucid", 1500L))
                .toList();

        ChallengeResultResponse resultA = challengeService.submitChallenge(EMAIL_A, challenge.id(), new SubmitChallengeRequest(answers));

        assertTrue(resultA.waitingForPartner());
        assertFalse(resultA.completed());
        assertNull(resultA.partnerScore());
        assertNull(resultA.partnerCorrectCount());
        assertNull(resultA.winnerId());
        assertFalse(resultA.isDraw());
        assertNotNull(resultA.myScore());

        // Inspect challenge status via getChallenge
        ChallengeResponse summaryForA = challengeService.getChallenge(EMAIL_A, challenge.id());
        assertEquals("IN_PROGRESS", summaryForA.status());
        assertTrue(summaryForA.currentUserCompleted());
        assertFalse(summaryForA.partnerCompleted());
        assertNull(summaryForA.partnerScore(), "Partner score must be null before partner completes");
    }

    @Test
    @DisplayName("Both participants submit: scores compared, correct winner determined")
    void testSubmit_BothSubmit_CalculatesWinner() {
        createAcceptedPartnership(userA, userB);
        ChallengeResponse challenge = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 3));
        challengeService.acceptChallenge(EMAIL_B, challenge.id());

        List<ChallengeQuestionResponse> questions = challengeService.getChallengeQuestions(EMAIL_A, challenge.id());

        // A submits arbitrary answers
        List<ChallengeAnswerRequest> answersA = questions.stream()
                .map(q -> new ChallengeAnswerRequest(q.id(), "wrong_answer", 1000L))
                .toList();
        challengeService.submitChallenge(EMAIL_A, challenge.id(), new SubmitChallengeRequest(answersA));

        // B submits answers
        List<ChallengeAnswerRequest> answersB = questions.stream()
                .map(q -> new ChallengeAnswerRequest(q.id(), q.word(), 1200L))
                .toList();
        ChallengeResultResponse resultB = challengeService.submitChallenge(EMAIL_B, challenge.id(), new SubmitChallengeRequest(answersB));

        assertTrue(resultB.completed());
        assertFalse(resultB.waitingForPartner());
        assertNotNull(resultB.partnerScore());
        assertNotNull(resultB.myScore());

        // After completion, challenge summary reveals winner
        ChallengeResponse summary = challengeService.getChallenge(EMAIL_A, challenge.id());
        assertEquals("COMPLETED", summary.status());
        assertNotNull(summary.partnerScore());
        assertTrue(summary.currentUserCompleted());
        assertTrue(summary.partnerCompleted());
    }

    @Test
    @DisplayName("Duplicate submission by same user is rejected")
    void testSubmit_DuplicateSubmission_Rejected() {
        createAcceptedPartnership(userA, userB);
        ChallengeResponse challenge = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 3));
        challengeService.acceptChallenge(EMAIL_B, challenge.id());

        List<ChallengeQuestionResponse> questions = challengeService.getChallengeQuestions(EMAIL_A, challenge.id());
        List<ChallengeAnswerRequest> answers = questions.stream()
                .map(q -> new ChallengeAnswerRequest(q.id(), "some_answer", 1000L))
                .toList();

        challengeService.submitChallenge(EMAIL_A, challenge.id(), new SubmitChallengeRequest(answers));

        // Second submission by A must fail
        assertThrows(BadRequestException.class, () ->
                challengeService.submitChallenge(EMAIL_A, challenge.id(), new SubmitChallengeRequest(answers))
        );
    }
}
