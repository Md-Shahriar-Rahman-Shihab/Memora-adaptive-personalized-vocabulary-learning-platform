package com.memora.modules.learningpath;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.assessment.repository.AssessmentAnswerRepository;
import com.memora.modules.assessment.repository.AssessmentQuestionRepository;
import com.memora.modules.assessment.repository.AssessmentRepository;
import com.memora.modules.learningpath.domain.LearningPathStatus;
import com.memora.modules.learningpath.dto.LearningItemCompletionRequest;
import com.memora.modules.learningpath.repository.LearningPathItemRepository;
import com.memora.modules.learningpath.repository.LearningPathRepository;
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
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LearningPathControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private LearningPathItemRepository learningPathItemRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private QuestionAttemptRepository questionAttemptRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private UserWordProgressRepository userWordProgressRepository;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testLearner;

    @BeforeEach
    void setUp() {
        cleanDatabase();

        testLearner = userRepository.save(new User("Shihab", "shihab@memora.com", "hash", VocabularyLevel.B1, Role.LEARNER));
        seedVocabulary();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        learningPathItemRepository.deleteAll();
        learningPathRepository.deleteAll();
        assessmentAnswerRepository.deleteAll();
        assessmentQuestionRepository.deleteAll();
        assessmentRepository.deleteAll();
        questionAttemptRepository.deleteAll();
        quizAttemptRepository.deleteAll();
        questionRepository.deleteAll();
        quizRepository.deleteAll();
        userWordProgressRepository.deleteAll();
        vocabularyWordRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void seedVocabulary() {
        List<VocabularyWord> words = new ArrayList<>();
        DifficultyLevel[] levels = {DifficultyLevel.A1, DifficultyLevel.A2, DifficultyLevel.B1, DifficultyLevel.B2, DifficultyLevel.C1};

        for (DifficultyLevel level : levels) {
            for (int i = 1; i <= 6; i++) {
                String text = level.name().toLowerCase() + "_lp_word_" + i;
                words.add(new VocabularyWord(
                        text,
                        "meaning_" + text,
                        "definition of " + text,
                        "/pron/",
                        "Example sentence with " + text + ".",
                        level,
                        WordCategory.GENERAL
                ));
            }
        }
        vocabularyWordRepository.saveAll(words);
    }

    @Test
    @DisplayName("Complete Learning Path Flow: Start -> Current -> Today -> Start Item -> Complete Item -> Regenerate -> History")
    @WithMockUser(username = "shihab@memora.com", roles = {"LEARNER"})
    void testCompleteLearningPathLifecycle() throws Exception {
        // 1. Start Learning Path
        MvcResult startResult = mockMvc.perform(post("/api/v1/learning-path/start"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")))
                .andExpect(jsonPath("$.data.targetLevel", is("B1")))
                .andExpect(jsonPath("$.data.totalItems", greaterThan(0)))
                .andReturn();

        JsonNode startData = objectMapper.readTree(startResult.getResponse().getContentAsString()).path("data");
        long pathId = startData.path("id").asLong();
        JsonNode items = startData.path("items");
        long firstItemId = items.get(0).path("id").asLong();

        // 2. Retrieve Current Path
        mockMvc.perform(get("/api/v1/learning-path/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is((int) pathId)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        // 3. Retrieve Today's Path
        mockMvc.perform(get("/api/v1/learning-path/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.learningPathId", is((int) pathId)))
                .andExpect(jsonPath("$.data.targetLevel", is("B1")))
                .andExpect(jsonPath("$.data.items", not(empty())));

        // 4. Start an Item
        mockMvc.perform(post("/api/v1/learning-path/items/" + firstItemId + "/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is((int) firstItemId)))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")));

        // 5. Complete an Item
        LearningItemCompletionRequest completeReq = new LearningItemCompletionRequest(true, 1400L, null, "Great job");
        mockMvc.perform(post("/api/v1/learning-path/items/" + firstItemId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(completeReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.itemId", is((int) firstItemId)))
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.completedItems", is(1)));

        // 6. Regenerate Path
        mockMvc.perform(post("/api/v1/learning-path/regenerate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is((int) pathId)))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));

        // 7. Get History
        mockMvc.perform(get("/api/v1/learning-path/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is((int) pathId)));
    }

    @Test
    @DisplayName("Unauthenticated requests should return 401 Unauthorized")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(post("/api/v1/learning-path/start"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/learning-path/today"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Complete item with invalid request payload should return 400 Bad Request")
    @WithMockUser(username = "shihab@memora.com", roles = {"LEARNER"})
    void testCompleteItemValidationFailure() throws Exception {
        LearningItemCompletionRequest invalidReq = new LearningItemCompletionRequest(true, -100L, null, "Note");
        mockMvc.perform(post("/api/v1/learning-path/items/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)));
    }
}
