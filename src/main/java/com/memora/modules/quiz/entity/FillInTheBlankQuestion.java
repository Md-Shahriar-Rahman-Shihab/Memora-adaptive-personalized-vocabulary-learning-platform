package com.memora.modules.quiz.entity;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Concrete Question entity representing a Fill In The Blank Question.
 * Encapsulates the partial sentence context and the expected missing word.
 */
@Entity
@Table(name = "quiz_fill_in_the_blank_questions")
public class FillInTheBlankQuestion extends Question {

    @Column(name = "sentence", columnDefinition = "TEXT", nullable = false)
    private String sentence;

    @Column(name = "expected_answer", nullable = false)
    private String expectedAnswer;

    public FillInTheBlankQuestion() {
        super();
        setQuestionType(QuestionType.FILL_IN_THE_BLANK);
    }

    public FillInTheBlankQuestion(VocabularyWord vocabularyWord, String questionText, int points,
                                  String sentence, String expectedAnswer) {
        super(vocabularyWord, QuestionType.FILL_IN_THE_BLANK, questionText, points);
        this.sentence = sentence;
        this.expectedAnswer = expectedAnswer;
    }

    public String getSentence() {
        return sentence;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }

    public void setExpectedAnswer(String expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }
}
