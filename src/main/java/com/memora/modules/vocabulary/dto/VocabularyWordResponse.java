package com.memora.modules.vocabulary.dto;

import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;

/**
 * Output presentation model for a vocabulary word.
 */
public class VocabularyWordResponse {

    private Long id;
    private String word;
    private String meaning;
    private String definition;
    private String pronunciation;
    private String exampleSentence;
    private DifficultyLevel difficultyLevel;
    private WordCategory category;

    public VocabularyWordResponse() {
    }

    public VocabularyWordResponse(Long id, String word, String meaning, String definition,
                                  String pronunciation, String exampleSentence,
                                  DifficultyLevel difficultyLevel, WordCategory category) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.definition = definition;
        this.pronunciation = pronunciation;
        this.exampleSentence = exampleSentence;
        this.difficultyLevel = difficultyLevel;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
