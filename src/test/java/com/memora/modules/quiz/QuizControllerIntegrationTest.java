package com.memora.modules.quiz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.dto.AnswerSubmissionRequest;
import com.memora.modules.quiz.dto.QuizGenerationRequest;
import com.memora.modules.quiz.repository.QuestionAttemptRepository;
import com.memora.modules.quiz.repository.QuestionRepository;
import com.memora.modules.quiz.repository.QuizAttemptRepository;
import com.memora.modules.quiz.repository.QuizRepository;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class QuizControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private UserWordProgressRepository userWordProgressRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuestionAttemptRepository questionAttemptRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private VocabularyWord wordSerene;
    private VocabularyWord wordVibrant;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        testUser = userRepository.save(new User("Tariq", "tariq@memora.com", "hash", VocabularyLevel.A1, Role.LEARNER));

        wordSerene = vocabularyWordRepository.save(new VocabularyWord(
                "serene", "calm and peaceful", "calm, peaceful, and untroubled",
                "/səˈriːn/", "The lake was serene in the morning.", DifficultyLevel.A1, WordCategory.GENERAL
        ));

        wordVibrant = vocabularyWordRepository.save(new VocabularyWord(
                "vibrant", "full of energy", "full of energy and enthusiasm",
                "/ˈvaɪ.brənt/", "A vibrant city with rich culture.", DifficultyLevel.A1, WordCategory.GENERAL
        ));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        questionAttemptRepository.deleteAll();
        quizAttemptRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        userWordProgressRepository.deleteAll();
        vocabularyWordRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("End-to-End Quiz Flow: Generate -> Start -> Answer (Correct & Incorrect) -> Complete -> Verify Retention Update")
    @WithMockUser(username = "tariq@memora.com", roles = {"LEARNER"})
    void fullQuizWorkflowWithMemoryIntegration() throws Exception {
        // 1. Generate Quiz
        QuizGenerationRequest genRequest = new QuizGenerationRequest(DifficultyLevel.A1, 2, List.of(QuestionType.TRANSLATION));
        MvcResult genResult = mockMvc.perform(post("/api/v1/quizzes/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(genRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.questionCount", is(2)))
                .andExpect(jsonPath("$.data.questions", hasSize(2)))
                .andReturn();

        JsonNode quizJson = objectMapper.readTree(genResult.getResponse().getContentAsString()).path("data");
        long quizId = quizJson.path("id").asLong();
        long q1Id = quizJson.path("questions").get(0).path("id").asLong();
        String q1Word = quizJson.path("questions").get(0).path("word").asText();
        long q2Id = quizJson.path("questions").get(1).path("id").asLong();
        String q2Word = quizJson.path("questions").get(1).path("word").asText();

        // 2. Start Quiz
        mockMvc.perform(post("/api/v1/quizzes/" + quizId + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is((int) quizId)));

        // 3. Submit Correct Answer for Q1
        VocabularyWord word1 = vocabularyWordRepository.findByWordIgnoreCase(q1Word).orElseThrow();
        AnswerSubmissionRequest ans1 = new AnswerSubmissionRequest(q1Id, word1.getMeaning(), 1500L);

        mockMvc.perform(post("/api/v1/quizzes/" + quizId + "/questions/" + q1Id + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ans1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.correct", is(true)))
                .andExpect(jsonPath("$.data.score", is(10)))
                .andExpect(jsonPath("$.data.masteryScore", greaterThan(0.0)))
                .andExpect(jsonPath("$.data.nextReviewAt", notNullValue()));

        // 4. Submit Incorrect Answer for Q2
        VocabularyWord word2 = vocabularyWordRepository.findByWordIgnoreCase(q2Word).orElseThrow();
        AnswerSubmissionRequest ans2 = new AnswerSubmissionRequest(q2Id, "completely incorrect answer xyz", 2000L);

        mockMvc.perform(post("/api/v1/quizzes/" + quizId + "/questions/" + q2Id + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ans2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.correct", is(false)))
                .andExpect(jsonPath("$.data.score", is(0)))
                .andExpect(jsonPath("$.data.feedback", containsString("Incorrect")));

        // 5. Complete Quiz
        mockMvc.perform(post("/api/v1/quizzes/" + quizId + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.quizId", is((int) quizId)))
                .andExpect(jsonPath("$.data.totalQuestions", is(2)))
                .andExpect(jsonPath("$.data.correctAnswers", is(1)))
                .andExpect(jsonPath("$.data.totalScore", is(10)))
                .andExpect(jsonPath("$.data.percentage", is(50.0)));

        // 6. Get Quiz Result
        mockMvc.perform(get("/api/v1/quizzes/" + quizId + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.percentage", is(50.0)));

        // 7. Verify UserWordProgress in Database was updated by MemoryService
        User updatedUser = userRepository.findByEmail("tariq@memora.com").orElseThrow();

        UserWordProgress correctProgress = userWordProgressRepository
                .findByUserIdAndVocabularyWordId(updatedUser.getId(), word1.getId())
                .orElseThrow();
        assertEquals(1, correctProgress.getCorrectAttempts());
        assertTrue(correctProgress.getMasteryScore() > 0.0);
        assertNotNull(correctProgress.getNextReviewAt());

        UserWordProgress incorrectProgress = userWordProgressRepository
                .findByUserIdAndVocabularyWordId(updatedUser.getId(), word2.getId())
                .orElseThrow();
        assertEquals(1, incorrectProgress.getIncorrectAttempts());
    }

    @Test
    @DisplayName("Unauthenticated request to Quiz API should return 401 Unauthorized")
    void unauthenticatedQuizAccess() throws Exception {
        mockMvc.perform(post("/api/v1/quizzes/1/start"))
                .andExpect(status().isUnauthorized());
    }
}
