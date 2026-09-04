package com.memora.modules.gamification;

import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import com.memora.modules.gamification.dto.*;
import com.memora.modules.gamification.entity.*;
import com.memora.modules.gamification.factory.RewardStrategyFactory;
import com.memora.modules.gamification.repository.UserGamificationProfileRepository;
import com.memora.modules.gamification.repository.XpTransactionRepository;
import com.memora.modules.gamification.service.AchievementService;
import com.memora.modules.gamification.service.GamificationServiceImpl;
import com.memora.modules.gamification.service.StreakService;
import com.memora.modules.gamification.strategy.QuizRewardStrategy;
import com.memora.modules.quiz.repository.QuizAttemptRepository;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock
    private UserGamificationProfileRepository profileRepository;

    @Mock
    private XpTransactionRepository xpTransactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserWordProgressRepository userWordProgressRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private RewardStrategyFactory strategyFactory;

    @Mock
    private StreakService streakService;

    @Mock
    private AchievementService achievementService;

    private GamificationServiceImpl gamificationService;
    private User testUser;
    private UserGamificationProfile testProfile;

    @BeforeEach
    void setUp() {
        gamificationService = new GamificationServiceImpl(
                profileRepository,
                xpTransactionRepository,
                userRepository,
                userWordProgressRepository,
                quizAttemptRepository,
                strategyFactory,
                streakService,
                achievementService
        );

        testUser = new User("Alice", "alice@memora.com", "hash", VocabularyLevel.B1, Role.LEARNER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testProfile = new UserGamificationProfile(testUser);
        ReflectionTestUtils.setField(testProfile, "id", 10L);
    }

    @Test
    @DisplayName("recordActivity should calculate reward, update profile XP, advance streak, and check achievements")
    void testRecordActivity() {
        QuizRewardStrategy quizStrategy = new QuizRewardStrategy();
        when(strategyFactory.getStrategy(RewardActivityType.QUIZ)).thenReturn(quizStrategy);
        when(streakService.recordActivityStreak(testUser)).thenReturn(testProfile);
        when(userWordProgressRepository.findByUser(testUser)).thenReturn(List.of());
        when(quizAttemptRepository.findByUserIdOrderByStartedAtDesc(1L)).thenReturn(List.of());
        when(achievementService.evaluateAndAwardAchievements(any())).thenReturn(List.of(
                new Achievement(AchievementCode.FIRST_QUIZ, "Quiz Initiate", "Completed first quiz", "fa-icon")
        ));

        RewardContext context = RewardContext.forQuizAnswer(100L, true);
        GamificationActivityResultResponse response = gamificationService.recordActivity(testUser, RewardActivityType.QUIZ, context);

        assertNotNull(response);
        assertEquals(10, response.getXpEarned());
        assertEquals(10, response.getNewTotalXp());
        assertEquals(1, response.getNewAchievements().size());
        assertEquals(AchievementCode.FIRST_QUIZ, response.getNewAchievements().get(0).getCode());

        verify(xpTransactionRepository, times(1)).save(any(XpTransaction.class));
        verify(profileRepository, times(1)).save(testProfile);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("getProfile should aggregate comprehensive learner metrics")
    void testGetProfile() {
        testProfile.setTotalXp(250);
        testProfile.setCurrentStreak(5);
        testProfile.setLongestStreak(8);

        when(userRepository.findByEmail("alice@memora.com")).thenReturn(Optional.of(testUser));
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(testProfile));

        VocabularyWord word = new VocabularyWord("lucid", "clear", "def", "/pron/", "ex", DifficultyLevel.B1, WordCategory.ACADEMIC);
        UserWordProgress progress = new UserWordProgress(testUser, word);
        progress.setMasteryScore(75.0);
        progress.setCorrectAttempts(1);
        progress.setTotalAttempts(1);
        when(userWordProgressRepository.findByUser(testUser)).thenReturn(List.of(progress));

        when(quizAttemptRepository.findByUserIdOrderByStartedAtDesc(1L)).thenReturn(List.of());
        when(profileRepository.calculateRank(250, 5)).thenReturn(3L);
        when(achievementService.getLearnerAchievements(testUser)).thenReturn(List.of());

        LearnerProfileResponse profile = gamificationService.getProfile("alice@memora.com");

        assertNotNull(profile);
        assertEquals("Alice", profile.getName());
        assertEquals(250, profile.getXp());
        assertEquals(5, profile.getCurrentStreak());
        assertEquals(8, profile.getLongestStreak());
        assertEquals(1, profile.getWordsLearned());
        assertEquals(3L, profile.getRank());
        assertEquals("B1", profile.getLevel());
    }

    @Test
    @DisplayName("getLeaderboard should return deterministic ranking and omit private information")
    void testGetLeaderboard() {
        User user1 = new User("Bob", "bob@secret.com", "hash", VocabularyLevel.B2, Role.LEARNER);
        ReflectionTestUtils.setField(user1, "id", 2L);
        UserGamificationProfile profile1 = new UserGamificationProfile(user1);
        profile1.setTotalXp(500);
        profile1.setCurrentStreak(10);

        when(profileRepository.findLeaderboard(any(Pageable.class))).thenReturn(List.of(profile1));

        List<LeaderboardEntryResponse> leaderboard = gamificationService.getLeaderboard(10);

        assertNotNull(leaderboard);
        assertEquals(1, leaderboard.size());
        LeaderboardEntryResponse entry = leaderboard.get(0);
        assertEquals(1L, entry.getRank());
        assertEquals("Bob", entry.getDisplayName());
        assertEquals(500, entry.getXp());
        assertEquals(10, entry.getCurrentStreak());
        assertEquals("B2", entry.getLevel());
    }
}
