package com.memora.modules.gamification.entity;

import com.memora.common.domain.BaseEntity;
import jakarta.persistence.*;

/**
 * Catalog entity representing an unlockable system badge or achievement milestone.
 */
@Entity
@Table(name = "achievements",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_achievements_code", columnNames = "code")
        })
public class Achievement extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", nullable = false, length = 512)
    private String description;

    @Column(name = "icon", length = 128)
    private String icon;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    public Achievement() {
    }

    public Achievement(String code, String name, String description, String icon) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.active = true;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Transient
    public String getTitle() {
        return name;
    }

    @Transient
    public int getXpBonus() {
        if (code == null) return 25;
        return switch (code) {
            case "FIRST_LESSON", "FIRST_QUIZ" -> 25;
            case "WORD_STARTER", "PERFECT_SCORE" -> 50;
            case "STREAK_7" -> 75;
            case "VOCABULARY_EXPLORER", "QUIZ_MASTER" -> 100;
            case "MEMORY_MASTER" -> 150;
            case "CENTURY" -> 200;
            case "STREAK_30" -> 250;
            default -> 25;
        };
    }

    @Transient
    public String getBadgeCategory() {
        if (code == null) return "MILESTONE";
        return switch (code) {
            case "STREAK_7", "STREAK_30" -> "STREAK";
            case "MEMORY_MASTER", "WORD_STARTER" -> "MASTERY";
            case "FIRST_QUIZ", "PERFECT_SCORE", "QUIZ_MASTER" -> "QUIZ";
            case "VOCABULARY_EXPLORER", "CENTURY" -> "EXPLORATION";
            default -> "MILESTONE";
        };
    }
}
