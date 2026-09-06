package com.memora.modules.gamification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.memora.modules.gamification.domain.RewardActivityType;

import java.time.Instant;

/**
 * Output DTO representing a historical XP award transaction.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XpTransactionResponse {

    private Long id;
    private int amount;
    private RewardActivityType activityType;
    private String description;
    private String referenceId;
    private int balanceAfter;
    private Instant createdAt;

    public XpTransactionResponse() {
    }

    public XpTransactionResponse(Long id, int amount, RewardActivityType activityType,
                                 String description, String referenceId, int balanceAfter,
                                 Instant createdAt) {
        this.id = id;
        this.amount = amount;
        this.activityType = activityType;
        this.description = description;
        this.referenceId = referenceId;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getXpEarned() {
        return amount;
    }

    public int getResultingTotalXp() {
        return balanceAfter;
    }

    public String getSourceActivity() {
        return activityType != null ? activityType.name() : null;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
