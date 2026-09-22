package com.memora.modules.onboarding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.assessment.domain.AssessmentStatus;
import com.memora.modules.assessment.entity.Assessment;
import com.memora.modules.assessment.repository.AssessmentAnswerRepository;
import com.memora.modules.assessment.repository.AssessmentQuestionRepository;
import com.memora.modules.assessment.repository.AssessmentRepository;
import com.memora.modules.learningpath.entity.LearningPath;
import com.memora.modules.learningpath.repository.LearningPathItemRepository;
import com.memora.modules.learningpath.repository.LearningPathRepository;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OnboardingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Autowired
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Autowired
    private LearningPathRepository learningPathRepository;

    @Autowired
    private LearningPathItemRepository learningPathItemRepository;

    private User userA;
    private User userB;

    @BeforeEach
    void setUp() {
        cleanup();
        userA = userRepository.save(new User("Learner One", "userA@memora.com", "$2a$10$encodedHash", VocabularyLevel.A1, Role.LEARNER));
        userB = userRepository.save(new User("Learner Two", "userB@memora.com", "$2a$10$encodedHash", VocabularyLevel.A1, Role.LEARNER));
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        userRepository.findByEmail("userA@memora.com").ifPresent(u -> {
            learningPathItemRepository.deleteAll();
            learningPathRepository.deleteAll();
            assessmentAnswerRepository.deleteAll();
            assessmentQuestionRepository.deleteAll();
            assessmentRepository.deleteAll();
            userRepository.delete(u);
        });
        userRepository.findByEmail("userB@memora.com").ifPresent(userRepository::delete);
    }

    @Test
    @DisplayName("GET /api/v1/onboarding/state should return 401 when unauthenticated")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/onboarding/state"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/onboarding/state should return ONBOARDING_REQUIRED for brand-new user")
    @WithMockUser(username = "userA@memora.com", roles = {"LEARNER"})
    void shouldReturnOnboardingRequiredForBrandNewUser() throws Exception {
        mockMvc.perform(get("/api/v1/onboarding/state")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.state", is("ONBOARDING_REQUIRED")))
                .andExpect(jsonPath("$.data.assessmentId").doesNotExist())
                .andExpect(jsonPath("$.data.learningPathId").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/onboarding/state should return ASSESSMENT_IN_PROGRESS when user has in-progress assessment")
    @WithMockUser(username = "userA@memora.com", roles = {"LEARNER"})
    void shouldReturnAssessmentInProgress() throws Exception {
        Assessment assessment = new Assessment(userA, 20);
        assessment.setStatus(AssessmentStatus.IN_PROGRESS);
        Assessment saved = assessmentRepository.save(assessment);

        mockMvc.perform(get("/api/v1/onboarding/state")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.state", is("ASSESSMENT_IN_PROGRESS")))
                .andExpect(jsonPath("$.data.assessmentId", is(saved.getId().intValue())));
    }

    @Test
    @DisplayName("GET /api/v1/onboarding/state should return LEARNING_PATH_REQUIRED when assessment completed but no path exists")
    @WithMockUser(username = "userA@memora.com", roles = {"LEARNER"})
    void shouldReturnLearningPathRequired() throws Exception {
        Assessment assessment = new Assessment(userA, 20);
        assessment.setStatus(AssessmentStatus.COMPLETED);
        assessment.setEstimatedLevel(DifficultyLevel.B1);
        Assessment saved = assessmentRepository.save(assessment);

        mockMvc.perform(get("/api/v1/onboarding/state")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.state", is("LEARNING_PATH_REQUIRED")))
                .andExpect(jsonPath("$.data.assessmentId", is(saved.getId().intValue())));
    }

    @Test
    @DisplayName("GET /api/v1/onboarding/state should return LEARNING_ACTIVE when active learning path exists")
    @WithMockUser(username = "userA@memora.com", roles = {"LEARNER"})
    void shouldReturnLearningActiveWhenPathExists() throws Exception {
        LearningPath path = new LearningPath(userA, DifficultyLevel.B1, 1);
        LearningPath savedPath = learningPathRepository.save(path);

        mockMvc.perform(get("/api/v1/onboarding/state")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.state", is("LEARNING_ACTIVE")))
                .andExpect(jsonPath("$.data.learningPathId", is(savedPath.getId().intValue())));
    }

    @Test
    @DisplayName("GET /api/v1/onboarding/state should isolate users and not leak onboarding state across learners")
    @WithMockUser(username = "userB@memora.com", roles = {"LEARNER"})
    void shouldIsolateUserStates() throws Exception {
        // User A has an active learning path
        LearningPath pathA = new LearningPath(userA, DifficultyLevel.B2, 1);
        learningPathRepository.save(pathA);

        // User B has no records
        mockMvc.perform(get("/api/v1/onboarding/state")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.state", is("ONBOARDING_REQUIRED")))
                .andExpect(jsonPath("$.data.learningPathId").doesNotExist());
    }
}
