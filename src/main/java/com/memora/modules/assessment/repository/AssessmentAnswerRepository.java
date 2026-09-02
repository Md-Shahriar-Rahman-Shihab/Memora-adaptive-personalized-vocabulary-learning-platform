package com.memora.modules.assessment.repository;

import com.memora.modules.assessment.entity.AssessmentAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link AssessmentAnswer} entities.
 */
@Repository
public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, Long> {

    List<AssessmentAnswer> findByAssessmentId(Long assessmentId);

    Optional<AssessmentAnswer> findByAssessmentIdAndAssessmentQuestionId(Long assessmentId, Long assessmentQuestionId);

    boolean existsByAssessmentIdAndAssessmentQuestionId(Long assessmentId, Long assessmentQuestionId);
}
