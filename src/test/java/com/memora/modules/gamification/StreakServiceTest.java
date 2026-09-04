package com.memora.modules.gamification;

import com.memora.modules.gamification.entity.UserGamificationProfile;
import com.memora.modules.gamification.repository.UserGamificationProfileRepository;
import com.memora.modules.gamification.service.StreakServiceImpl;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {

    @Mock
    private UserGamificationProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    private StreakServiceImpl streakService;
    private User testUser;
    private UserGamificationProfile profile;

    @BeforeEach
    void setUp() {
        streakService = new StreakServiceImpl(profileRepository, userRepository);

        testUser = new User("Learner", "learner@memora.com", "hash", VocabularyLevel.A1, Role.LEARNER);
        ReflectionTestUtils.setField(testUser, "id", 1L);

        profile = new UserGamificationProfile(testUser);
        ReflectionTestUtils.setField(profile, "id", 10L);
    }

    @Test
    @DisplayName("First activity should initialize streak to 1 and longest streak to 1")
    void testFirstActivityInitializesStreak() {
        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserGamificationProfile.class))).thenAnswer(i -> i.getArgument(0));

        LocalDate today = LocalDate.of(2026, 9, 1);
        UserGamificationProfile result = streakService.recordActivityStreak(testUser, today);

        assertEquals(1, result.getCurrentStreak());
        assertEquals(1, result.getLongestStreak());
        assertEquals(today, result.getLastActivityDate());
        assertEquals(1, testUser.getStreak());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Activity on consecutive day should increment streak and update longest streak")
    void testNextDayIncrementsStreak() {
        LocalDate day1 = LocalDate.of(2026, 9, 1);
        profile.setCurrentStreak(1);
        profile.setLongestStreak(1);
        profile.setLastActivityDate(day1);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserGamificationProfile.class))).thenAnswer(i -> i.getArgument(0));

        LocalDate day2 = LocalDate.of(2026, 9, 2);
        UserGamificationProfile result = streakService.recordActivityStreak(testUser, day2);

        assertEquals(2, result.getCurrentStreak());
        assertEquals(2, result.getLongestStreak());
        assertEquals(day2, result.getLastActivityDate());
    }

    @Test
    @DisplayName("Multiple activities on the same day should NOT increment streak again")
    void testSameDayDoesNotIncrementStreak() {
        LocalDate today = LocalDate.of(2026, 9, 1);
        profile.setCurrentStreak(3);
        profile.setLongestStreak(5);
        profile.setLastActivityDate(today);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserGamificationProfile.class))).thenAnswer(i -> i.getArgument(0));

        UserGamificationProfile result = streakService.recordActivityStreak(testUser, today);

        assertEquals(3, result.getCurrentStreak());
        assertEquals(5, result.getLongestStreak());
        assertEquals(today, result.getLastActivityDate());
    }

    @Test
    @DisplayName("Activity after missed day should reset streak to 1 while preserving longest streak")
    void testMissedDayResetsStreak() {
        LocalDate past = LocalDate.of(2026, 9, 1);
        profile.setCurrentStreak(5);
        profile.setLongestStreak(10);
        profile.setLastActivityDate(past);

        when(profileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(UserGamificationProfile.class))).thenAnswer(i -> i.getArgument(0));

        LocalDate gapDay = LocalDate.of(2026, 9, 4); // 3 days gap
        UserGamificationProfile result = streakService.recordActivityStreak(testUser, gapDay);

        assertEquals(1, result.getCurrentStreak());
        assertEquals(10, result.getLongestStreak(), "Longest streak must be preserved on reset");
        assertEquals(gapDay, result.getLastActivityDate());
    }
}
