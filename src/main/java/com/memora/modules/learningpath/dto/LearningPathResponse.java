package com.memora.modules.learningpath.dto;

import com.memora.modules.learningpath.domain.LearningPathStatus;
import com.memora.modules.vocabulary.domain.DifficultyLevel;

import java.time.Instant;
import java.util.List;

/**
 * Output payload summarizing a complete LearningPath instance and its items.
 */
public class LearningPathResponse {

    private Long id;
    private LearningPathStatus status;
    private DifficultyLevel targetLevel;
    private int currentDay;
    private int totalItems;
    private int completedItems;
    private List<LearningPathItemResponse> items;
    private Instant createdAt;

    public LearningPathResponse() {
    }

    public LearningPathResponse(Long id, LearningPathStatus status, DifficultyLevel targetLevel,
                                int currentDay, int totalItems, int completedItems,
                                List<LearningPathItemResponse> items, Instant createdAt) {
        this.id = id;
        this.status = status;
        this.targetLevel = targetLevel;
        this.currentDay = currentDay;
        this.totalItems = totalItems;
        this.completedItems = completedItems;
        this.items = items;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LearningPathStatus getStatus() {
        return status;
    }

    public void setStatus(LearningPathStatus status) {
        this.status = status;
    }

    public DifficultyLevel getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(DifficultyLevel targetLevel) {
        this.targetLevel = targetLevel;
    }

    public int getCurrentDay() {
        return currentDay;
    }

    public void setCurrentDay(int currentDay) {
        this.currentDay = currentDay;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getCompletedItems() {
        return completedItems;
    }

    public void setCompletedItems(int completedItems) {
        this.completedItems = completedItems;
    }

    public List<LearningPathItemResponse> getItems() {
        return items;
    }

    public void setItems(List<LearningPathItemResponse> items) {
        this.items = items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
