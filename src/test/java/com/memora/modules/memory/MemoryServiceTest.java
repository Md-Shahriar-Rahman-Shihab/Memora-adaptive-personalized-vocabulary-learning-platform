package com.memora.modules.memory;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.dto.MemoryWordResponse;
import com.memora.modules.memory.dto.WordReviewRequest;
import com.memora.modules.memory.dto.WordReviewResponse;
import com.memora.modules.memory.mapper.MemoryMapper;
import com.memora.modules.memory.service.MemoryService;
import com.memora.modules.memory.service.MemoryServiceImpl;
import com.memora.modules.memory.strategy.LeitnerMemoryStrategy;
import com.memora.modules.memory.strategy.MemoryStrategyFactory;
import com.memora.modules.memory.strategy.SM2MemoryStrategy;
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
import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import com.memora.modules.gamification.dto.GamificationActivityResultResponse;
import com.memora.modules.gamification.service.GamificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemoryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VocabularyWordRepository vocabularyWordRepository;

    @Mock
    private UserWordProgressRepository userWordProgressRepository;

    @Mock
    private GamificationService gamificationService;

    private MemoryService memoryService;
    private User testUser;
    private VocabularyWord testWord;

    @BeforeEach
    void setUp() {
        SM2MemoryStrategy sm2Strategy = new SM2MemoryStrategy();
        LeitnerMemoryStrategy leitnerStrategy = new LeitnerMemoryStrategy();
        MemoryStrategyFactory factory = new MemoryStrategyFactory(List.of(sm2Strategy, leitnerStrategy));
        MemoryMapper mapper = new MemoryMapper();

        memoryService = new MemoryServiceImpl(
                userRepository,
                vocabularyWordRepository,
                userWordProgressRepository,
                factory,
                mapper,
                gamificationService
        );

        testUser = new User("Alice", "alice@example.com", "encodedPassword", VocabularyLevel.B1, Role.LEARNER);
        testUser.setId(1L);

        testWord = new VocabularyWord("serendipity", "সৌভাগ্যজনক আকস্মিক আবিষ্কার", "occurrence of events by chance in a happy way",
                "/ˌser.ənˈdɪp.ə.ti/", "A fortunate stroke of serendipity.", DifficultyLevel.B2, WordCategory.GENERAL);
        testWord.setId(10L);
    }

    @Test
    @DisplayName("First review creates new progress and computes SM2 schedule")
    void firstReviewCreatesProgressAndSchedules() {
        WordReviewRequest request = new WordReviewRequest(10L, true, 1200L, MemoryAlgorithmType.SM2);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(vocabularyWordRepository.findById(10L)).thenReturn(Optional.of(testWord));
        when(userWordProgressRepository.findByUserAndVocabularyWord(testUser, testWord)).thenReturn(Optional.empty());
        when(userWordProgressRepository.save(any(UserWordProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WordReviewResponse response = memoryService.recordReview("alice@example.com", request);

        assertThat(response).isNotNull();
        assertThat(response.getWordId()).isEqualTo(10L);
        assertThat(response.getWord()).isEqualTo("serendipity");
        assertThat(response.isCorrect()).isTrue();
        assertThat(response.getAlgorithm()).isEqualTo(MemoryAlgorithmType.SM2);
        assertThat(response.getReviewIntervalDays()).isEqualTo(1);
        assertThat(response.getMasteryScore()).isGreaterThan(0.0);
        assertThat(response.getNextReviewAt()).isNotNull();

        verify(userWordProgressRepository).save(any(UserWordProgress.class));
    }

    @Test
    @DisplayName("Subsequent correct review updates incremental response time and expands interval")
    void subsequentReviewUpdatesIncrementalResponseTime() {
        UserWordProgress existingProgress = new UserWordProgress(testUser, testWord);
        existingProgress.setTotalAttempts(1);
        existingProgress.setCorrectAttempts(1);
        existingProgress.setConsecutiveCorrect(1);
        existingProgress.setAverageResponseTime(1000.0);
        existingProgress.setMasteryScore(65.0);

        WordReviewRequest request = new WordReviewRequest(10L, true, 2000L, MemoryAlgorithmType.SM2);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(vocabularyWordRepository.findById(10L)).thenReturn(Optional.of(testWord));
        when(userWordProgressRepository.findByUserAndVocabularyWord(testUser, testWord)).thenReturn(Optional.of(existingProgress));
        when(userWordProgressRepository.save(any(UserWordProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WordReviewResponse response = memoryService.recordReview("alice@example.com", request);

        assertThat(response).isNotNull();
        assertThat(response.getReviewIntervalDays()).isEqualTo(3); // 2nd consecutive correct
        assertThat(existingProgress.getTotalAttempts()).isEqualTo(2);
        assertThat(existingProgress.getCorrectAttempts()).isEqualTo(2);
        assertThat(existingProgress.getConsecutiveCorrect()).isEqualTo(2);
        // Incremental avg: (1000 * 1 + 2000) / 2 = 1500.0
        assertThat(existingProgress.getAverageResponseTime()).isEqualTo(1500.0);
    }

    @Test
    @DisplayName("Incorrect review resets consecutive streak and updates failure metrics")
    void incorrectReviewResetsStreak() {
        UserWordProgress existingProgress = new UserWordProgress(testUser, testWord);
        existingProgress.setTotalAttempts(2);
        existingProgress.setCorrectAttempts(2);
        existingProgress.setConsecutiveCorrect(2);
        existingProgress.setAverageResponseTime(1500.0);
        existingProgress.setMasteryScore(75.0);

        WordReviewRequest request = new WordReviewRequest(10L, false, 4000L, MemoryAlgorithmType.SM2);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(vocabularyWordRepository.findById(10L)).thenReturn(Optional.of(testWord));
        when(userWordProgressRepository.findByUserAndVocabularyWord(testUser, testWord)).thenReturn(Optional.of(existingProgress));
        when(userWordProgressRepository.save(any(UserWordProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WordReviewResponse response = memoryService.recordReview("alice@example.com", request);

        assertThat(response.isCorrect()).isFalse();
        assertThat(existingProgress.getTotalAttempts()).isEqualTo(3);
        assertThat(existingProgress.getIncorrectAttempts()).isEqualTo(1);
        assertThat(existingProgress.getConsecutiveCorrect()).isEqualTo(0);
        assertThat(existingProgress.getConsecutiveIncorrect()).isEqualTo(1);
        assertThat(response.getReviewIntervalDays()).isEqualTo(1);
    }

    @Test
    @DisplayName("Leitner review promotes box and updates schedule")
    void leitnerReviewPromotesBox() {
        UserWordProgress existingProgress = new UserWordProgress(testUser, testWord);
        existingProgress.setLeitnerBox(1);

        WordReviewRequest request = new WordReviewRequest(10L, true, 1800L, MemoryAlgorithmType.LEITNER);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(vocabularyWordRepository.findById(10L)).thenReturn(Optional.of(testWord));
        when(userWordProgressRepository.findByUserAndVocabularyWord(testUser, testWord)).thenReturn(Optional.of(existingProgress));
        when(userWordProgressRepository.save(any(UserWordProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WordReviewResponse response = memoryService.recordReview("alice@example.com", request);

        assertThat(response.getAlgorithm()).isEqualTo(MemoryAlgorithmType.LEITNER);
        assertThat(response.getReviewIntervalDays()).isEqualTo(3); // Box 2 interval
        assertThat(existingProgress.getLeitnerBox()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user does not exist")
    void shouldThrowWhenUserNotFound() {
        WordReviewRequest request = new WordReviewRequest(10L, true, 1800L, MemoryAlgorithmType.SM2);
        when(userRepository.findByEmail("unknown@memora.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memoryService.recordReview("unknown@memora.com", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when word does not exist")
    void shouldThrowWhenWordNotFound() {
        WordReviewRequest request = new WordReviewRequest(999L, true, 1800L, MemoryAlgorithmType.SM2);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(vocabularyWordRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memoryService.recordReview("alice@example.com", request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Vocabulary word not found");
    }

    @Test
    @DisplayName("Should fetch due reviews for user")
    void shouldFetchDueReviews() {
        UserWordProgress dueProgress = new UserWordProgress(testUser, testWord);
        dueProgress.setNextReviewAt(Instant.now().minusSeconds(3600));

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(userWordProgressRepository.findDueForReview(eq(testUser), any(Instant.class)))
                .thenReturn(List.of(dueProgress));

        List<MemoryWordResponse> dueList = memoryService.getDueReviews("alice@example.com");

        assertThat(dueList).hasSize(1);
        assertThat(dueList.get(0).getWord()).isEqualTo("serendipity");
    }

    @Test
    @DisplayName("Should fetch weak words for user")
    void shouldFetchWeakWords() {
        UserWordProgress weakProgress = new UserWordProgress(testUser, testWord);
        weakProgress.setForgettingRisk(ForgettingRisk.HIGH);
        weakProgress.setMasteryScore(25.0);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(userWordProgressRepository.findWeakWords(testUser))
                .thenReturn(List.of(weakProgress));

        List<MemoryWordResponse> weakList = memoryService.getWeakWords("alice@example.com");

        assertThat(weakList).hasSize(1);
        assertThat(weakList.get(0).getForgettingRisk()).isEqualTo(ForgettingRisk.HIGH);
        assertThat(weakList.get(0).getMasteryScore()).isEqualTo(25.0);
    }

    @Test
    @DisplayName("Review completion awards XP and streak via GamificationService")
    void reviewCompletionAwardsXpAndStreak() {
        WordReviewRequest request = new WordReviewRequest(10L, true, 1500L, MemoryAlgorithmType.SM2, true);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(vocabularyWordRepository.findById(10L)).thenReturn(Optional.of(testWord));
        when(userWordProgressRepository.findByUserAndVocabularyWord(testUser, testWord)).thenReturn(Optional.empty());
        when(userWordProgressRepository.save(any(UserWordProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GamificationActivityResultResponse gamificationResult = new GamificationActivityResultResponse(
                5,
                105,
                3,
                5,
                List.of(),
                "Activity recorded: +5 XP earned, 3-day streak"
        );
        when(gamificationService.recordActivity(eq(testUser), eq(RewardActivityType.REVIEW), any(RewardContext.class)))
                .thenReturn(gamificationResult);

        WordReviewResponse response = memoryService.recordReview("alice@example.com", request);

        assertThat(response).isNotNull();
        assertThat(response.getXpEarned()).isEqualTo(5);
        assertThat(response.getCurrentStreak()).isEqualTo(3);
        assertThat(response.getTotalXp()).isEqualTo(105);
        assertThat(response.getPreviousMasteryScore()).isEqualTo(0.0);
        assertThat(response.getPreviousForgettingRisk()).isEqualTo(ForgettingRisk.LOW);

        verify(gamificationService).recordActivity(eq(testUser), eq(RewardActivityType.REVIEW), any(RewardContext.class));
    }

    @Test
    @DisplayName("Review completion with awardXp false does not invoke GamificationService")
    void reviewWithAwardXpFalseDoesNotInvokeGamification() {
        WordReviewRequest request = new WordReviewRequest(10L, true, 1500L, MemoryAlgorithmType.SM2, false);

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(vocabularyWordRepository.findById(10L)).thenReturn(Optional.of(testWord));
        when(userWordProgressRepository.findByUserAndVocabularyWord(testUser, testWord)).thenReturn(Optional.empty());
        when(userWordProgressRepository.save(any(UserWordProgress.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WordReviewResponse response = memoryService.recordReview("alice@example.com", request);

        assertThat(response).isNotNull();
        assertThat(response.getXpEarned()).isNull();
        verify(gamificationService, never()).recordActivity(any(), any(), any());
    }

    @Test
    @DisplayName("Due reviews include definition, pronunciation, and example sentence")
    void dueReviewsIncludeLexicalDetails() {
        UserWordProgress dueProgress = new UserWordProgress(testUser, testWord);
        dueProgress.setNextReviewAt(Instant.now().minusSeconds(3600));

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(userWordProgressRepository.findDueForReview(eq(testUser), any(Instant.class)))
                .thenReturn(List.of(dueProgress));

        List<MemoryWordResponse> dueList = memoryService.getDueReviews("alice@example.com");

        assertThat(dueList).hasSize(1);
        MemoryWordResponse word = dueList.get(0);
        assertThat(word.getWord()).isEqualTo("serendipity");
        assertThat(word.getMeaning()).isEqualTo("সৌভাগ্যজনক আকস্মিক আবিষ্কার");
        assertThat(word.getDefinition()).isEqualTo("occurrence of events by chance in a happy way");
        assertThat(word.getPronunciation()).isEqualTo("/ˌser.ənˈdɪp.ə.ti/");
        assertThat(word.getExampleSentence()).isEqualTo("A fortunate stroke of serendipity.");
    }

    @Test
    @DisplayName("Should return empty list when no reviews are due")
    void noDueReviewsReturnsEmptyList() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(testUser));
        when(userWordProgressRepository.findDueForReview(eq(testUser), any(Instant.class)))
                .thenReturn(List.of());

        List<MemoryWordResponse> dueList = memoryService.getDueReviews("alice@example.com");

        assertThat(dueList).isEmpty();
    }
}
