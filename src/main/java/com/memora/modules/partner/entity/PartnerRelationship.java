package com.memora.modules.partner.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.user.entity.User;
import jakarta.persistence.*;

/**
 * Domain entity representing a persistent, bidirectional learning partner relationship
 * between two Memora learners.
 *
 * Implements canonical pair ordering: userOne.id is strictly less than userTwo.id.
 * Demonstrates Abstraction, Encapsulation, and BaseEntity inheritance.
 */
@Entity
@Table(name = "partner_relationships",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_partner_pair", columnNames = {"user_one_id", "user_two_id"})
        },
        indexes = {
                @Index(name = "idx_partner_user_one", columnList = "user_one_id"),
                @Index(name = "idx_partner_user_two", columnList = "user_two_id"),
                @Index(name = "idx_partner_status", columnList = "status")
        })
public class PartnerRelationship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PartnerRelationshipStatus status;

    public PartnerRelationship() {
    }

    public PartnerRelationship(User userOne, User userTwo, User requestedBy, PartnerRelationshipStatus status) {
        if (userOne == null || userTwo == null || requestedBy == null) {
            throw new IllegalArgumentException("Users and requestedBy cannot be null");
        }
        if (userOne.getId() != null && userTwo.getId() != null && userOne.getId() >= userTwo.getId()) {
            throw new IllegalArgumentException("Canonical ordering violation: userOne.id must be strictly less than userTwo.id");
        }
        this.userOne = userOne;
        this.userTwo = userTwo;
        this.requestedBy = requestedBy;
        this.status = status != null ? status : PartnerRelationshipStatus.PENDING;
    }

    public User getUserOne() {
        return userOne;
    }

    public void setUserOne(User userOne) {
        this.userOne = userOne;
    }

    public User getUserTwo() {
        return userTwo;
    }

    public void setUserTwo(User userTwo) {
        this.userTwo = userTwo;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public PartnerRelationshipStatus getStatus() {
        return status;
    }

    public void setStatus(PartnerRelationshipStatus status) {
        this.status = status;
    }

    /**
     * Retrieves the partner of the specified user within this relationship.
     */
    public User getOtherUser(Long userId) {
        if (userOne != null && userOne.getId() != null && userOne.getId().equals(userId)) {
            return userTwo;
        }
        return userOne;
    }

    /**
     * Checks if the specified user is the sender of this request.
     */
    public boolean isSender(Long userId) {
        return requestedBy != null && requestedBy.getId() != null && requestedBy.getId().equals(userId);
    }

    /**
     * Checks if the specified user is an involved participant in this relationship.
     */
    public boolean isInvolved(Long userId) {
        return (userOne != null && userOne.getId() != null && userOne.getId().equals(userId))
                || (userTwo != null && userTwo.getId() != null && userTwo.getId().equals(userId));
    }
}
