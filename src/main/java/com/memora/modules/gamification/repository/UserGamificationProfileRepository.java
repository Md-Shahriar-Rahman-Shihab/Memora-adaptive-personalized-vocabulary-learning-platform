package com.memora.modules.gamification.repository;

import com.memora.modules.gamification.entity.UserGamificationProfile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserGamificationProfile}.
 */
@Repository
public interface UserGamificationProfileRepository extends JpaRepository<UserGamificationProfile, Long> {

    Optional<UserGamificationProfile> findByUserId(Long userId);

    @Query("SELECT p FROM UserGamificationProfile p WHERE p.user.email = :email")
    Optional<UserGamificationProfile> findByUserEmail(@Param("email") String email);

    @Query("SELECT p FROM UserGamificationProfile p JOIN FETCH p.user u WHERE u.role = com.memora.modules.user.domain.Role.LEARNER ORDER BY p.totalXp DESC, p.currentStreak DESC, p.createdAt ASC, p.id ASC")
    List<UserGamificationProfile> findLeaderboard(Pageable pageable);

    @Query("SELECT COUNT(p) + 1 FROM UserGamificationProfile p WHERE p.totalXp > :xp OR (p.totalXp = :xp AND p.currentStreak > :streak)")
    long calculateRank(@Param("xp") int xp, @Param("streak") int streak);
}
