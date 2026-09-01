package com.memora.modules.quiz.entity;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Concrete Question entity representing a Translation Question.
 * Encapsulates the expected translation string.
 */
@Entity
@Table(name = "quiz_translation_questions")
public class TranslationQuestion extends Question {

    @Column(name = "expected_answer", nullable = false)
    private String expectedAnswer;

    public TranslationQuestion() {
        super();
        setQuestionType(QuestionType.TRANSLATION);
    }

    public TranslationQuestion(VocabularyWord vocabularyWord, String questionText, int points, String expectedAnswer) {
        super(vocabularyWord, QuestionType.TRANSLATION, questionText, points);
        this.expectedAnswer = expectedAnswer;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }

    public void setExpectedAnswer(String expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }
}
