package com.memora.modules.partner.challenge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.partner.challenge.domain.ChallengeStatus;
import com.memora.modules.partner.challenge.dto.ChallengeAnswerRequest;
import com.memora.modules.partner.challenge.dto.CreateChallengeRequest;
import com.memora.modules.partner.challenge.dto.SubmitChallengeRequest;
import com.memora.modules.partner.challenge.entity.VocabularyChallenge;
import com.memora.modules.partner.challenge.repository.ChallengeAttemptRepository;
import com.memora.modules.partner.challenge.repository.VocabularyChallengeRepository;
import com.memora.modules.partner.challenge.service.VocabularyChallengeService;
import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerRelationshipRepository;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VocabularyChallengeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    private ObjectMapper objectMapper;

    private User userA;
    private User userB;
    private User userC;

    private static final String EMAIL_A = "alice.chctrl@test.com";
    private static final String EMAIL_B = "bob.chctrl@test.com";
    private static final String EMAIL_C = "charlie.chctrl@test.com";

    @BeforeEach
    void setUp() {
        cleanTestData();

        userA = userRepository.save(new User("Alice ChallengeCtrl", EMAIL_A, "hash1", VocabularyLevel.B1, Role.LEARNER));
        userB = userRepository.save(new User("Bob ChallengeCtrl", EMAIL_B, "hash2", VocabularyLevel.B1, Role.LEARNER));
        userC = userRepository.save(new User("Charlie ChallengeCtrl", EMAIL_C, "hash3", VocabularyLevel.B1, Role.LEARNER));

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

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("POST /api/v1/partners/challenges creates challenge (201 Created)")
    void testCreateChallenge_Success() throws Exception {
        createAcceptedPartnership(userA, userB);

        CreateChallengeRequest request = new CreateChallengeRequest(userB.getId(), "B1", 5);

        mockMvc.perform(post("/api/v1/partners/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andExpect(jsonPath("$.data.challengerId", is(userA.getId().intValue())))
                .andExpect(jsonPath("$.data.challengedUserId", is(userB.getId().intValue())))
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.token").doesNotExist());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("POST /api/v1/partners/challenges with unrelated user returns 403 Forbidden")
    void testCreateChallenge_UnrelatedUser_Forbidden() throws Exception {
        CreateChallengeRequest request = new CreateChallengeRequest(userC.getId(), "B1", 5);

        mockMvc.perform(post("/api/v1/partners/challenges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMAIL_B)
    @DisplayName("POST /api/v1/partners/challenges/{id}/accept accepts challenge (200 OK)")
    void testAcceptChallenge_Success() throws Exception {
        createAcceptedPartnership(userA, userB);
        var created = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 5));

        mockMvc.perform(post("/api/v1/partners/challenges/" + created.id() + "/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("ACCEPTED")));
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("POST /api/v1/partners/challenges/{id}/accept by challenger returns 403 Forbidden")
    void testAcceptChallenge_ByChallenger_Forbidden() throws Exception {
        createAcceptedPartnership(userA, userB);
        var created = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 5));

        mockMvc.perform(post("/api/v1/partners/challenges/" + created.id() + "/accept"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("GET /api/v1/partners/challenges/{id}/questions returns questions without answers")
    void testGetChallengeQuestions_PrivacySafe() throws Exception {
        createAcceptedPartnership(userA, userB);
        var created = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 5));
        challengeService.acceptChallenge(EMAIL_B, created.id());

        mockMvc.perform(get("/api/v1/partners/challenges/" + created.id() + "/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.data[0].id").isNumber())
                .andExpect(jsonPath("$.data[0].questionText").isString())
                .andExpect(jsonPath("$.data[0].correctOption").doesNotExist())
                .andExpect(jsonPath("$.data[0].expectedAnswer").doesNotExist());
    }

    @Test
    @WithMockUser(username = EMAIL_C)
    @DisplayName("GET /api/v1/partners/challenges/{id}/questions by unrelated user returns 403 Forbidden")
    void testGetChallengeQuestions_UnrelatedUser_Forbidden() throws Exception {
        createAcceptedPartnership(userA, userB);
        var created = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 5));
        challengeService.acceptChallenge(EMAIL_B, created.id());

        mockMvc.perform(get("/api/v1/partners/challenges/" + created.id() + "/questions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("End-to-end challenge lifecycle and anti-cheating check")
    void testEndToEnd_LifecycleAndPrivacy() throws Exception {
        createAcceptedPartnership(userA, userB);
        var created = challengeService.createChallenge(EMAIL_A, new CreateChallengeRequest(userB.getId(), "B1", 3));
        challengeService.acceptChallenge(EMAIL_B, created.id());

        var questions = challengeService.getChallengeQuestions(EMAIL_A, created.id());
        List<ChallengeAnswerRequest> answers = questions.stream()
                .map(q -> new ChallengeAnswerRequest(q.id(), "lucid", 1000L))
                .toList();

        // Alice submits
        SubmitChallengeRequest submitReq = new SubmitChallengeRequest(answers);
        mockMvc.perform(post("/api/v1/partners/challenges/" + created.id() + "/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.waitingForPartner", is(true)))
                .andExpect(jsonPath("$.data.completed", is(false)))
                .andExpect(jsonPath("$.data.partnerScore").doesNotExist())
                .andExpect(jsonPath("$.data.winnerId").doesNotExist());

        // Check get challenge for Alice - partner score must be null
        mockMvc.perform(get("/api/v1/partners/challenges/" + created.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentUserCompleted", is(true)))
                .andExpect(jsonPath("$.data.partnerCompleted", is(false)))
                .andExpect(jsonPath("$.data.partnerScore").doesNotExist())
                .andExpect(jsonPath("$.data.winnerId").doesNotExist());
    }
}
