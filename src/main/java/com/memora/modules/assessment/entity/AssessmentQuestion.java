package com.memora.modules.assessment.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.quiz.entity.Question;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import jakarta.persistence.*;

/**
 * Domain entity associating a Question with an Assessment, recording presentation order
 * and target CEFR difficulty level.
 */
@Entity
@Table(name = "assessment_questions",
        indexes = {
                @Index(name = "idx_aq_assessment", columnList = "assessment_id"),
                @Index(name = "idx_aq_question", columnList = "question_id")
        })
public class AssessmentQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    public AssessmentQuestion() {
    }

    public AssessmentQuestion(Assessment assessment, Question question, DifficultyLevel difficultyLevel, int orderIndex) {
        this.assessment = assessment;
        this.question = question;
        this.difficultyLevel = difficultyLevel;
        this.orderIndex = orderIndex;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public void setAssessment(Assessment assessment) {
        this.assessment = assessment;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
}
