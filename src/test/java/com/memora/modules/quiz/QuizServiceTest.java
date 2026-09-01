package com.memora.modules.quiz;

import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.dto.WordReviewRequest;
import com.memora.modules.memory.dto.WordReviewResponse;
import com.memora.modules.memory.service.MemoryService;
import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.dto.*;
import com.memora.modules.quiz.entity.*;
import com.memora.modules.quiz.factory.QuestionEvaluatorFactory;
import com.memora.modules.quiz.factory.QuestionFactory;
import com.memora.modules.quiz.repository.QuestionAttemptRepository;
import com.memora.modules.quiz.repository.QuestionRepository;
import com.memora.modules.quiz.repository.QuizAttemptRepository;
import com.memora.modules.quiz.repository.QuizRepository;
import com.memora.modules.quiz.service.QuizServiceImpl;
import com.memora.modules.quiz.strategy.QuestionEvaluatorStrategy;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private QuestionAttemptRepository questionAttemptRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VocabularyWordRepository vocabularyWordRepository;

    @Mock
    private QuestionFactory questionFactory;

    @Mock
    private QuestionEvaluatorFactory evaluatorFactory;

    @Mock
    private MemoryService memoryService;

    @Mock
    private QuestionEvaluatorStrategy evaluatorStrategy;

    private QuizServiceImpl quizService;

    private User testUser;
    private VocabularyWord testWord;
    private Quiz testQuiz;
    private Question testQuestion;

    @BeforeEach
    void setUp() {
        quizService = new QuizServiceImpl(
                quizRepository,
                questionRepository,
                quizAttemptRepository,
                questionAttemptRepository,
                userRepository,
                vocabularyWordRepository,
                questionFactory,
                evaluatorFactory,
                memoryService
        );

        testUser = new User("Alice", "alice@memora.com", "hash", VocabularyLevel.A1, Role.LEARNER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testWord = new VocabularyWord("apple", "আপেল", "a round fruit", "/ˈæp.əl/", "I ate an apple.", DifficultyLevel.A1, WordCategory.DAILY_LIFE);
        ReflectionTestUtils.setField(testWord, "id", 10L);

        testQuiz = new Quiz("A1 Practice Quiz", DifficultyLevel.A1, 1);
        ReflectionTestUtils.setField(testQuiz, "id", 100L);

        testQuestion = new TranslationQuestion(testWord, "Translate 'apple'", 10, "আপেল");
        ReflectionTestUtils.setField(testQuestion, "id", 200L);
        testQuiz.addQuestion(testQuestion);
    }

    @Test
    @DisplayName("generateQuiz should create quiz and return QuizResponse")
    void generateQuizSuccess() {
        when(vocabularyWordRepository.findByDifficultyLevel(DifficultyLevel.A1)).thenReturn(List.of(testWord));
        when(questionFactory.createQuestion(any(), eq(testWord), any())).thenReturn(testQuestion);
        when(quizRepository.save(any(Quiz.class))).thenAnswer(invocation -> {
            Quiz q = invocation.getArgument(0);
            ReflectionTestUtils.setField(q, "id", 100L);
            return q;
        });

        QuizGenerationRequest req = new QuizGenerationRequest(DifficultyLevel.A1, 1, List.of(QuestionType.TRANSLATION));
        QuizResponse response = quizService.generateQuiz(req);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1, response.getQuestionCount());
        assertEquals(1, response.getQuestions().size());
        assertEquals("Translate 'apple'", response.getQuestions().get(0).getQuestionText());
    }

    @Test
    @DisplayName("startQuiz should initialize QuizAttempt and return QuizResponse")
    void startQuizSuccess() {
        when(userRepository.findByEmail("alice@memora.com")).thenReturn(Optional.of(testUser));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(testQuiz));
        when(quizAttemptRepository.findActiveAttempt(100L, 1L)).thenReturn(Optional.empty());
        when(quizAttemptRepository.save(any(QuizAttempt.class))).thenAnswer(i -> i.getArgument(0));

        QuizResponse response = quizService.startQuiz("alice@memora.com", 100L);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        verify(quizAttemptRepository, times(1)).save(any(QuizAttempt.class));
    }

    @Test
    @DisplayName("submitAnswer should evaluate answer, update attempts and sync with MemoryService")
    void submitAnswerSuccess() {
        QuizAttempt attempt = new QuizAttempt(testUser, testQuiz, 1);
        ReflectionTestUtils.setField(attempt, "id", 500L);

        when(userRepository.findByEmail("alice@memora.com")).thenReturn(Optional.of(testUser));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(testQuiz));
        when(questionRepository.findById(200L)).thenReturn(Optional.of(testQuestion));
        when(quizAttemptRepository.findActiveAttempt(100L, 1L)).thenReturn(Optional.of(attempt));
        when(evaluatorFactory.getEvaluator(QuestionType.TRANSLATION)).thenReturn(evaluatorStrategy);
        when(evaluatorStrategy.evaluate(testQuestion, "আপেল"))
                .thenReturn(new EvaluationResult(true, 10, "Correct translation! Well done."));

        WordReviewResponse memoryResponse = new WordReviewResponse(
                10L, "apple", true, 65.0, ForgettingRisk.LOW, Instant.now().plusSeconds(86400), 1, MemoryAlgorithmType.SM2
        );
        when(memoryService.recordReview(eq("alice@memora.com"), any(WordReviewRequest.class)))
                .thenReturn(memoryResponse);

        AnswerSubmissionRequest req = new AnswerSubmissionRequest(200L, "আপেল", 1200L);
        AnswerResponse response = quizService.submitAnswer("alice@memora.com", 100L, 200L, req);

        assertNotNull(response);
        assertTrue(response.isCorrect());
        assertEquals(10, response.getScore());
        assertEquals(65.0, response.getMasteryScore());
        assertEquals(ForgettingRisk.LOW, response.getForgettingRisk());

        verify(questionAttemptRepository, times(1)).save(any(QuestionAttempt.class));
        verify(memoryService, times(1)).recordReview(eq("alice@memora.com"), any(WordReviewRequest.class));
    }

    @Test
    @DisplayName("completeQuiz should finalize active attempt and return QuizResultResponse")
    void completeQuizSuccess() {
        QuizAttempt attempt = new QuizAttempt(testUser, testQuiz, 1);
        attempt.addQuestionAttempt(new QuestionAttempt(attempt, testQuestion, "আপেল", true, 10, 1000L));

        when(userRepository.findByEmail("alice@memora.com")).thenReturn(Optional.of(testUser));
        when(quizRepository.findById(100L)).thenReturn(Optional.of(testQuiz));
        when(quizAttemptRepository.findActiveAttempt(100L, 1L)).thenReturn(Optional.of(attempt));

        QuizResultResponse result = quizService.completeQuiz("alice@memora.com", 100L);

        assertNotNull(result);
        assertEquals(100L, result.getQuizId());
        assertEquals(1, result.getTotalQuestions());
        assertEquals(1, result.getCorrectAnswers());
        assertEquals(10, result.getTotalScore());
        assertEquals(100.0, result.getPercentage());
        assertNotNull(attempt.getCompletedAt());
    }
}
