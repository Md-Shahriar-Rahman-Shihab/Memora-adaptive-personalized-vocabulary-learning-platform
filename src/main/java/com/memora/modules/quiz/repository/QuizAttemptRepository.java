package com.memora.modules.quiz.repository;

import com.memora.modules.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link QuizAttempt} operations.
 */
@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(Long userId);

    List<QuizAttempt> findByQuizIdAndUserIdOrderByStartedAtDesc(Long quizId, Long userId);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.user.id = :userId ORDER BY qa.startedAt DESC")
    List<QuizAttempt> findLatestAttemptsForQuiz(@Param("quizId") Long quizId, @Param("userId") Long userId);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.quiz.id = :quizId AND qa.user.id = :userId AND qa.completedAt IS NULL ORDER BY qa.startedAt DESC")
    List<QuizAttempt> findActiveAttemptsList(@Param("quizId") Long quizId, @Param("userId") Long userId);

    default Optional<QuizAttempt> findActiveAttempt(Long quizId, Long userId) {
        List<QuizAttempt> list = findActiveAttemptsList(quizId, userId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
