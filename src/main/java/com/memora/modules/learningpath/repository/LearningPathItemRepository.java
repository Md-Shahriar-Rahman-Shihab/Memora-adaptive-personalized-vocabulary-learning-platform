package com.memora.modules.learningpath.repository;

import com.memora.modules.learningpath.domain.LearningItemStatus;
import com.memora.modules.learningpath.entity.LearningPathItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA repository for {@link LearningPathItem} entities.
 */
@Repository
public interface LearningPathItemRepository extends JpaRepository<LearningPathItem, Long> {

    List<LearningPathItem> findByLearningPathIdOrderByOrderIndexAsc(Long learningPathId);

    List<LearningPathItem> findByLearningPathIdAndStatusOrderByOrderIndexAsc(Long learningPathId, LearningItemStatus status);

    Optional<LearningPathItem> findByLearningPathIdAndOrderIndex(Long learningPathId, int orderIndex);

    void deleteByLearningPathIdAndStatus(Long learningPathId, LearningItemStatus status);

    List<LearningPathItem> findByQuizId(Long quizId);

    @Query("SELECT DISTINCT lpi.vocabularyWord.id FROM LearningPathItem lpi WHERE lpi.learningPath.user.id = :userId AND lpi.vocabularyWord IS NOT NULL")
    Set<Long> findLearnedWordIdsByUserId(@Param("userId") Long userId);
}
