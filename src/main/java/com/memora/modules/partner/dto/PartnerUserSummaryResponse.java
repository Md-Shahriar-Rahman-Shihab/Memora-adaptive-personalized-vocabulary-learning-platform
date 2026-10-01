package com.memora.modules.partner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Privacy-safe public summary of a Memora learner.
 * Excludes passwords, emails, credentials, private stats, and internal audit fields.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartnerUserSummaryResponse {

    private Long id;
    private String name;
    private String currentLevel;
    private int xp;
    private int streak;
    private String relationshipStatus;

    public PartnerUserSummaryResponse() {
    }

    public PartnerUserSummaryResponse(Long id, String name, String currentLevel, int xp, int streak) {
        this.id = id;
        this.name = name;
        this.currentLevel = currentLevel;
        this.xp = xp;
        this.streak = streak;
    }

    public PartnerUserSummaryResponse(Long id, String name, String currentLevel, int xp, int streak, String relationshipStatus) {
        this.id = id;
        this.name = name;
        this.currentLevel = currentLevel;
        this.xp = xp;
        this.streak = streak;
        this.relationshipStatus = relationshipStatus;
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

    public String getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(String currentLevel) {
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

    public String getRelationshipStatus() {
        return relationshipStatus;
    }

    public void setRelationshipStatus(String relationshipStatus) {
        this.relationshipStatus = relationshipStatus;
    }
}
