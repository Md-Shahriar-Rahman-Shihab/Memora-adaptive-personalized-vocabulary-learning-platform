package com.memora.modules.partner;

import com.memora.modules.partner.challenge.dto.ChallengeAnswerRequest;
import com.memora.modules.partner.challenge.dto.ChallengeQuestionResponse;
import com.memora.modules.partner.challenge.dto.ChallengeResponse;
import com.memora.modules.partner.challenge.dto.CreateChallengeRequest;
import com.memora.modules.partner.challenge.dto.SubmitChallengeRequest;
import com.memora.modules.partner.challenge.service.VocabularyChallengeService;
import com.memora.modules.partner.domain.PartnerActivityType;
import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.partner.dto.PartnerActivityResponse;
import com.memora.modules.partner.dto.PartnerLeaderboardEntryResponse;
import com.memora.modules.partner.dto.PartnerRequestResponse;
import com.memora.modules.partner.dto.PartnerUserSummaryResponse;
import com.memora.modules.partner.dto.SendPartnerRequestDto;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerActivityRepository;
import com.memora.modules.partner.repository.PartnerRelationshipRepository;
import com.memora.modules.partner.service.PartnerActivityService;
import com.memora.modules.partner.service.PartnerLeaderboardService;
import com.memora.modules.partner.service.PartnerService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class PartnerPhaseEThreeUserEndToEndTest {

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private PartnerLeaderboardService partnerLeaderboardService;

    @Autowired
    private PartnerActivityService partnerActivityService;

    @Autowired
    private VocabularyChallengeService vocabularyChallengeService;

    @Autowired
    private PartnerRelationshipRepository partnerRelationshipRepository;

    @Autowired
    private PartnerActivityRepository partnerActivityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private com.memora.modules.partner.challenge.repository.ChallengeAttemptRepository challengeAttemptRepository;

    @Autowired
    private com.memora.modules.partner.challenge.repository.VocabularyChallengeRepository vocabularyChallengeRepository;

    private User userA;
    private User userB;
    private User userC;

    private static final String EMAIL_A = "user.a.e2e@test.com";
    private static final String EMAIL_B = "user.b.e2e@test.com";
    private static final String EMAIL_C = "user.c.e2e@test.com";

    @BeforeEach
    void setUp() {
        cleanTestData();

        userA = userRepository.save(new User("User A", EMAIL_A, "pwdA", VocabularyLevel.B1, Role.LEARNER));
        userA.setXp(500);
        userA = userRepository.save(userA);

        userB = userRepository.save(new User("User B", EMAIL_B, "pwdB", VocabularyLevel.B2, Role.LEARNER));
        userB.setXp(1500);
        userB = userRepository.save(userB);

        userC = userRepository.save(new User("User C", EMAIL_C, "pwdC", VocabularyLevel.C1, Role.LEARNER));
        userC.setXp(3000);
        userC = userRepository.save(userC);

        seedWordsForChallenge();
    }

    @AfterEach
    void tearDown() {
        cleanTestData();
    }

    private void cleanTestData() {
        challengeAttemptRepository.deleteAll();
        vocabularyChallengeRepository.deleteAll();
        partnerActivityRepository.deleteAll();
        partnerRelationshipRepository.deleteAll();
        userRepository.findByEmail(EMAIL_A).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_B).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_C).ifPresent(userRepository::delete);
    }

    private void seedWordsForChallenge() {
        for (int i = 1; i <= 6; i++) {
            String wordText = "phase_e_word_" + i;
            if (!vocabularyWordRepository.existsByWordIgnoreCase(wordText)) {
                VocabularyWord word = new VocabularyWord(
                        wordText,
                        "Meaning " + i,
                        "Definition for " + wordText,
                        "/phonetic/",
                        "Example for " + wordText,
                        DifficultyLevel.B1,
                        WordCategory.ACADEMIC
                );
                vocabularyWordRepository.save(word);
            }
        }
    }

    @Test
    @DisplayName("29.1 Partner Search: A searches B by exact email, sends request, B accepts")
    void testPartnerSearchAndConnection_ThreeUsers() {
        // A searches B by exact email
        PartnerUserSummaryResponse searchResult = partnerService.searchByEmail(EMAIL_A, EMAIL_B);
        assertThat(searchResult.getId()).isEqualTo(userB.getId());
        assertThat(searchResult.getName()).isEqualTo("User B");
        assertThat(searchResult.getCurrentLevel()).isEqualTo("B2");
        assertThat(searchResult.getRelationshipStatus()).isEqualTo("NONE");

        // A sends request to B
        PartnerRequestResponse request = partnerService.sendPartnerRequest(EMAIL_A, new SendPartnerRequestDto(userB.getId()));
        assertThat(request.getStatus()).isEqualTo(PartnerRelationshipStatus.PENDING.name());

        // Verify status is now PENDING_SENT when A searches B again
        PartnerUserSummaryResponse afterRequestSearch = partnerService.searchByEmail(EMAIL_A, EMAIL_B);
        assertThat(afterRequestSearch.getRelationshipStatus()).contains("PENDING");

        // B accepts request from A
        partnerService.acceptRequest(EMAIL_B, request.getId());

        // Verify status is now ACCEPTED
        PartnerUserSummaryResponse afterAcceptSearch = partnerService.searchByEmail(EMAIL_A, EMAIL_B);
        assertThat(afterAcceptSearch.getRelationshipStatus()).isEqualTo("ACCEPTED");
    }


    @Test
    @DisplayName("29.2 Pair Leaderboard: A sees A & B, but NOT unrelated User C")
    void testPairLeaderboardIsolation_ThreeUsers() {
        // A and B connect
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.ACCEPTED));

        // A views pair leaderboard
        List<PartnerLeaderboardEntryResponse> aLeaderboard = partnerLeaderboardService.getPartnerLeaderboard(EMAIL_A);
        assertThat(aLeaderboard).hasSize(2);
        assertThat(aLeaderboard.get(0).getUserId()).isEqualTo(userB.getId()); // 1500 XP
        assertThat(aLeaderboard.get(0).getRank()).isEqualTo(1);
        assertThat(aLeaderboard.get(1).getUserId()).isEqualTo(userA.getId()); // 500 XP
        assertThat(aLeaderboard.get(1).getRank()).isEqualTo(2);

        // User C (unrelated) MUST NOT be present
        assertThat(aLeaderboard).noneMatch(entry -> entry.getUserId().equals(userC.getId()));

        // B views pair leaderboard
        List<PartnerLeaderboardEntryResponse> bLeaderboard = partnerLeaderboardService.getPartnerLeaderboard(EMAIL_B);
        assertThat(bLeaderboard).hasSize(2);
        assertThat(bLeaderboard).noneMatch(entry -> entry.getUserId().equals(userC.getId()));

        // C views pair leaderboard: has no accepted partners, so only sees self
        List<PartnerLeaderboardEntryResponse> cLeaderboard = partnerLeaderboardService.getPartnerLeaderboard(EMAIL_C);
        assertThat(cLeaderboard).hasSize(1);
        assertThat(cLeaderboard.get(0).getUserId()).isEqualTo(userC.getId());
    }

    @Test
    @DisplayName("29.3 Activity Feed Isolation: A and B see pair activities, C cannot see them")
    void testActivityPrivacyIsolation_ThreeUsers() {
        // A and B connect
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship relAB = partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.ACCEPTED));

        partnerActivityService.logActivity(relAB, userA, PartnerActivityType.PARTNER_CONNECTED, "User A connected with User B", null, 0);
        partnerActivityService.logActivity(relAB, userA, PartnerActivityType.CHALLENGE_CREATED, "User A challenged User B to a vocabulary duel", null, 0);

        // A sees both activities
        List<PartnerActivityResponse> aActivities = partnerActivityService.getPartnerActivities(EMAIL_A, 20);
        assertThat(aActivities).hasSize(2);
        assertThat(aActivities.get(0).getTitle()).isEqualTo("User A challenged User B to a vocabulary duel"); // newest first

        // B sees both activities
        List<PartnerActivityResponse> bActivities = partnerActivityService.getPartnerActivities(EMAIL_B, 20);
        assertThat(bActivities).hasSize(2);

        // C has no accepted partner with A or B, so C sees ZERO activities
        List<PartnerActivityResponse> cActivities = partnerActivityService.getPartnerActivities(EMAIL_C, 20);
        assertThat(cActivities).isEmpty();
    }

    @Test
    @DisplayName("29.4 Phase D Challenge Still Works & Activity Integration with Idempotency")
    void testChallengeLifecycleAndActivityIdempotency_ThreeUsers() {
        // A and B connect
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship relAB = partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.ACCEPTED));

        // A creates challenge for B
        ChallengeResponse created = vocabularyChallengeService.createChallenge(
                EMAIL_A,
                new CreateChallengeRequest(userB.getId(), "B1", 5)
        );
        assertThat(created).isNotNull();

        // Check activity logged for CHALLENGE_CREATED
        List<PartnerActivityResponse> actAfterCreate = partnerActivityService.getPartnerActivities(EMAIL_A, 20);
        assertThat(actAfterCreate).anyMatch(a -> a.getActivityType().equals(PartnerActivityType.CHALLENGE_CREATED.name()));

        // B accepts challenge
        ChallengeResponse accepted = vocabularyChallengeService.acceptChallenge(EMAIL_B, created.id());
        assertThat(accepted).isNotNull();

        // Check activity logged for CHALLENGE_ACCEPTED
        List<PartnerActivityResponse> actAfterAccept = partnerActivityService.getPartnerActivities(EMAIL_A, 20);
        assertThat(actAfterAccept).anyMatch(a -> a.getActivityType().equals(PartnerActivityType.CHALLENGE_ACCEPTED.name()));

        // Retrieve deterministic questions
        List<ChallengeQuestionResponse> questions = vocabularyChallengeService.getChallengeQuestions(EMAIL_A, created.id());
        assertThat(questions).hasSize(5);

        // A submits answers
        List<ChallengeAnswerRequest> aAnswers = questions.stream()
                .map(q -> new ChallengeAnswerRequest(q.id(), "some_answer", 1500L))
                .toList();
        vocabularyChallengeService.submitChallenge(EMAIL_A, created.id(), new SubmitChallengeRequest(aAnswers));

        // B submits answers
        List<ChallengeAnswerRequest> bAnswers = questions.stream()
                .map(q -> new ChallengeAnswerRequest(q.id(), "some_other_answer", 1800L))
                .toList();
        vocabularyChallengeService.submitChallenge(EMAIL_B, created.id(), new SubmitChallengeRequest(bAnswers));

        // Check activity logged for match completion (draw or win)
        List<PartnerActivityResponse> finalActivities = partnerActivityService.getPartnerActivities(EMAIL_A, 20);
        assertThat(finalActivities).anyMatch(a ->
                a.getActivityType().equals(PartnerActivityType.CHALLENGE_COMPLETED.name()) ||
                a.getActivityType().equals(PartnerActivityType.CHALLENGE_WON.name()) ||
                a.getActivityType().equals(PartnerActivityType.CHALLENGE_DRAW.name())
        );

        int countBeforeDuplicate = finalActivities.size();

        // Verify duplicate submission is rejected (Phase D idempotency)
        assertThatThrownBy(() -> vocabularyChallengeService.submitChallenge(EMAIL_B, created.id(), new SubmitChallengeRequest(bAnswers)))
                .isInstanceOf(RuntimeException.class);

        // Verify no duplicate activity was created
        List<PartnerActivityResponse> actAfterDuplicate = partnerActivityService.getPartnerActivities(EMAIL_A, 20);
        assertThat(actAfterDuplicate).hasSize(countBeforeDuplicate);
    }
}
