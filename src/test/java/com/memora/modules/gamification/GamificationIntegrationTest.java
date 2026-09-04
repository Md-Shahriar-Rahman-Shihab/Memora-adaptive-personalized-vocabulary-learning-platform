package com.memora.modules.gamification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.assessment.repository.AssessmentAnswerRepository;
import com.memora.modules.assessment.repository.AssessmentQuestionRepository;
import com.memora.modules.assessment.repository.AssessmentRepository;
import com.memora.modules.gamification.config.AchievementDataSeeder;
import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import com.memora.modules.gamification.entity.Achievement;
import com.memora.modules.gamification.entity.UserGamificationProfile;
import com.memora.modules.gamification.repository.AchievementRepository;
import com.memora.modules.gamification.repository.UserAchievementRepository;
import com.memora.modules.gamification.repository.UserGamificationProfileRepository;
import com.memora.modules.gamification.repository.XpTransactionRepository;
import com.memora.modules.gamification.service.GamificationService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GamificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGamificationProfileRepository profileRepository;

    @Autowired
    private XpTransactionRepository xpTransactionRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private AchievementDataSeeder achievementDataSeeder;

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private LearningPathItemRepository learningPathItemRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuestionAttemptRepository questionAttemptRepository;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private UserWordProgressRepository userWordProgressRepository;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        cleanDatabase();
        achievementDataSeeder.seedAchievements();

        testUser = userRepository.save(new User("Tariq", "tariq@memora.com", "hash", VocabularyLevel.B1, Role.LEARNER));
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
        userAchievementRepository.deleteAll();
        xpTransactionRepository.deleteAll();
        profileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Complete Gamification Flow: record activity -> profile -> xp-history -> achievements -> leaderboard")
    @WithMockUser(username = "tariq@memora.com", roles = {"LEARNER"})
    void testCompleteGamificationFlow() throws Exception {
        // 1. Record activity: correct quiz answer (+10 XP, streak 1)
        gamificationService.recordActivity(testUser, RewardActivityType.QUIZ, RewardContext.forQuizAnswer(1L, true));

        // 2. GET /api/v1/profile
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Tariq")))
                .andExpect(jsonPath("$.data.level", is("B1")))
                .andExpect(jsonPath("$.data.xp", is(10)))
                .andExpect(jsonPath("$.data.currentStreak", is(1)))
                .andExpect(jsonPath("$.data.longestStreak", is(1)))
                .andExpect(jsonPath("$.data.rank", is(1)));

        // 3. GET /api/v1/profile/xp-history
        mockMvc.perform(get("/api/v1/profile/xp-history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].amount", is(10)))
                .andExpect(jsonPath("$.data[0].activityType", is("QUIZ")));

        // 4. GET /api/v1/profile/stats
        mockMvc.perform(get("/api/v1/profile/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalXp", is(10)))
                .andExpect(jsonPath("$.data.currentStreak", is(1)));

        // 5. GET /api/v1/leaderboard (public)
        mockMvc.perform(get("/api/v1/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].displayName", is("Tariq")))
                .andExpect(jsonPath("$.data[0].xp", is(10)))
                .andExpect(jsonPath("$.data[0].level", is("B1")));

        // 6. GET /api/v1/achievements (public catalog)
        mockMvc.perform(get("/api/v1/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(10))));
    }

    @Test
    @DisplayName("Unauthenticated request to /api/v1/profile should return 401 Unauthorized")
    void testUnauthenticatedProfile() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/profile/xp-history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Public endpoints /api/v1/leaderboard and /api/v1/achievements should be accessible without token")
    void testPublicEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        mockMvc.perform(get("/api/v1/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
}
