package com.memora.modules.vocabulary.dto;

import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload for creating or updating a vocabulary word.
 */
public class VocabularyWordRequest {

    @NotBlank(message = "Word must not be blank")
    @Size(min = 1, max = 100, message = "Word must be between 1 and 100 characters")
    private String word;

    @NotBlank(message = "Meaning must not be blank")
    @Size(max = 255, message = "Meaning must not exceed 255 characters")
    private String meaning;

    private String definition;

    private String pronunciation;

    private String exampleSentence;

    @NotNull(message = "Difficulty level is required")
    private DifficultyLevel difficultyLevel;

    @NotNull(message = "Word category is required")
    private WordCategory category;

    public VocabularyWordRequest() {
    }

    public VocabularyWordRequest(String word, String meaning, String definition, String pronunciation,
                                 String exampleSentence, DifficultyLevel difficultyLevel, WordCategory category) {
        this.word = word;
        this.meaning = meaning;
        this.definition = definition;
        this.pronunciation = pronunciation;
        this.exampleSentence = exampleSentence;
        this.difficultyLevel = difficultyLevel;
        this.category = category;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
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
