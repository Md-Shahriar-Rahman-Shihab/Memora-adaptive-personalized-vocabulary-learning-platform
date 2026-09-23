package com.memora.modules.learningpath;

import com.memora.modules.assessment.domain.AssessmentStatus;
import com.memora.modules.assessment.entity.Assessment;
import com.memora.modules.assessment.repository.AssessmentRepository;
import com.memora.modules.learningpath.domain.*;
import com.memora.modules.learningpath.dto.*;
import com.memora.modules.learningpath.entity.LearningPath;
import com.memora.modules.learningpath.entity.LearningPathItem;
import com.memora.modules.learningpath.factory.LearningPathStrategyFactory;
import com.memora.modules.learningpath.repository.LearningPathItemRepository;
import com.memora.modules.learningpath.repository.LearningPathRepository;
import com.memora.modules.learningpath.service.LearningPathServiceImpl;
import com.memora.modules.learningpath.strategy.AdaptiveLearningPathStrategy;
import com.memora.modules.memory.dto.WordReviewRequest;
import com.memora.modules.memory.service.MemoryService;
import com.memora.modules.quiz.dto.QuizGenerationRequest;
import com.memora.modules.quiz.dto.QuizResponse;
import com.memora.modules.quiz.entity.Quiz;
import com.memora.modules.quiz.repository.QuizRepository;
import com.memora.modules.quiz.service.QuizService;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningPathServiceTest {

    @Mock
    private LearningPathRepository learningPathRepository;

    @Mock
    private LearningPathItemRepository learningPathItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VocabularyWordRepository vocabularyWordRepository;

    @Mock
    private UserWordProgressRepository userWordProgressRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private MemoryService memoryService;

    @Mock
    private QuizService quizService;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private LearningPathStrategyFactory strategyFactory;

    private LearningPathServiceImpl learningPathService;
    private AdaptiveLearningPathStrategy adaptiveStrategy;

    private User testUser;
    private VocabularyWord testWord;
    private LearningPath testPath;
    private LearningPathItem testItem;

    @BeforeEach
    void setUp() {
        adaptiveStrategy = new AdaptiveLearningPathStrategy();
        learningPathService = new LearningPathServiceImpl(
                learningPathRepository,
                learningPathItemRepository,
                userRepository,
                vocabularyWordRepository,
                userWordProgressRepository,
                assessmentRepository,
                memoryService,
                quizService,
                quizRepository,
                strategyFactory
        );

        testUser = new User("Alice", "alice@memora.com", "hash", VocabularyLevel.B1, Role.LEARNER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testWord = new VocabularyWord("meticulous", "careful", "def", null, null, DifficultyLevel.B1, WordCategory.GENERAL);
        ReflectionTestUtils.setField(testWord, "id", 10L);

        testPath = new LearningPath(testUser, DifficultyLevel.B1, 1);
        ReflectionTestUtils.setField(testPath, "id", 100L);

        testItem = new LearningPathItem(testPath, testWord, null, LearningItemType.REVIEW, LearningItemPriority.HIGH, 1, "Review test");
        ReflectionTestUtils.setField(testItem, "id", 200L);
        testPath.addItem(testItem);
    }

    @Test
    @DisplayName("startPath should create and persist new path when no active path exists")
    void testStartPathNew() {
        when(userRepository.findByEmail("alice@memora.com")).thenReturn(Optional.of(testUser));
        when(learningPathRepository.findActivePathByUserId(1L)).thenReturn(Optional.empty());

        Assessment assessment = new Assessment(testUser, 20);
        assessment.setStatus(AssessmentStatus.COMPLETED);
        assessment.setEstimatedLevel(DifficultyLevel.B1);
        when(assessmentRepository.findByUserIdOrderByStartedAtDesc(1L)).thenReturn(List.of(assessment));

        when(memoryService.getDueReviews("alice@memora.com")).thenReturn(List.of());
        when(memoryService.getWeakWords("alice@memora.com")).thenReturn(List.of());
        when(userWordProgressRepository.findByUser(testUser)).thenReturn(List.of());
        when(vocabularyWordRepository.findByDifficultyLevel(DifficultyLevel.B1)).thenReturn(List.of(testWord));
        when(strategyFactory.getDefaultStrategy()).thenReturn(adaptiveStrategy);

        when(vocabularyWordRepository.findById(10L)).thenReturn(Optional.of(testWord));
        when(quizService.generateQuiz(any(QuizGenerationRequest.class))).thenReturn(new QuizResponse());
        when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(i -> {
            LearningPath p = i.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 100L);
            return p;
        });
        when(learningPathItemRepository.findByLearningPathIdOrderByOrderIndexAsc(100L))
                .thenReturn(List.of(testItem));

        LearningPathResponse response = learningPathService.startPath("alice@memora.com");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(DifficultyLevel.B1, response.getTargetLevel());
        assertEquals(LearningPathStatus.ACTIVE, response.getStatus());
        verify(learningPathRepository, times(1)).save(any(LearningPath.class));
    }

    @Test
    @DisplayName("startPath should return existing active path without duplicating")
    void testStartPathReturnsExistingActive() {
        when(userRepository.findByEmail("alice@memora.com")).thenReturn(Optional.of(testUser));
        when(learningPathRepository.findActivePathByUserId(1L)).thenReturn(Optional.of(testPath));
        when(learningPathItemRepository.findByLearningPathIdOrderByOrderIndexAsc(100L))
                .thenReturn(List.of(testItem));

        LearningPathResponse response = learningPathService.startPath("alice@memora.com");

        assertNotNull(response);
        assertEquals(100L, response.getId());
        verify(learningPathRepository, never()).save(any(LearningPath.class));
    }

    @Test
    @DisplayName("startItem should mark item as IN_PROGRESS")
    void testStartItem() {
        when(learningPathItemRepository.findById(200L)).thenReturn(Optional.of(testItem));
        when(learningPathItemRepository.save(any(LearningPathItem.class))).thenAnswer(i -> i.getArgument(0));

        LearningPathItemResponse response = learningPathService.startItem("alice@memora.com", 200L);

        assertNotNull(response);
        assertEquals(LearningItemStatus.IN_PROGRESS, response.getStatus());
        verify(learningPathItemRepository, times(1)).save(testItem);
    }

    @Test
    @DisplayName("completeItem for review type should record review with MemoryService")
    void testCompleteItemReview() {
        when(learningPathItemRepository.findById(200L)).thenReturn(Optional.of(testItem));
        when(learningPathItemRepository.save(any(LearningPathItem.class))).thenAnswer(i -> i.getArgument(0));
        when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(i -> i.getArgument(0));

        LearningItemCompletionRequest request = new LearningItemCompletionRequest(true, 1200L, null, null);
        LearningItemCompletionResponse response = learningPathService.completeItem("alice@memora.com", 200L, request);

        assertNotNull(response);
        assertEquals(LearningItemStatus.COMPLETED, response.getStatus());
        verify(memoryService, times(1)).recordReview(eq("alice@memora.com"), any(WordReviewRequest.class));
        verify(learningPathRepository, times(1)).save(testPath);
    }

    @Test
    @DisplayName("completeItem for new word type should record review with MemoryService")
    void testCompleteItemNewWord() {
        LearningPathItem newWordItem = new LearningPathItem(testPath, testWord, null, LearningItemType.NEW_WORD, LearningItemPriority.HIGH, 2, "Learn new word");
        ReflectionTestUtils.setField(newWordItem, "id", 201L);
        testPath.addItem(newWordItem);

        when(learningPathItemRepository.findById(201L)).thenReturn(Optional.of(newWordItem));
        when(learningPathItemRepository.save(any(LearningPathItem.class))).thenAnswer(i -> i.getArgument(0));
        when(learningPathRepository.save(any(LearningPath.class))).thenAnswer(i -> i.getArgument(0));

        LearningItemCompletionRequest request = new LearningItemCompletionRequest(true, 1000L, null, null);
        LearningItemCompletionResponse response = learningPathService.completeItem("alice@memora.com", 201L, request);

        assertNotNull(response);
        assertEquals(LearningItemStatus.COMPLETED, response.getStatus());
        assertEquals(LearningItemType.NEW_WORD, response.getItemType());
        verify(memoryService, times(1)).recordReview(eq("alice@memora.com"), any(WordReviewRequest.class));
    }

    @Test
    @DisplayName("completeItem on already completed item should return idempotent response with 0 xpEarned")
    void testCompleteItemAlreadyCompleted() {
        testItem.markCompleted();
        when(learningPathItemRepository.findById(200L)).thenReturn(Optional.of(testItem));

        LearningItemCompletionResponse response = learningPathService.completeItem("alice@memora.com", 200L, null);

        assertNotNull(response);
        assertEquals(LearningItemStatus.COMPLETED, response.getStatus());
        assertEquals(Integer.valueOf(0), response.getXpEarned());
        assertEquals("Item is already completed", response.getMessage());
        verify(learningPathItemRepository, never()).save(any(LearningPathItem.class));
    }

    @Test
    @DisplayName("Unauthorized learner accessing another user's path item should throw AccessDeniedException")
    void testUnauthorizedAccess() {
        when(learningPathItemRepository.findById(200L)).thenReturn(Optional.of(testItem));

        assertThrows(AccessDeniedException.class, () ->
                learningPathService.startItem("intruder@memora.com", 200L));
    }
}
