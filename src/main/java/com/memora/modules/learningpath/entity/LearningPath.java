package com.memora.modules.learningpath.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.learningpath.domain.LearningPathStatus;
import com.memora.modules.user.entity.User;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a learner's adaptive personalized learning path curriculum session.
 */
@Entity
@Table(name = "learning_paths",
        indexes = {
                @Index(name = "idx_lp_user_status", columnList = "user_id, status"),
                @Index(name = "idx_lp_user", columnList = "user_id")
        })
public class LearningPath extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LearningPathStatus status = LearningPathStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_level", nullable = false)
    private DifficultyLevel targetLevel;

    @Column(name = "current_day", nullable = false)
    private int currentDay = 1;

    @Column(name = "total_items", nullable = false)
    private int totalItems = 0;

    @Column(name = "completed_items", nullable = false)
    private int completedItems = 0;

    @OneToMany(mappedBy = "learningPath", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<LearningPathItem> learningPathItems = new ArrayList<>();

    public LearningPath() {
    }

    public LearningPath(User user, DifficultyLevel targetLevel, int currentDay) {
        this.user = user;
        this.targetLevel = targetLevel;
        this.currentDay = currentDay;
        this.status = LearningPathStatus.ACTIVE;
        this.totalItems = 0;
        this.completedItems = 0;
    }

    public void addItem(LearningPathItem item) {
        if (item != null) {
            learningPathItems.add(item);
            item.setLearningPath(this);
            this.totalItems = learningPathItems.size();
        }
    }

    public void incrementCompleted() {
        this.completedItems++;
        if (this.completedItems >= this.totalItems && this.totalItems > 0) {
            this.status = LearningPathStatus.COMPLETED;
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public List<LearningPathItem> getLearningPathItems() {
        return learningPathItems;
    }

    public void setLearningPathItems(List<LearningPathItem> learningPathItems) {
        this.learningPathItems = learningPathItems != null ? learningPathItems : new ArrayList<>();
        this.totalItems = this.learningPathItems.size();
    }
}
