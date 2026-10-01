package com.memora.modules.partner.repository;

import com.memora.modules.partner.domain.PartnerActivityType;
import com.memora.modules.partner.entity.PartnerActivity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA Repository for {@link PartnerActivity} persistence and queries.
 */
@Repository
public interface PartnerActivityRepository extends JpaRepository<PartnerActivity, Long> {

    @Query("SELECT pa FROM PartnerActivity pa " +
           "JOIN FETCH pa.actor " +
           "JOIN pa.relationship pr " +
           "WHERE pr.status = com.memora.modules.partner.domain.PartnerRelationshipStatus.ACCEPTED " +
           "AND (pr.userOne.id = :userId OR pr.userTwo.id = :userId) " +
           "ORDER BY pa.createdAt DESC, pa.id DESC")
    List<PartnerActivity> findRecentActivitiesForUser(@Param("userId") Long userId, Pageable pageable);

    boolean existsByRelationshipIdAndActivityTypeAndTitle(Long relationshipId,
                                                          PartnerActivityType activityType,
                                                          String title);

    boolean existsByRelationshipIdAndActivityTypeAndActorIdAndSourceEntityId(Long relationshipId,
                                                                             PartnerActivityType activityType,
                                                                             Long actorId,
                                                                             Long sourceEntityId);
}
