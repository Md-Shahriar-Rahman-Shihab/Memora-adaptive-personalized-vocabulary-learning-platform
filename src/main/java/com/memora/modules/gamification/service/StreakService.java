package com.memora.modules.gamification.service;

import com.memora.modules.gamification.entity.UserGamificationProfile;
import com.memora.modules.user.entity.User;

import java.time.LocalDate;

/**
 * Service contract for calendar-day learning streak tracking and longest streak preservation.
 */
public interface StreakService {

    /**
     * Records a meaningful learning activity for the learner today, advancing or resetting the streak.
     *
     * @param user The learner {@link User}
     * @return Updated {@link UserGamificationProfile}
     */
    UserGamificationProfile recordActivityStreak(User user);

    /**
     * Records a learning activity on a specified date (useful for timezone-adjusted activity recording and testing).
     */
    UserGamificationProfile recordActivityStreak(User user, LocalDate activityDate);
}
