package com.memora.modules.learningpath.domain;

/**
 * Value object produced by learning path strategies representing a planned curriculum item.
 */
public class LearningPathItemCandidate {

    private final Long wordId;
    private final String word;
    private final LearningItemType itemType;
    private final LearningItemPriority priority;
    private final String rationale;

    public LearningPathItemCandidate(Long wordId, String word, LearningItemType itemType,
                                    LearningItemPriority priority, String rationale) {
        this.wordId = wordId;
        this.word = word;
        this.itemType = itemType;
        this.priority = priority;
        this.rationale = rationale;
    }

    public static LearningPathItemCandidate ofNewWord(Long wordId, String word, LearningItemPriority priority, String rationale) {
        return new LearningPathItemCandidate(wordId, word, LearningItemType.NEW_WORD, priority, rationale);
    }

    public static LearningPathItemCandidate ofReview(Long wordId, String word, LearningItemPriority priority, String rationale) {
        return new LearningPathItemCandidate(wordId, word, LearningItemType.REVIEW, priority, rationale);
    }

    public static LearningPathItemCandidate ofQuiz(String rationale) {
        return new LearningPathItemCandidate(null, null, LearningItemType.QUIZ, LearningItemPriority.HIGH, rationale);
    }

    public Long getWordId() {
        return wordId;
    }

    public String getWord() {
        return word;
    }

    public LearningItemType getItemType() {
        return itemType;
    }

    public LearningItemPriority getPriority() {
        return priority;
    }

    public String getRationale() {
        return rationale;
    }
}
