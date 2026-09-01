package com.memora.modules.quiz.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import jakarta.persistence.*;

/**
 * Abstract domain entity representing a vocabulary quiz question.
 * Demonstrates Abstraction and Inheritance (extending BaseEntity).
 * Specific question behavior and state are encapsulated in polymorphic subclasses.
 */
@Entity
@Table(name = "quiz_questions",
        indexes = {
                @Index(name = "idx_question_quiz", columnList = "quiz_id"),
                @Index(name = "idx_question_vocab", columnList = "vocabulary_word_id"),
                @Index(name = "idx_question_type", columnList = "question_type")
        })
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Question extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vocabulary_word_id", nullable = false)
    private VocabularyWord vocabularyWord;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @Column(name = "points", nullable = false)
    private int points = 10;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    protected Question() {
    }

    protected Question(VocabularyWord vocabularyWord, QuestionType questionType, String questionText, int points) {
        this.vocabularyWord = vocabularyWord;
        this.questionType = questionType;
        this.questionText = questionText;
        this.points = points;
    }

    public VocabularyWord getVocabularyWord() {
        return vocabularyWord;
    }

    public void setVocabularyWord(VocabularyWord vocabularyWord) {
        this.vocabularyWord = vocabularyWord;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
}
