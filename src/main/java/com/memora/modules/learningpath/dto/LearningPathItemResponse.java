package com.memora.modules.learningpath.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.memora.modules.learningpath.domain.LearningItemPriority;
import com.memora.modules.learningpath.domain.LearningItemStatus;
import com.memora.modules.learningpath.domain.LearningItemType;

import java.time.Instant;

/**
 * Output representation of an individual learning path item.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningPathItemResponse {

    private Long id;
    private LearningItemType type;
    private Long wordId;
    private String word;
    private String meaning;
    private Long quizId;
    private LearningItemPriority priority;
    private LearningItemStatus status;
    private int orderIndex;
    private String notes;
    private Instant scheduledAt;
    private Instant completedAt;

    public LearningPathItemResponse() {
    }

    public LearningPathItemResponse(Long id, LearningItemType type, Long wordId, String word,
                                    String meaning, Long quizId, LearningItemPriority priority,
                                    LearningItemStatus status, int orderIndex, String notes,
                                    Instant scheduledAt, Instant completedAt) {
        this.id = id;
        this.type = type;
        this.wordId = wordId;
        this.word = word;
        this.meaning = meaning;
        this.quizId = quizId;
        this.priority = priority;
        this.status = status;
        this.orderIndex = orderIndex;
        this.notes = notes;
        this.scheduledAt = scheduledAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LearningItemType getType() {
        return type;
    }

    public void setType(LearningItemType type) {
        this.type = type;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public LearningItemPriority getPriority() {
        return priority;
    }

    public void setPriority(LearningItemPriority priority) {
        this.priority = priority;
    }

    public LearningItemStatus getStatus() {
        return status;
    }

    public void setStatus(LearningItemStatus status) {
        this.status = status;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
