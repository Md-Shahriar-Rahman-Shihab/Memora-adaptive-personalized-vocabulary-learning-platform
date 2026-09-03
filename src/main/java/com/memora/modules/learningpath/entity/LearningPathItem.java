package com.memora.modules.learningpath.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.learningpath.domain.LearningItemPriority;
import com.memora.modules.learningpath.domain.LearningItemStatus;
import com.memora.modules.learningpath.domain.LearningItemType;
import com.memora.modules.quiz.entity.Quiz;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Domain entity representing an individual learning task (new word introduction, review item, or quiz)
 * within a LearningPath.
 */
@Entity
@Table(name = "learning_path_items",
        indexes = {
                @Index(name = "idx_lpi_path_status", columnList = "learning_path_id, status"),
                @Index(name = "idx_lpi_path_scheduled", columnList = "learning_path_id, scheduled_at"),
                @Index(name = "idx_lpi_path", columnList = "learning_path_id")
        })
public class LearningPathItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learning_path_id", nullable = false)
    private LearningPath learningPath;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vocabulary_word_id")
    private VocabularyWord vocabularyWord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private LearningItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private LearningItemPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LearningItemStatus status = LearningItemStatus.PENDING;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public LearningPathItem() {
    }

    public LearningPathItem(LearningPath learningPath,
                            VocabularyWord vocabularyWord,
                            Quiz quiz,
                            LearningItemType itemType,
                            LearningItemPriority priority,
                            int orderIndex,
                            String notes) {
        this.learningPath = learningPath;
        this.vocabularyWord = vocabularyWord;
        this.quiz = quiz;
        this.itemType = itemType;
        this.priority = priority;
        this.orderIndex = orderIndex;
        this.notes = notes;
        this.status = LearningItemStatus.PENDING;
        this.scheduledAt = Instant.now();
    }

    public void markInProgress() {
        if (this.status == LearningItemStatus.PENDING) {
            this.status = LearningItemStatus.IN_PROGRESS;
        }
    }

    public void markCompleted() {
        this.status = LearningItemStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void markSkipped() {
        this.status = LearningItemStatus.SKIPPED;
    }

    public LearningPath getLearningPath() {
        return learningPath;
    }

    public void setLearningPath(LearningPath learningPath) {
        this.learningPath = learningPath;
    }

    public VocabularyWord getVocabularyWord() {
        return vocabularyWord;
    }

    public void setVocabularyWord(VocabularyWord vocabularyWord) {
        this.vocabularyWord = vocabularyWord;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public LearningItemType getItemType() {
        return itemType;
    }

    public void setItemType(LearningItemType itemType) {
        this.itemType = itemType;
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
}
