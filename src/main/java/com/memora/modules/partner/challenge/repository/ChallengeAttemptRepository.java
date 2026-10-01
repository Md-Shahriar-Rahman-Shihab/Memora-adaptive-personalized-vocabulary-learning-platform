package com.memora.modules.partner.challenge.repository;

import com.memora.modules.partner.challenge.entity.ChallengeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link ChallengeAttempt} management.
 */
@Repository
public interface ChallengeAttemptRepository extends JpaRepository<ChallengeAttempt, Long> {

    @Query("SELECT ca FROM ChallengeAttempt ca " +
           "LEFT JOIN FETCH ca.questionAttempts qa " +
           "LEFT JOIN FETCH qa.question " +
           "WHERE ca.challenge.id = :challengeId AND ca.user.id = :userId")
    Optional<ChallengeAttempt> findByChallengeIdAndUserId(@Param("challengeId") Long challengeId,
                                                          @Param("userId") Long userId);

    @Query("SELECT ca FROM ChallengeAttempt ca " +
           "LEFT JOIN FETCH ca.questionAttempts qa " +
           "LEFT JOIN FETCH qa.question " +
           "WHERE ca.challenge.id = :challengeId")
    List<ChallengeAttempt> findByChallengeId(@Param("challengeId") Long challengeId);
}
