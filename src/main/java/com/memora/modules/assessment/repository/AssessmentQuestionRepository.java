package com.memora.modules.assessment.repository;

import com.memora.modules.assessment.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link AssessmentQuestion} entities.
 */
@Repository
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {

    List<AssessmentQuestion> findByAssessmentIdOrderByOrderIndexAsc(Long assessmentId);

    Optional<AssessmentQuestion> findByAssessmentIdAndQuestionId(Long assessmentId, Long questionId);
}
