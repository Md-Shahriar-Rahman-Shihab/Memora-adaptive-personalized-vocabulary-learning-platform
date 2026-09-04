package com.memora.modules.gamification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Output DTO representing an achievement badge earned by a learner.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserAchievementResponse {

    private String code;
    private String name;
    private String description;
    private String icon;
    private Instant earnedAt;

    public UserAchievementResponse() {
    }

    public UserAchievementResponse(String code, String name, String description, String icon, Instant earnedAt) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.earnedAt = earnedAt;
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

    public Instant getEarnedAt() {
        return earnedAt;
    }

    public void setEarnedAt(Instant earnedAt) {
        this.earnedAt = earnedAt;
    }
}
