package com.memora.modules.learningpath.repository;

import com.memora.modules.learningpath.domain.LearningItemStatus;
import com.memora.modules.learningpath.entity.LearningPathItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link LearningPathItem} entities.
 */
@Repository
public interface LearningPathItemRepository extends JpaRepository<LearningPathItem, Long> {

    List<LearningPathItem> findByLearningPathIdOrderByOrderIndexAsc(Long learningPathId);

    List<LearningPathItem> findByLearningPathIdAndStatusOrderByOrderIndexAsc(Long learningPathId, LearningItemStatus status);

    Optional<LearningPathItem> findByLearningPathIdAndOrderIndex(Long learningPathId, int orderIndex);

    void deleteByLearningPathIdAndStatus(Long learningPathId, LearningItemStatus status);
}
