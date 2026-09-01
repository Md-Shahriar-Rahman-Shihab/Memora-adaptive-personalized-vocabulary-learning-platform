package com.memora.modules.quiz.repository;

import com.memora.modules.quiz.entity.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link QuestionAttempt} operations.
 */
@Repository
public interface QuestionAttemptRepository extends JpaRepository<QuestionAttempt, Long> {

    List<QuestionAttempt> findByQuizAttemptId(Long quizAttemptId);

    Optional<QuestionAttempt> findByQuizAttemptIdAndQuestionId(Long quizAttemptId, Long questionId);
}
