package com.memora.modules.vocabulary.repository;

import com.memora.modules.user.entity.User;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserWordProgress} persistence and retrieval.
 */
@Repository
public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {

    Optional<UserWordProgress> findByUserAndVocabularyWord(User user, VocabularyWord vocabularyWord);

    Optional<UserWordProgress> findByUserIdAndVocabularyWordId(Long userId, Long vocabularyWordId);

    List<UserWordProgress> findByUser(User user);

    List<UserWordProgress> findByUserAndForgettingRisk(User user, ForgettingRisk forgettingRisk);

    @Query("SELECT uwp FROM UserWordProgress uwp WHERE uwp.user = :user AND uwp.nextReviewAt IS NOT NULL AND uwp.nextReviewAt <= :now ORDER BY uwp.nextReviewAt ASC")
    List<UserWordProgress> findDueForReview(@Param("user") User user, @Param("now") Instant now);

    @Query("SELECT uwp FROM UserWordProgress uwp WHERE uwp.user = :user AND (uwp.forgettingRisk IN (com.memora.modules.vocabulary.domain.ForgettingRisk.HIGH, com.memora.modules.vocabulary.domain.ForgettingRisk.CRITICAL) OR uwp.incorrectAttempts > uwp.correctAttempts) ORDER BY uwp.incorrectAttempts DESC, uwp.masteryScore ASC")
    List<UserWordProgress> findWeakWords(@Param("user") User user);
}
