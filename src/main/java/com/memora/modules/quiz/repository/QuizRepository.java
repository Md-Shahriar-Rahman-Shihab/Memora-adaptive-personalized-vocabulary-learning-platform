package com.memora.modules.quiz.repository;

import com.memora.modules.quiz.entity.Quiz;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Quiz} operations.
 */
@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByDifficultyLevel(DifficultyLevel difficultyLevel);
}
