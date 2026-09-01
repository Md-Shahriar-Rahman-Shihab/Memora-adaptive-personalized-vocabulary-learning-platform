package com.memora.modules.quiz.dto;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * Request payload for deterministically generating a new vocabulary quiz.
 */
public class QuizGenerationRequest {

    private DifficultyLevel difficultyLevel = DifficultyLevel.A1;

    @Min(value = 1, message = "Quiz must contain at least 1 question")
    @Max(value = 50, message = "Quiz cannot contain more than 50 questions")
    private Integer questionCount = 5;

    private List<QuestionType> questionTypes;

    public QuizGenerationRequest() {
    }

    public QuizGenerationRequest(DifficultyLevel difficultyLevel, Integer questionCount, List<QuestionType> questionTypes) {
        this.difficultyLevel = difficultyLevel != null ? difficultyLevel : DifficultyLevel.A1;
        this.questionCount = questionCount != null ? questionCount : 5;
        this.questionTypes = questionTypes;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public List<QuestionType> getQuestionTypes() {
        return questionTypes;
    }

    public void setQuestionTypes(List<QuestionType> questionTypes) {
        this.questionTypes = questionTypes;
    }
}
