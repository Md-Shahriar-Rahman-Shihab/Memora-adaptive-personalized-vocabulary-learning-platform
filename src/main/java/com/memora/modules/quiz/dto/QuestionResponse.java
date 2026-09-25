package com.memora.modules.quiz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.memora.modules.quiz.domain.QuestionType;

import java.util.List;

/**
 * Output payload presenting a Question to the learner.
 * Crucially omits correct answer values to ensure assessment integrity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionResponse {

    private Long id;
    private QuestionType questionType;
    private String word;
    private String questionText;
    private String sentence;
    private List<String> options;
    private int points;
    private com.memora.modules.vocabulary.domain.DifficultyLevel difficultyLevel;

    public QuestionResponse() {
    }

    public QuestionResponse(Long id, QuestionType questionType, String word, String questionText,
                            String sentence, List<String> options, int points) {
        this(id, questionType, word, questionText, sentence, options, points, null);
    }

    public QuestionResponse(Long id, QuestionType questionType, String word, String questionText,
                            String sentence, List<String> options, int points,
                            com.memora.modules.vocabulary.domain.DifficultyLevel difficultyLevel) {
        this.id = id;
        this.questionType = questionType;
        this.word = word;
        this.questionText = questionText;
        this.sentence = sentence;
        this.options = options;
        this.points = points;
        this.difficultyLevel = difficultyLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public com.memora.modules.vocabulary.domain.DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(com.memora.modules.vocabulary.domain.DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }
}
