package com.memora.modules.gamification.repository;

import com.memora.modules.gamification.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link UserAchievement}.
 */
@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    List<UserAchievement> findByUserIdOrderByEarnedAtDesc(Long userId);

    @Query("SELECT COUNT(ua) > 0 FROM UserAchievement ua WHERE ua.user.id = :userId AND ua.achievement.code = :code")
    boolean existsByUserIdAndAchievementCode(@Param("userId") Long userId, @Param("code") String code);

    long countByUserId(Long userId);
}
