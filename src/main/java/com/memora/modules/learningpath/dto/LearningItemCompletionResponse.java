package com.memora.modules.learningpath.dto;

import com.memora.modules.learningpath.domain.LearningItemStatus;
import com.memora.modules.learningpath.domain.LearningItemType;

import java.time.Instant;

/**
 * Output payload returned when a learning path item is marked as completed.
 */
public class LearningItemCompletionResponse {

    private Long itemId;
    private LearningItemStatus status;
    private LearningItemType itemType;
    private Instant completedAt;
    private boolean pathCompleted;
    private int completedItems;
    private int totalItems;
    private String message;
    private Integer xpEarned;

    public LearningItemCompletionResponse() {
    }

    public LearningItemCompletionResponse(Long itemId, LearningItemStatus status, LearningItemType itemType,
                                          Instant completedAt, boolean pathCompleted, int completedItems,
                                          int totalItems, String message) {
        this(itemId, status, itemType, completedAt, pathCompleted, completedItems, totalItems, message, null);
    }

    public LearningItemCompletionResponse(Long itemId, LearningItemStatus status, LearningItemType itemType,
                                          Instant completedAt, boolean pathCompleted, int completedItems,
                                          int totalItems, String message, Integer xpEarned) {
        this.itemId = itemId;
        this.status = status;
        this.itemType = itemType;
        this.completedAt = completedAt;
        this.pathCompleted = pathCompleted;
        this.completedItems = completedItems;
        this.totalItems = totalItems;
        this.message = message;
        this.xpEarned = xpEarned;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public LearningItemStatus getStatus() {
        return status;
    }

    public void setStatus(LearningItemStatus status) {
        this.status = status;
    }

    public LearningItemType getItemType() {
        return itemType;
    }

    public void setItemType(LearningItemType itemType) {
        this.itemType = itemType;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isPathCompleted() {
        return pathCompleted;
    }

    public void setPathCompleted(boolean pathCompleted) {
        this.pathCompleted = pathCompleted;
    }

    public int getCompletedItems() {
        return completedItems;
    }

    public void setCompletedItems(int completedItems) {
        this.completedItems = completedItems;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }
}
