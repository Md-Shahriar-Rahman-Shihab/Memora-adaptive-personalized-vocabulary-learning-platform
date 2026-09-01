package com.memora.modules.quiz.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a generated or curated Quiz.
 */
@Entity
@Table(name = "quizzes",
        indexes = {
                @Index(name = "idx_quiz_difficulty", columnList = "difficulty_level")
        })
public class Quiz extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

    public Quiz() {
    }

    public Quiz(String title, DifficultyLevel difficultyLevel, int questionCount) {
        this.title = title;
        this.difficultyLevel = difficultyLevel;
        this.questionCount = questionCount;
    }

    public void addQuestion(Question question) {
        if (question != null) {
            questions.add(question);
            question.setQuiz(this);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions != null ? questions : new ArrayList<>();
    }
}
