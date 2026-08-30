package com.memora.modules.user.dto;

import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;

/**
 * Safe user response payload exposing only public learner profile information.
 * Never includes passwords or password hashes.
 */
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private VocabularyLevel currentLevel;
    private int xp;
    private int streak;
    private Role role;

    public UserResponse() {
    }

    public UserResponse(Long id, String name, String email, VocabularyLevel currentLevel, int xp, int streak, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.currentLevel = currentLevel;
        this.xp = xp;
        this.streak = streak;
        this.role = role;
    }

    public static UserResponse fromEntity(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCurrentLevel(),
                user.getXp(),
                user.getStreak(),
                user.getRole()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
