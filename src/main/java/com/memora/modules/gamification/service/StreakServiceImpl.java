package com.memora.modules.gamification.service;

import com.memora.modules.gamification.entity.UserGamificationProfile;
import com.memora.modules.gamification.repository.UserGamificationProfileRepository;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Core implementation of {@link StreakService}.
 * Adheres to calendar-day boundaries and prevents same-day multiple streak increments.
 */
@Service
@Transactional
public class StreakServiceImpl implements StreakService {

    private final UserGamificationProfileRepository profileRepository;
    private final UserRepository userRepository;

    public StreakServiceImpl(UserGamificationProfileRepository profileRepository,
                             UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserGamificationProfile recordActivityStreak(User user) {
        return recordActivityStreak(user, LocalDate.now(ZoneOffset.UTC));
    }

    @Override
    public UserGamificationProfile recordActivityStreak(User user, LocalDate activityDate) {
        if (user == null || activityDate == null) return null;

        UserGamificationProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> new UserGamificationProfile(user));

        LocalDate lastActivity = profile.getLastActivityDate();

        if (lastActivity == null) {
            // First time activity
            profile.setCurrentStreak(1);
            profile.setLongestStreak(Math.max(1, profile.getLongestStreak()));
            profile.setLastActivityDate(activityDate);
        } else if (lastActivity.equals(activityDate)) {
            // Same calendar day: do not increment streak again
        } else if (lastActivity.equals(activityDate.minusDays(1))) {
            // Consecutive calendar day: increment streak
            int newStreak = profile.getCurrentStreak() + 1;
            profile.setCurrentStreak(newStreak);
            profile.setLastActivityDate(activityDate);
        } else if (activityDate.isAfter(lastActivity)) {
            // Gap of 2 or more days: reset streak to 1
            profile.setCurrentStreak(1);
            profile.setLastActivityDate(activityDate);
        }

        user.setStreak(profile.getCurrentStreak());
        userRepository.save(user);

        return profileRepository.save(profile);
    }
}
