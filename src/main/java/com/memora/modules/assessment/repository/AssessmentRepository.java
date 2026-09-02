package com.memora.modules.assessment.repository;

import com.memora.modules.assessment.domain.AssessmentStatus;
import com.memora.modules.assessment.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Assessment} entities.
 */
@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByUserIdOrderByStartedAtDesc(Long userId);

    @Query("SELECT a FROM Assessment a WHERE a.user.id = :userId AND a.status = 'IN_PROGRESS' ORDER BY a.startedAt DESC")
    Optional<Assessment> findActiveAssessment(@Param("userId") Long userId);
}
