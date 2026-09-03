package com.memora.modules.learningpath.repository;

import com.memora.modules.learningpath.domain.LearningPathStatus;
import com.memora.modules.learningpath.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link LearningPath} entities.
 */
@Repository
public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {

    Optional<LearningPath> findByUserIdAndStatus(Long userId, LearningPathStatus status);

    List<LearningPath> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT lp FROM LearningPath lp WHERE lp.user.id = :userId AND lp.status = 'ACTIVE' ORDER BY lp.createdAt DESC")
    Optional<LearningPath> findActivePathByUserId(@Param("userId") Long userId);
}
