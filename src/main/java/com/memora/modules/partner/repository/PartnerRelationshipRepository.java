package com.memora.modules.partner.repository;

import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.partner.entity.PartnerRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for {@link PartnerRelationship} persistence and queries.
 */
@Repository
public interface PartnerRelationshipRepository extends JpaRepository<PartnerRelationship, Long> {

    Optional<PartnerRelationship> findByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    @Query("SELECT pr FROM PartnerRelationship pr " +
           "JOIN FETCH pr.userOne " +
           "JOIN FETCH pr.userTwo " +
           "JOIN FETCH pr.requestedBy " +
           "WHERE pr.id = :id")
    Optional<PartnerRelationship> findByIdWithUsers(@Param("id") Long id);

    @Query("SELECT pr FROM PartnerRelationship pr " +
           "JOIN FETCH pr.userOne " +
           "JOIN FETCH pr.userTwo " +
           "JOIN FETCH pr.requestedBy " +
           "WHERE pr.status = com.memora.modules.partner.domain.PartnerRelationshipStatus.ACCEPTED " +
           "AND (pr.userOne.id = :userId OR pr.userTwo.id = :userId)")
    List<PartnerRelationship> findActivePartnersForUser(@Param("userId") Long userId);

    @Query("SELECT pr FROM PartnerRelationship pr " +
           "JOIN FETCH pr.userOne " +
           "JOIN FETCH pr.userTwo " +
           "JOIN FETCH pr.requestedBy " +
           "WHERE pr.status = com.memora.modules.partner.domain.PartnerRelationshipStatus.PENDING " +
           "AND (pr.userOne.id = :userId OR pr.userTwo.id = :userId)")
    List<PartnerRelationship> findPendingRequestsForUser(@Param("userId") Long userId);

    @Query("SELECT pr FROM PartnerRelationship pr " +
           "WHERE pr.status = com.memora.modules.partner.domain.PartnerRelationshipStatus.ACCEPTED " +
           "AND ((pr.userOne.id = :userAId AND pr.userTwo.id = :userBId) OR (pr.userOne.id = :userBId AND pr.userTwo.id = :userAId))")
    Optional<PartnerRelationship> findActiveRelationshipBetweenUsers(@Param("userAId") Long userAId, @Param("userBId") Long userBId);

    @Query("SELECT pr FROM PartnerRelationship pr " +
           "WHERE (pr.userOne.id = :userId OR pr.userTwo.id = :userId)")
    List<PartnerRelationship> findAllRelationshipsForUser(@Param("userId") Long userId);

    default Optional<PartnerRelationship> findRelationshipBetween(Long id1, Long id2) {
        if (id1 == null || id2 == null) {
            return Optional.empty();
        }
        Long min = Math.min(id1, id2);
        Long max = Math.max(id1, id2);
        return findByUserOneIdAndUserTwoId(min, max);
    }
}
