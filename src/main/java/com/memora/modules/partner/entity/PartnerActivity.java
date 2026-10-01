package com.memora.modules.partner.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.partner.domain.PartnerActivityType;
import com.memora.modules.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Domain entity representing an observable, privacy-safe learning activity
 * performed between accepted learning partners (e.g. duels completed, won, milestones).
 */
@Entity
@Table(name = "partner_activities",
        indexes = {
                @Index(name = "idx_partner_act_rel", columnList = "relationship_id"),
                @Index(name = "idx_partner_act_source", columnList = "relationship_id, source_entity_id"),
                @Index(name = "idx_partner_act_created", columnList = "created_at")
        })
public class PartnerActivity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relationship_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PartnerRelationship relationship;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private PartnerActivityType activityType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "details")
    private String details;

    @Column(name = "xp_earned")
    private Integer xpEarned;

    @Column(name = "source_entity_id")
    private Long sourceEntityId;

    public PartnerActivity() {
    }

    public PartnerActivity(PartnerRelationship relationship,
                           User actor,
                           PartnerActivityType activityType,
                           String title,
                           String details,
                           Integer xpEarned) {
        this.relationship = relationship;
        this.actor = actor;
        this.activityType = activityType;
        this.title = title;
        this.details = details;
        this.xpEarned = xpEarned != null ? xpEarned : 0;
    }

    public PartnerRelationship getRelationship() {
        return relationship;
    }

    public void setRelationship(PartnerRelationship relationship) {
        this.relationship = relationship;
    }

    public User getActor() {
        return actor;
    }

    public void setActor(User actor) {
        this.actor = actor;
    }

    public PartnerActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(PartnerActivityType activityType) {
        this.activityType = activityType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }

    public Long getSourceEntityId() {
        return sourceEntityId;
    }

    public void setSourceEntityId(Long sourceEntityId) {
        this.sourceEntityId = sourceEntityId;
    }
}
