package com.memora.modules.assessment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.assessment.domain.AssessmentStatus;
import com.memora.modules.assessment.dto.AssessmentAnswerRequest;
import com.memora.modules.assessment.repository.AssessmentAnswerRepository;
import com.memora.modules.assessment.repository.AssessmentQuestionRepository;
import com.memora.modules.assessment.repository.AssessmentRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AssessmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuestionAttemptRepository questionAttemptRepository;

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

        testLearner = userRepository.save(new User("Tariq", "tariq@memora.com", "hash", VocabularyLevel.A1, Role.LEARNER));

        // Ensure each level has at least 4 words in DB for assessment generation
        seedCuratedVocabulary();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
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

    private void seedCuratedVocabulary() {
        List<VocabularyWord> words = new ArrayList<>();
        DifficultyLevel[] levels = {DifficultyLevel.A1, DifficultyLevel.A2, DifficultyLevel.B1, DifficultyLevel.B2, DifficultyLevel.C1};

        for (DifficultyLevel level : levels) {
            for (int i = 1; i <= 4; i++) {
                String wordText = level.name().toLowerCase() + "_word_" + i;
                words.add(new VocabularyWord(
                        wordText,
                        "meaning_" + wordText,
                        "definition for " + wordText,
                        "/pron/",
                        "Example sentence containing " + wordText + ".",
                        level,
                        WordCategory.GENERAL
                ));
            }
        }
        vocabularyWordRepository.saveAll(words);
    }

    @Test
    @DisplayName("Complete Assessment Flow: Start -> Details -> Answer All -> Complete -> Result -> History")
    @WithMockUser(username = "tariq@memora.com", roles = {"LEARNER"})
    void testCompleteAssessmentLifecycle() throws Exception {
        // 1. Start Assessment
        MvcResult startResult = mockMvc.perform(post("/api/v1/assessments/start"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.totalQuestions", is(20)))
                .andExpect(jsonPath("$.data.questions", hasSize(20)))
                .andReturn();

        JsonNode startData = objectMapper.readTree(startResult.getResponse().getContentAsString()).path("data");
        long assessmentId = startData.path("assessmentId").asLong();
        JsonNode questionsJson = startData.path("questions");

        // 2. Retrieve Assessment Details
        mockMvc.perform(get("/api/v1/assessments/" + assessmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.assessmentId", is((int) assessmentId)))
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.totalQuestions", is(20)))
                .andExpect(jsonPath("$.data.answeredQuestions", is(0)))
                .andExpect(jsonPath("$.data.remainingQuestions", is(20)));

        // 3. Submit Answers for all 20 questions
        for (int i = 0; i < questionsJson.size(); i++) {
            JsonNode qNode = questionsJson.get(i);
            long questionId = qNode.path("questionId").asLong();
            String level = qNode.path("difficultyLevel").asText();

            // Answer correctly for A1, A2, B1, and incorrectly for B2, C1
            String submittedAnswer;
            if ("A1".equals(level) || "A2".equals(level) || "B1".equals(level)) {
                // Look up question entity to get correct answer
                var qEntity = questionRepository.findById(questionId).orElseThrow();
                if (qEntity instanceof com.memora.modules.quiz.entity.MultipleChoiceQuestion mcq) {
                    submittedAnswer = mcq.getCorrectOption();
                } else if (qEntity instanceof com.memora.modules.quiz.entity.TranslationQuestion tq) {
                    submittedAnswer = tq.getExpectedAnswer();
                } else if (qEntity instanceof com.memora.modules.quiz.entity.FillInTheBlankQuestion fib) {
                    submittedAnswer = fib.getExpectedAnswer();
                } else {
                    submittedAnswer = "correct";
                }
            } else {
                submittedAnswer = "deliberately incorrect answer";
            }

            AssessmentAnswerRequest answerReq = new AssessmentAnswerRequest(submittedAnswer, 1500L);

            mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/questions/" + questionId + "/answer")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answerReq)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.responseTimeMs", is(1500)));
        }

        // Verify all 20 questions are now answered
        mockMvc.perform(get("/api/v1/assessments/" + assessmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answeredQuestions", is(20)))
                .andExpect(jsonPath("$.data.remainingQuestions", is(0)));

        // 4. Complete Assessment
        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.assessmentId", is((int) assessmentId)))
                .andExpect(jsonPath("$.data.estimatedLevel", is("B1")))
                .andExpect(jsonPath("$.data.totalQuestions", is(20)))
                .andExpect(jsonPath("$.data.correctAnswers", is(12)))
                .andExpect(jsonPath("$.data.accuracy", is(60.0)))
                .andExpect(jsonPath("$.data.confidenceScore", greaterThan(50.0)));

        // 5. Get Assessment Result
        mockMvc.perform(get("/api/v1/assessments/" + assessmentId + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.estimatedLevel", is("B1")));

        // 6. Get Assessment History
        mockMvc.perform(get("/api/v1/assessments/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].assessmentId", is((int) assessmentId)))
                .andExpect(jsonPath("$.data[0].estimatedLevel", is("B1")));

        // 7. Verify user's current level in database was updated
        User updatedLearner = userRepository.findByEmail("tariq@memora.com").orElseThrow();
        assertEquals(VocabularyLevel.B1, updatedLearner.getCurrentLevel());
    }

    @Test
    @DisplayName("Unauthenticated request to Assessment endpoints should return 401 Unauthorized")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(post("/api/v1/assessments/start"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/assessments/1"))
                .andExpect(status().isUnauthorized());
    }
}
