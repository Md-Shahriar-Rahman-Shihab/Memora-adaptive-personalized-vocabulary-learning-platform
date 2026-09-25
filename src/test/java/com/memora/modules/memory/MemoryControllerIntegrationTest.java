package com.memora.modules.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.dto.WordReviewRequest;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MemoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private UserWordProgressRepository userWordProgressRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private VocabularyWord wordEloquent;
    private VocabularyWord wordResilient;

    @BeforeEach
    void setUp() {
        userWordProgressRepository.deleteAll();
        vocabularyWordRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(new User("Shihab", "shihab@memora.com", "secretHash", VocabularyLevel.B1, Role.LEARNER));

        wordEloquent = vocabularyWordRepository.save(new VocabularyWord(
                "eloquent", "বাকপটু", "fluent or persuasive in speaking or writing",
                "/ˈel.ə.kwənt/", "An eloquent speech moved the audience.", DifficultyLevel.B2, WordCategory.ACADEMIC
        ));

        wordResilient = vocabularyWordRepository.save(new VocabularyWord(
                "resilient", "স্থিতিস্থাপক / কষ্টসহিষ্ণু", "able to withstand or recover quickly from difficult conditions",
                "/rɪˈzɪl.jənt/", "She is remarkably resilient in tough situations.", DifficultyLevel.B2, WordCategory.GENERAL
        ));
    }

    @Test
    @DisplayName("POST /api/v1/memory/review without authentication should return 401 Unauthorized")
    void reviewWithoutAuthReturns401() throws Exception {
        WordReviewRequest request = new WordReviewRequest(wordEloquent.getId(), true, 1500L, MemoryAlgorithmType.SM2);

        mockMvc.perform(post("/api/v1/memory/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @WithMockUser(username = "shihab@memora.com", roles = {"LEARNER"})
    @DisplayName("POST /api/v1/memory/review with SM2 should update progress and return 200 OK")
    void reviewWithSM2ReturnsOk() throws Exception {
        WordReviewRequest request = new WordReviewRequest(wordEloquent.getId(), true, 1600L, MemoryAlgorithmType.SM2);

        mockMvc.perform(post("/api/v1/memory/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.wordId", is(wordEloquent.getId().intValue())))
                .andExpect(jsonPath("$.data.word", is("eloquent")))
                .andExpect(jsonPath("$.data.correct", is(true)))
                .andExpect(jsonPath("$.data.algorithm", is("SM2")))
                .andExpect(jsonPath("$.data.reviewIntervalDays", is(1)))
                .andExpect(jsonPath("$.data.masteryScore", greaterThan(0.0)))
                .andExpect(jsonPath("$.data.nextReviewAt", notNullValue()))
                .andExpect(jsonPath("$.data.xpEarned", is(5)));
    }

    @Test
    @WithMockUser(username = "shihab@memora.com", roles = {"LEARNER"})
    @DisplayName("POST /api/v1/memory/review with LEITNER should promote box and return 200 OK")
    void reviewWithLeitnerReturnsOk() throws Exception {
        WordReviewRequest request = new WordReviewRequest(wordResilient.getId(), true, 1400L, MemoryAlgorithmType.LEITNER);

        mockMvc.perform(post("/api/v1/memory/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.wordId", is(wordResilient.getId().intValue())))
                .andExpect(jsonPath("$.data.word", is("resilient")))
                .andExpect(jsonPath("$.data.correct", is(true)))
                .andExpect(jsonPath("$.data.algorithm", is("LEITNER")))
                .andExpect(jsonPath("$.data.reviewIntervalDays", is(3))); // Promoted from Box 1 -> Box 2 (3 days)
    }

    @Test
    @WithMockUser(username = "shihab@memora.com", roles = {"LEARNER"})
    @DisplayName("POST /api/v1/memory/review with invalid request body should return 400 Bad Request")
    void reviewWithInvalidPayloadReturns400() throws Exception {
        WordReviewRequest invalidRequest = new WordReviewRequest(null, null, -500L, null);

        mockMvc.perform(post("/api/v1/memory/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @WithMockUser(username = "shihab@memora.com", roles = {"LEARNER"})
    @DisplayName("POST /api/v1/memory/review with non-existent word ID should return 404 Not Found")
    void reviewWithNonExistentWordReturns404() throws Exception {
        WordReviewRequest request = new WordReviewRequest(9999L, true, 1500L, MemoryAlgorithmType.SM2);

        mockMvc.perform(post("/api/v1/memory/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)));
    }

    @Test
    @WithMockUser(username = "shihab@memora.com", roles = {"LEARNER"})
    @DisplayName("GET /api/v1/memory/due should return words requiring review")
    void getDueReviewsReturnsDueList() throws Exception {
        UserWordProgress dueProgress = new UserWordProgress(testUser, wordEloquent);
        dueProgress.setNextReviewAt(Instant.now().minusSeconds(7200)); // Overdue
        userWordProgressRepository.save(dueProgress);

        UserWordProgress futureProgress = new UserWordProgress(testUser, wordResilient);
        futureProgress.setNextReviewAt(Instant.now().plusSeconds(86400)); // In future
        userWordProgressRepository.save(futureProgress);

        mockMvc.perform(get("/api/v1/memory/due"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].word", is("eloquent")))
                .andExpect(jsonPath("$.data[0].definition", is("fluent or persuasive in speaking or writing")))
                .andExpect(jsonPath("$.data[0].pronunciation", is("/ˈel.ə.kwənt/")))
                .andExpect(jsonPath("$.data[0].exampleSentence", is("An eloquent speech moved the audience.")));
    }

    @Test
    @WithMockUser(username = "shihab@memora.com", roles = {"LEARNER"})
    @DisplayName("GET /api/v1/memory/weak should return learner weak words")
    void getWeakWordsReturnsWeakList() throws Exception {
        UserWordProgress weakProgress = new UserWordProgress(testUser, wordEloquent);
        weakProgress.setForgettingRisk(ForgettingRisk.HIGH);
        weakProgress.setMasteryScore(20.0);
        weakProgress.setIncorrectAttempts(4);
        weakProgress.setCorrectAttempts(1);
        userWordProgressRepository.save(weakProgress);

        mockMvc.perform(get("/api/v1/memory/weak"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].word", is("eloquent")))
                .andExpect(jsonPath("$.data[0].forgettingRisk", is("HIGH")));
    }
}
