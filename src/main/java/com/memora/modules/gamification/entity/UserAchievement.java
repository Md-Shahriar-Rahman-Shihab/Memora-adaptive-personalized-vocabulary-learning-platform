package com.memora.modules.gamification.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

/**
 * Domain entity recording that an authenticated learner has unlocked a specific achievement.
 * Strict unique constraint ensures idempotent awarding.
 */
@Entity
@Table(name = "user_achievements",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_achievement", columnNames = {"user_id", "achievement_id"})
        },
        indexes = {
                @Index(name = "idx_ua_user_id", columnList = "user_id"),
                @Index(name = "idx_ua_user_earned", columnList = "user_id, earned_at")
        })
public class UserAchievement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;

    @Column(name = "earned_at", nullable = false)
    private Instant earnedAt;

    public UserAchievement() {
    }

    public UserAchievement(User user, Achievement achievement) {
        this.user = user;
        this.achievement = achievement;
        this.earnedAt = Instant.now();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Achievement getAchievement() {
        return achievement;
    }

    public void setAchievement(Achievement achievement) {
        this.achievement = achievement;
    }

    public Instant getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(Instant earnedAt) {
        this.earnedAt = earnedAt;
    }
}
