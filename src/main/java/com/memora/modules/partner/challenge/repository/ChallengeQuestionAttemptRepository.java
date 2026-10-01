package com.memora.modules.partner.challenge.repository;

import com.memora.modules.partner.challenge.entity.ChallengeQuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA Repository for {@link ChallengeQuestionAttempt}.
 */
@Repository
public interface ChallengeQuestionAttemptRepository extends JpaRepository<ChallengeQuestionAttempt, Long> {
}
