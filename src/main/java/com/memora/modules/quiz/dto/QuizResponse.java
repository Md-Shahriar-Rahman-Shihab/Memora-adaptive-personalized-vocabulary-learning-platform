package com.memora.modules.quiz.dto;

import com.memora.modules.vocabulary.domain.DifficultyLevel;

import java.util.List;

/**
 * Output payload representing a Quiz and its constituent questions for a learner.
 */
public class QuizResponse {

    private Long id;
    private String title;
    private DifficultyLevel difficultyLevel;
    private int questionCount;
    private List<QuestionResponse> questions;

    public QuizResponse() {
    }

    public QuizResponse(Long id, String title, DifficultyLevel difficultyLevel, int questionCount, List<QuestionResponse> questions) {
        this.id = id;
        this.title = title;
        this.difficultyLevel = difficultyLevel;
        this.questionCount = questionCount;
        this.questions = questions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<QuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }
}
