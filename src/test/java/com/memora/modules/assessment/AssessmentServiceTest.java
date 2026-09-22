package com.memora.modules.assessment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.assessment.domain.AssessmentPerformance;
import com.memora.modules.assessment.domain.AssessmentStatus;
import com.memora.modules.assessment.domain.PlacementResult;
import com.memora.modules.assessment.dto.AssessmentAnswerRequest;
import com.memora.modules.assessment.dto.AssessmentAnswerResponse;
import com.memora.modules.assessment.dto.AssessmentDetailResponse;
import com.memora.modules.assessment.dto.AssessmentStartResponse;
import com.memora.modules.assessment.dto.PlacementResultResponse;
import com.memora.modules.assessment.entity.Assessment;
import com.memora.modules.assessment.entity.AssessmentAnswer;
import com.memora.modules.assessment.entity.AssessmentQuestion;
import com.memora.modules.assessment.factory.PlacementStrategyFactory;
import com.memora.modules.assessment.repository.AssessmentAnswerRepository;
import com.memora.modules.assessment.repository.AssessmentQuestionRepository;
import com.memora.modules.assessment.repository.AssessmentRepository;
import com.memora.modules.assessment.service.AssessmentQuestionGenerator;
import com.memora.modules.assessment.service.AssessmentServiceImpl;
import com.memora.modules.assessment.strategy.DefaultPlacementStrategy;
import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.dto.EvaluationResult;
import com.memora.modules.quiz.entity.Question;
import com.memora.modules.quiz.entity.TranslationQuestion;
import com.memora.modules.quiz.factory.QuestionEvaluatorFactory;
import com.memora.modules.quiz.strategy.QuestionEvaluatorStrategy;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private AssessmentQuestionRepository assessmentQuestionRepository;

    @Mock
    private AssessmentAnswerRepository assessmentAnswerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssessmentQuestionGenerator questionGenerator;

    @Mock
    private QuestionEvaluatorFactory evaluatorFactory;

    @Mock
    private PlacementStrategyFactory placementStrategyFactory;

    @Mock
    private QuestionEvaluatorStrategy evaluatorStrategy;

    private AssessmentServiceImpl assessmentService;
    private ObjectMapper objectMapper;

    private User testUser;
    private Assessment testAssessment;
    private Question testQuestion;
    private AssessmentQuestion testAssessmentQuestion;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        assessmentService = new AssessmentServiceImpl(
                assessmentRepository,
                assessmentQuestionRepository,
                assessmentAnswerRepository,
                userRepository,
                questionGenerator,
                evaluatorFactory,
                placementStrategyFactory,
                objectMapper
        );

        testUser = new User("Alice", "alice@memora.com", "hash", VocabularyLevel.A1, Role.LEARNER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testAssessment = new Assessment(testUser, 1);
        ReflectionTestUtils.setField(testAssessment, "id", 100L);

        VocabularyWord word = new VocabularyWord("happy", "feeling pleasure", "def", null, null, DifficultyLevel.A1, WordCategory.GENERAL);
        testQuestion = new TranslationQuestion(word, "Translate 'happy'", 10, "feeling pleasure");
        ReflectionTestUtils.setField(testQuestion, "id", 200L);

        testAssessmentQuestion = new AssessmentQuestion(testAssessment, testQuestion, DifficultyLevel.A1, 1);
        ReflectionTestUtils.setField(testAssessmentQuestion, "id", 300L);
        testAssessment.addAssessmentQuestion(testAssessmentQuestion);
    }

    @Test
    @DisplayName("startAssessment should generate questions, persist assessment, and return start response")
    void testStartAssessment() {
        when(userRepository.findByEmail("alice@memora.com")).thenReturn(Optional.of(testUser));
        when(questionGenerator.generateAssessmentQuestions(any(Assessment.class))).thenReturn(List.of(testAssessmentQuestion));
        when(assessmentRepository.save(any(Assessment.class))).thenAnswer(i -> {
            Assessment a = i.getArgument(0);
            ReflectionTestUtils.setField(a, "id", 100L);
            return a;
        });

        AssessmentStartResponse response = assessmentService.startAssessment("alice@memora.com");

        assertNotNull(response);
        assertEquals(100L, response.getAssessmentId());
        assertEquals(AssessmentStatus.IN_PROGRESS, response.getStatus());
        assertEquals(1, response.getTotalQuestions());
        assertEquals(1, response.getQuestions().size());
        assertEquals(DifficultyLevel.A1, response.getQuestions().get(0).getDifficultyLevel());
    }

    @Test
    @DisplayName("startAssessment should return existing active assessment if one is already in progress")
    void testStartAssessmentReturnsExistingIfActiveAlreadyExists() {
        when(userRepository.findByEmail("alice@memora.com")).thenReturn(Optional.of(testUser));
        when(assessmentRepository.findActiveAssessment(1L)).thenReturn(Optional.of(testAssessment));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L))
                .thenReturn(List.of(testAssessmentQuestion));
        when(assessmentAnswerRepository.countByAssessmentId(100L)).thenReturn(1);

        AssessmentStartResponse response = assessmentService.startAssessment("alice@memora.com");

        assertNotNull(response);
        assertEquals(100L, response.getAssessmentId());
        assertEquals(AssessmentStatus.IN_PROGRESS, response.getStatus());
        assertEquals(1, response.getAnsweredQuestions());
        assertEquals(1, response.getQuestions().size());
        verify(assessmentRepository, never()).save(any(Assessment.class));
    }

    @Test
    @DisplayName("getAssessment should return details with countByAssessmentId")
    void testGetAssessmentSuccess() {
        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(testAssessment));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L))
                .thenReturn(List.of(testAssessmentQuestion));
        when(assessmentAnswerRepository.countByAssessmentId(100L)).thenReturn(1);

        AssessmentDetailResponse response = assessmentService.getAssessment("alice@memora.com", 100L);

        assertNotNull(response);
        assertEquals(100L, response.getAssessmentId());
        assertEquals(1, response.getAnsweredQuestions());
        assertEquals(0, response.getRemainingQuestions());
        assertEquals(1, response.getQuestions().size());
    }

    @Test
    @DisplayName("submitAnswer should evaluate answer, persist answer, and return response")
    void testSubmitAnswerSuccess() {
        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(testAssessment));
        when(assessmentQuestionRepository.findByAssessmentIdAndQuestionId(100L, 200L))
                .thenReturn(Optional.of(testAssessmentQuestion));
        when(assessmentAnswerRepository.existsByAssessmentIdAndAssessmentQuestionId(100L, 300L))
                .thenReturn(false);
        when(evaluatorFactory.getEvaluator(QuestionType.TRANSLATION)).thenReturn(evaluatorStrategy);
        when(evaluatorStrategy.evaluate(testQuestion, "feeling pleasure"))
                .thenReturn(new EvaluationResult(true, 10, "Correct!"));

        AssessmentAnswerRequest request = new AssessmentAnswerRequest("feeling pleasure", 1500L);
        AssessmentAnswerResponse response = assessmentService.submitAnswer("alice@memora.com", 100L, 200L, request);

        assertNotNull(response);
        assertTrue(response.isCorrect());
        assertEquals(1500L, response.getResponseTimeMs());
        verify(assessmentAnswerRepository, times(1)).save(any(AssessmentAnswer.class));
    }

    @Test
    @DisplayName("submitAnswer should prevent duplicate answers for the same question")
    void testSubmitAnswerDuplicatePrevention() {
        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(testAssessment));
        when(assessmentQuestionRepository.findByAssessmentIdAndQuestionId(100L, 200L))
                .thenReturn(Optional.of(testAssessmentQuestion));
        when(assessmentAnswerRepository.existsByAssessmentIdAndAssessmentQuestionId(100L, 300L))
                .thenReturn(true);

        AssessmentAnswerRequest request = new AssessmentAnswerRequest("feeling pleasure", 1500L);

        assertThrows(IllegalArgumentException.class, () ->
                assessmentService.submitAnswer("alice@memora.com", 100L, 200L, request));
    }

    @Test
    @DisplayName("Accessing or answering assessment of another user should throw AccessDeniedException")
    void testUnauthorizedAccess() {
        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(testAssessment));

        assertThrows(AccessDeniedException.class, () ->
                assessmentService.getAssessment("intruder@memora.com", 100L));
    }

    @Test
    @DisplayName("completeAssessment should fail if not all questions are answered")
    void testCompleteAssessmentBeforeAllAnswered() {
        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(testAssessment));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L))
                .thenReturn(List.of(testAssessmentQuestion));
        when(assessmentAnswerRepository.findByAssessmentId(100L)).thenReturn(List.of()); // 0 answers

        assertThrows(IllegalArgumentException.class, () ->
                assessmentService.completeAssessment("alice@memora.com", 100L));
    }

    @Test
    @DisplayName("completeAssessment should calculate CEFR level and mark assessment COMPLETED")
    void testCompleteAssessmentSuccess() {
        AssessmentAnswer answer = new AssessmentAnswer(testAssessment, testAssessmentQuestion, "feeling pleasure", true, 1200L);
        ReflectionTestUtils.setField(answer, "id", 400L);

        when(assessmentRepository.findById(100L)).thenReturn(Optional.of(testAssessment));
        when(assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(100L))
                .thenReturn(List.of(testAssessmentQuestion));
        when(assessmentAnswerRepository.findByAssessmentId(100L)).thenReturn(List.of(answer));

        DefaultPlacementStrategy placementStrategy = new DefaultPlacementStrategy();
        when(placementStrategyFactory.getDefaultStrategy()).thenReturn(placementStrategy);

        PlacementResultResponse result = assessmentService.completeAssessment("alice@memora.com", 100L);

        assertNotNull(result);
        assertEquals(100L, result.getAssessmentId());
        assertEquals(DifficultyLevel.A1, result.getEstimatedLevel());
        assertEquals(100.0, result.getAccuracy());
        assertEquals(AssessmentStatus.COMPLETED, testAssessment.getStatus());
        verify(assessmentRepository, times(1)).save(testAssessment);
    }
}
