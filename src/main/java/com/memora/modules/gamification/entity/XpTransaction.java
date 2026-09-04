package com.memora.modules.gamification.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

/**
 * Auditable immutable transaction log for every XP award or bonus received by a learner.
 */
@Entity
@Table(name = "xp_transactions",
        indexes = {
                @Index(name = "idx_xp_user_created", columnList = "user_id, created_at"),
                @Index(name = "idx_xp_user_activity", columnList = "user_id, activity_type")
        })
public class XpTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private RewardActivityType activityType;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "balance_after", nullable = false)
    private int balanceAfter;

    public XpTransaction() {
    }

    public XpTransaction(User user, int amount, RewardActivityType activityType,
                         String description, String referenceId, int balanceAfter) {
        this.user = user;
        this.amount = amount;
        this.activityType = activityType;
        this.description = description;
        this.referenceId = referenceId;
        this.balanceAfter = balanceAfter;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public RewardActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(RewardActivityType activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public int getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(int balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
}
