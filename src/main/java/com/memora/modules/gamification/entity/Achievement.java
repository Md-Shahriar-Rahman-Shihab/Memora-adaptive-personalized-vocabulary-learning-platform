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
}
