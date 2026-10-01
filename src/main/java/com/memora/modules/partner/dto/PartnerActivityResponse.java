package com.memora.modules.partner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Privacy-safe representation of a partner activity event.
 * Never exposes actor email, passwords, authentication data, or private user records.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PartnerActivityResponse {

    private Long id;
    private ActorSummary actor;
    private String activityType;
    private String title;
    private String details;
    private Integer xpEarned;
    private Instant createdAt;

    public PartnerActivityResponse() {
    }

    public PartnerActivityResponse(Long id,
                                   ActorSummary actor,
                                   String activityType,
                                   String title,
                                   String details,
                                   Integer xpEarned,
                                   Instant createdAt) {
        this.id = id;
        this.actor = actor;
        this.activityType = activityType;
        this.title = title;
        this.details = details;
        this.xpEarned = xpEarned;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ActorSummary getActor() {
        return actor;
    }

    public void setActor(ActorSummary actor) {
        this.actor = actor;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public static class ActorSummary {
        private Long id;
        private String name;

        public ActorSummary() {
        }

        public ActorSummary(Long id, String name) {
            this.id = id;
            this.name = name;
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
    }
}
