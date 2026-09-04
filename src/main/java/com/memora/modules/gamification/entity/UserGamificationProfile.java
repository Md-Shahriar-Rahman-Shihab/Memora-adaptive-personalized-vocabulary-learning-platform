package com.memora.modules.gamification.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.user.entity.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

/**
 * Domain entity representing a learner's persistent gamification profile,
 * tracking cumulative XP points, current consecutive learning streak, and longest streak.
 */
@Entity
@Table(name = "user_gamification_profiles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_gamification_profile_user", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_ugp_user_id", columnList = "user_id"),
                @Index(name = "idx_ugp_xp_streak", columnList = "total_xp, current_streak")
        })
public class UserGamificationProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "total_xp", nullable = false)
    private int totalXp = 0;

    @Column(name = "current_streak", nullable = false)
    private int currentStreak = 0;

    @Column(name = "longest_streak", nullable = false)
    private int longestStreak = 0;

    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    public UserGamificationProfile() {
    }

    public UserGamificationProfile(User user) {
        this.user = user;
        this.totalXp = user != null ? user.getXp() : 0;
        this.currentStreak = user != null ? user.getStreak() : 0;
        this.longestStreak = this.currentStreak;
    }

    public void addXp(int points) {
        if (points > 0) {
            this.totalXp += points;
            if (this.user != null) {
                this.user.setXp(this.totalXp);
            }
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
        if (this.user != null) {
            this.user.setXp(totalXp);
        }
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
        if (currentStreak > this.longestStreak) {
            this.longestStreak = currentStreak;
        }
        if (this.user != null) {
            this.user.setStreak(currentStreak);
        }
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(LocalDate lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }
}
