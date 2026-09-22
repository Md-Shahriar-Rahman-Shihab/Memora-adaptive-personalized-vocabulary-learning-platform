package com.memora.modules.onboarding;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.assessment.domain.AssessmentStatus;
import com.memora.modules.assessment.entity.Assessment;
import com.memora.modules.assessment.repository.AssessmentRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.learningpath.entity.LearningPath;
import com.memora.modules.learningpath.repository.LearningPathRepository;
import com.memora.modules.onboarding.domain.OnboardingState;
import com.memora.modules.onboarding.dto.OnboardingStateResponse;
import com.memora.modules.onboarding.service.OnboardingServiceImpl;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private LearningPathRepository learningPathRepository;

    private OnboardingServiceImpl onboardingService;

    private User testUser;

    @BeforeEach
    void setUp() {
        onboardingService = new OnboardingServiceImpl(userRepository, assessmentRepository, learningPathRepository);
        testUser = new User("Alice", "alice@example.com", "hash", VocabularyLevel.A1, Role.LEARNER);
        ReflectionTestUtils.setField(testUser, "id", 100L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user is not found")
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                onboardingService.getOnboardingState("unknown@example.com"));
    }

    @Test
    @DisplayName("Should return ONBOARDING_REQUIRED for brand-new learner with no records")
    void shouldReturnOnboardingRequiredForBrandNewLearner() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(assessmentRepository.findActiveAssessment(100L)).thenReturn(Optional.empty());
        when(learningPathRepository.findActivePathByUserId(100L)).thenReturn(Optional.empty());
        when(learningPathRepository.findByUserIdOrderByCreatedAtDesc(100L)).thenReturn(Collections.emptyList());
        when(assessmentRepository.findByUserIdOrderByStartedAtDesc(100L)).thenReturn(Collections.emptyList());

        OnboardingStateResponse response = onboardingService.getOnboardingState(testUser.getEmail());

        assertNotNull(response);
        assertEquals(OnboardingState.ONBOARDING_REQUIRED, response.getState());
        assertNull(response.getAssessmentId());
        assertNull(response.getLearningPathId());
    }

    @Test
    @DisplayName("Should return ASSESSMENT_IN_PROGRESS when learner has an active assessment")
    void shouldReturnAssessmentInProgress() {
        Assessment assessment = new Assessment(testUser, 20);
        ReflectionTestUtils.setField(assessment, "id", 201L);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(assessmentRepository.findActiveAssessment(100L)).thenReturn(Optional.of(assessment));

        OnboardingStateResponse response = onboardingService.getOnboardingState(testUser.getEmail());

        assertNotNull(response);
        assertEquals(OnboardingState.ASSESSMENT_IN_PROGRESS, response.getState());
        assertEquals(201L, response.getAssessmentId());
        assertNull(response.getLearningPathId());
    }

    @Test
    @DisplayName("Should return LEARNING_ACTIVE when learner has an active learning path")
    void shouldReturnLearningActiveWhenActivePathExists() {
        LearningPath path = new LearningPath(testUser, com.memora.modules.vocabulary.domain.DifficultyLevel.A1, 1);
        ReflectionTestUtils.setField(path, "id", 301L);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(assessmentRepository.findActiveAssessment(100L)).thenReturn(Optional.empty());
        when(learningPathRepository.findActivePathByUserId(100L)).thenReturn(Optional.of(path));

        OnboardingStateResponse response = onboardingService.getOnboardingState(testUser.getEmail());

        assertNotNull(response);
        assertEquals(OnboardingState.LEARNING_ACTIVE, response.getState());
        assertNull(response.getAssessmentId());
        assertEquals(301L, response.getLearningPathId());
    }

    @Test
    @DisplayName("Should return LEARNING_ACTIVE when learner has historical learning path")
    void shouldReturnLearningActiveWhenHistoricalPathExists() {
        LearningPath path = new LearningPath(testUser, com.memora.modules.vocabulary.domain.DifficultyLevel.A2, 1);
        ReflectionTestUtils.setField(path, "id", 302L);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(assessmentRepository.findActiveAssessment(100L)).thenReturn(Optional.empty());
        when(learningPathRepository.findActivePathByUserId(100L)).thenReturn(Optional.empty());
        when(learningPathRepository.findByUserIdOrderByCreatedAtDesc(100L)).thenReturn(List.of(path));

        OnboardingStateResponse response = onboardingService.getOnboardingState(testUser.getEmail());

        assertNotNull(response);
        assertEquals(OnboardingState.LEARNING_ACTIVE, response.getState());
        assertNull(response.getAssessmentId());
        assertEquals(302L, response.getLearningPathId());
    }

    @Test
    @DisplayName("Should return LEARNING_PATH_REQUIRED when assessment is completed but learning path does not exist")
    void shouldReturnLearningPathRequiredWhenAssessmentCompleted() {
        Assessment completed = new Assessment(testUser, 20);
        ReflectionTestUtils.setField(completed, "id", 202L);
        completed.setStatus(AssessmentStatus.COMPLETED);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(assessmentRepository.findActiveAssessment(100L)).thenReturn(Optional.empty());
        when(learningPathRepository.findActivePathByUserId(100L)).thenReturn(Optional.empty());
        when(learningPathRepository.findByUserIdOrderByCreatedAtDesc(100L)).thenReturn(Collections.emptyList());
        when(assessmentRepository.findByUserIdOrderByStartedAtDesc(100L)).thenReturn(List.of(completed));

        OnboardingStateResponse response = onboardingService.getOnboardingState(testUser.getEmail());

        assertNotNull(response);
        assertEquals(OnboardingState.LEARNING_PATH_REQUIRED, response.getState());
        assertEquals(202L, response.getAssessmentId());
        assertNull(response.getLearningPathId());
    }
}
