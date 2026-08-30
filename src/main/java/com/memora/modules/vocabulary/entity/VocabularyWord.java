package com.memora.modules.vocabulary.entity;

import com.memora.common.domain.BaseEntity;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import jakarta.persistence.*;

/**
 * Domain entity representing a lexical vocabulary item.
 * Demonstrates Inheritance (extending BaseEntity) and Encapsulation.
 */
@Entity
@Table(name = "vocabulary_words",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vocabulary_word", columnNames = "word")
        },
        indexes = {
                @Index(name = "idx_vocab_word", columnList = "word"),
                @Index(name = "idx_vocab_difficulty", columnList = "difficulty_level"),
                @Index(name = "idx_vocab_category", columnList = "category")
        })
public class VocabularyWord extends BaseEntity {

    @Column(name = "word", nullable = false, unique = true)
    private String word;

    @Column(name = "meaning", nullable = false)
    private String meaning;

    @Column(name = "definition", columnDefinition = "TEXT")
    private String definition;

    @Column(name = "pronunciation")
    private String pronunciation;

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String exampleSentence;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private WordCategory category;

    public VocabularyWord() {
    }

    public VocabularyWord(String word, String meaning, String definition, String pronunciation,
                          String exampleSentence, DifficultyLevel difficultyLevel, WordCategory category) {
        this.word = word != null ? word.toLowerCase().trim() : null;
        this.meaning = meaning;
        this.definition = definition;
        this.pronunciation = pronunciation;
        this.exampleSentence = exampleSentence;
        this.difficultyLevel = difficultyLevel != null ? difficultyLevel : DifficultyLevel.A1;
        this.category = category != null ? category : WordCategory.GENERAL;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word != null ? word.toLowerCase().trim() : null;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public WordCategory getCategory() {
        return category;
    }

    public void setCategory(WordCategory category) {
        this.category = category;
    }
}
