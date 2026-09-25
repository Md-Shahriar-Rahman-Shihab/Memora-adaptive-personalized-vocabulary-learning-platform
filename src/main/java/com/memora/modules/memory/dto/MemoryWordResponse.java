package com.memora.modules.memory.dto;

import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import com.memora.modules.vocabulary.domain.WordCategory;

import java.time.Instant;

/**
 * Output representation of a vocabulary word along with its adaptive memory state and review schedule.
 */
public class MemoryWordResponse {

    private Long wordId;
    private String word;
    private String meaning;
    private DifficultyLevel difficultyLevel;
    private WordCategory category;
    private double masteryScore;
    private ForgettingRisk forgettingRisk;
    private Instant nextReviewAt;
    private Instant lastReviewedAt;
    private int totalAttempts;
    private int correctAttempts;
    private int incorrectAttempts;
    private int consecutiveCorrect;
    private double averageResponseTime;
    private int leitnerBox;
    private String definition;
    private String pronunciation;
    private String exampleSentence;

    public MemoryWordResponse() {
    }

    public MemoryWordResponse(Long wordId,
                              String word,
                              String meaning,
                              DifficultyLevel difficultyLevel,
                              WordCategory category,
                              double masteryScore,
                              ForgettingRisk forgettingRisk,
                              Instant nextReviewAt,
                              Instant lastReviewedAt,
                              int totalAttempts,
                              int correctAttempts,
                              int incorrectAttempts,
                              int consecutiveCorrect,
                              double averageResponseTime,
                              int leitnerBox) {
        this(wordId, word, meaning, difficultyLevel, category, masteryScore, forgettingRisk,
                nextReviewAt, lastReviewedAt, totalAttempts, correctAttempts, incorrectAttempts,
                consecutiveCorrect, averageResponseTime, leitnerBox, null, null, null);
    }

    public MemoryWordResponse(Long wordId,
                              String word,
                              String meaning,
                              DifficultyLevel difficultyLevel,
                              WordCategory category,
                              double masteryScore,
                              ForgettingRisk forgettingRisk,
                              Instant nextReviewAt,
                              Instant lastReviewedAt,
                              int totalAttempts,
                              int correctAttempts,
                              int incorrectAttempts,
                              int consecutiveCorrect,
                              double averageResponseTime,
                              int leitnerBox,
                              String definition,
                              String pronunciation,
                              String exampleSentence) {
        this.wordId = wordId;
        this.word = word;
        this.meaning = meaning;
        this.difficultyLevel = difficultyLevel;
        this.category = category;
        this.masteryScore = masteryScore;
        this.forgettingRisk = forgettingRisk;
        this.nextReviewAt = nextReviewAt;
        this.lastReviewedAt = lastReviewedAt;
        this.totalAttempts = totalAttempts;
        this.correctAttempts = correctAttempts;
        this.incorrectAttempts = incorrectAttempts;
        this.consecutiveCorrect = consecutiveCorrect;
        this.averageResponseTime = averageResponseTime;
        this.leitnerBox = leitnerBox;
        this.definition = definition;
        this.pronunciation = pronunciation;
        this.exampleSentence = exampleSentence;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
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

    public double getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(double masteryScore) {
        this.masteryScore = masteryScore;
    }

    public ForgettingRisk getForgettingRisk() {
        return forgettingRisk;
    }

    public void setForgettingRisk(ForgettingRisk forgettingRisk) {
        this.forgettingRisk = forgettingRisk;
    }

    public Instant getNextReviewAt() {
        return nextReviewAt;
    }

    public void setNextReviewAt(Instant nextReviewAt) {
        this.nextReviewAt = nextReviewAt;
    }

    public Instant getLastReviewedAt() {
        return lastReviewedAt;
    }

    public void setLastReviewedAt(Instant lastReviewedAt) {
        this.lastReviewedAt = lastReviewedAt;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getCorrectAttempts() {
        return correctAttempts;
    }

    public void setCorrectAttempts(int correctAttempts) {
        this.correctAttempts = correctAttempts;
    }

    public int getIncorrectAttempts() {
        return incorrectAttempts;
    }

    public void setIncorrectAttempts(int incorrectAttempts) {
        this.incorrectAttempts = incorrectAttempts;
    }

    public int getConsecutiveCorrect() {
        return consecutiveCorrect;
    }

    public void setConsecutiveCorrect(int consecutiveCorrect) {
        this.consecutiveCorrect = consecutiveCorrect;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public int getLeitnerBox() {
        return leitnerBox;
    }

    public void setLeitnerBox(int leitnerBox) {
        this.leitnerBox = leitnerBox;
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
}
