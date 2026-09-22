package com.memora.modules.quiz.entity;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete Question entity representing a Multiple Choice Question (MCQ).
 * Encapsulates choice options and the correct option value.
 */
@Entity
@Table(name = "quiz_multiple_choice_questions")
public class MultipleChoiceQuestion extends Question {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "quiz_mcq_options", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "option_text", nullable = false)
    @OrderColumn(name = "option_order")
    @org.hibernate.annotations.BatchSize(size = 50)
    private List<String> options = new ArrayList<>();

    @Column(name = "correct_option", nullable = false)
    private String correctOption;

    public MultipleChoiceQuestion() {
        super();
        setQuestionType(QuestionType.MULTIPLE_CHOICE);
    }

    public MultipleChoiceQuestion(VocabularyWord vocabularyWord, String questionText, int points,
                                  List<String> options, String correctOption) {
        super(vocabularyWord, QuestionType.MULTIPLE_CHOICE, questionText, points);
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
        this.correctOption = correctOption;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }
}
