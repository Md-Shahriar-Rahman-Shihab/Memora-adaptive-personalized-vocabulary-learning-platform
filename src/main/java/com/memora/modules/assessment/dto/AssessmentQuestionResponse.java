package com.memora.modules.assessment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.vocabulary.domain.DifficultyLevel;

import java.util.List;

/**
 * Output payload presenting an assessment question to the learner.
 * Crucially excludes any correct answers or answer indicators.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssessmentQuestionResponse {

    private Long questionId;
    private QuestionType questionType;
    private String questionText;
    private String sentence;
    private List<String> options;
    private DifficultyLevel difficultyLevel;

    public AssessmentQuestionResponse() {
    }

    public AssessmentQuestionResponse(Long questionId, QuestionType questionType, String questionText,
                                      String sentence, List<String> options, DifficultyLevel difficultyLevel) {
        this.questionId = questionId;
        this.questionType = questionType;
        this.questionText = questionText;
        this.sentence = sentence;
        this.options = options;
        this.difficultyLevel = difficultyLevel;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
}
