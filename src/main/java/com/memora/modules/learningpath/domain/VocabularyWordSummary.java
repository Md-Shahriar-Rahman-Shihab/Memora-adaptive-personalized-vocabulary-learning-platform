package com.memora.modules.learningpath.domain;

import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;

/**
 * Lightweight value object representing a candidate vocabulary word for learning path generation.
 */
public class VocabularyWordSummary {

    private final Long wordId;
    private final String word;
    private final String meaning;
    private final DifficultyLevel difficultyLevel;
    private final WordCategory category;

    public VocabularyWordSummary(Long wordId, String word, String meaning, DifficultyLevel difficultyLevel, WordCategory category) {
        this.wordId = wordId;
        this.word = word;
        this.meaning = meaning;
        this.difficultyLevel = difficultyLevel;
        this.category = category;
    }

    public Long getWordId() {
        return wordId;
    }

    public String getWord() {
        return word;
    }

    public String getMeaning() {
        return meaning;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public WordCategory getCategory() {
        return category;
    }
}
