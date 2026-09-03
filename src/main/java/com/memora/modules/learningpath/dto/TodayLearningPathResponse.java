package com.memora.modules.learningpath.dto;

import com.memora.modules.vocabulary.domain.DifficultyLevel;

import java.util.List;

/**
 * Output payload presenting today's prioritized learning items to the learner.
 */
public class TodayLearningPathResponse {

    private String date;
    private Long learningPathId;
    private DifficultyLevel targetLevel;
    private int currentDay;
    private int totalItems;
    private int completedItems;
    private List<LearningPathItemResponse> items;

    public TodayLearningPathResponse() {
    }

    public TodayLearningPathResponse(String date, Long learningPathId, DifficultyLevel targetLevel,
                                     int currentDay, int totalItems, int completedItems,
                                     List<LearningPathItemResponse> items) {
        this.date = date;
        this.learningPathId = learningPathId;
        this.targetLevel = targetLevel;
        this.currentDay = currentDay;
        this.totalItems = totalItems;
        this.completedItems = completedItems;
        this.items = items;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getLearningPathId() {
        return learningPathId;
    }

    public void setLearningPathId(Long learningPathId) {
        this.learningPathId = learningPathId;
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
}
