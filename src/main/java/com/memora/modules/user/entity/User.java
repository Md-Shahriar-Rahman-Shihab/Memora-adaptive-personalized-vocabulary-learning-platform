package com.memora.modules.user.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import jakarta.persistence.*;

/**
 * User domain entity representing a registered learner or administrator in Memora.
 * Demonstrates Inheritance (extending BaseEntity) and Encapsulation.
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
public class User extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_level", nullable = false)
    private VocabularyLevel currentLevel;

    @Column(name = "xp", nullable = false)
    private int xp;

    @Column(name = "streak", nullable = false)
    private int streak;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    public User() {
    }

    public User(String name, String email, String passwordHash, VocabularyLevel currentLevel, Role role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.currentLevel = currentLevel != null ? currentLevel : VocabularyLevel.A1;
        this.role = role != null ? role : Role.LEARNER;
        this.xp = 0;
        this.streak = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public VocabularyLevel getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(VocabularyLevel currentLevel) {
        this.currentLevel = currentLevel;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
