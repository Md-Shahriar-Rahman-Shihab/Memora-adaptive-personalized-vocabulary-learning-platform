package com.memora.modules.assessment.repository;

import com.memora.modules.assessment.entity.AssessmentQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link AssessmentQuestion} entities.
 */
@Repository
public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {

    @Query("SELECT aq FROM AssessmentQuestion aq JOIN FETCH aq.question q WHERE aq.assessment.id = :assessmentId ORDER BY aq.orderIndex ASC")
    List<AssessmentQuestion> findByAssessmentIdOrderByOrderIndexAsc(@Param("assessmentId") Long assessmentId);

    Optional<AssessmentQuestion> findByAssessmentIdAndQuestionId(Long assessmentId, Long questionId);
}
