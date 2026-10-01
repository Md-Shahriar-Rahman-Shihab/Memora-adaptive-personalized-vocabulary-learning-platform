package com.memora.modules.partner.challenge.repository;

import com.memora.modules.partner.challenge.domain.ChallengeStatus;
import com.memora.modules.partner.challenge.entity.VocabularyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link VocabularyChallenge} persistence and queries.
 */
@Repository
public interface VocabularyChallengeRepository extends JpaRepository<VocabularyChallenge, Long> {

    @Query("SELECT c FROM VocabularyChallenge c " +
           "JOIN FETCH c.challenger " +
           "JOIN FETCH c.challengedUser " +
           "LEFT JOIN FETCH c.winner " +
           "LEFT JOIN FETCH c.quiz q " +
           "LEFT JOIN FETCH q.questions " +
           "WHERE c.id = :id")
    Optional<VocabularyChallenge> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT c FROM VocabularyChallenge c " +
           "JOIN FETCH c.challenger " +
           "JOIN FETCH c.challengedUser " +
           "LEFT JOIN FETCH c.winner " +
           "WHERE c.challenger.id = :userId OR c.challengedUser.id = :userId " +
           "ORDER BY c.createdAt DESC")
    List<VocabularyChallenge> findAllForUser(@Param("userId") Long userId);

    @Query("SELECT c FROM VocabularyChallenge c " +
           "JOIN FETCH c.challenger " +
           "JOIN FETCH c.challengedUser " +
           "LEFT JOIN FETCH c.winner " +
           "WHERE (c.challenger.id = :userId OR c.challengedUser.id = :userId) " +
           "AND c.status = :status " +
           "ORDER BY c.createdAt DESC")
    List<VocabularyChallenge> findForUserByStatus(@Param("userId") Long userId, @Param("status") ChallengeStatus status);

    @Query("SELECT c FROM VocabularyChallenge c " +
           "WHERE ((c.challenger.id = :userAId AND c.challengedUser.id = :userBId) " +
           "    OR (c.challenger.id = :userBId AND c.challengedUser.id = :userAId)) " +
           "AND c.status IN (:activeStatuses)")
    List<VocabularyChallenge> findActiveBetween(@Param("userAId") Long userAId,
                                                @Param("userBId") Long userBId,
                                                @Param("activeStatuses") List<ChallengeStatus> activeStatuses);
}
