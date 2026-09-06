package com.memora.modules.learningpath.strategy;

import com.memora.modules.learningpath.domain.*;
import com.memora.modules.memory.dto.MemoryWordResponse;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

/**
 * Adaptive implementation of {@link LearningPathStrategy}.
 * Dynamically balances review reinforcement, new vocabulary acquisition, and consolidating quizzes
 * based on learner forgetting risk, mastery scores, response latency, and CEFR proficiency.
 */
@Component
public class AdaptiveLearningPathStrategy implements LearningPathStrategy {

    public static final int MAX_DAILY_ITEMS = 10;
    public static final int MAX_REVIEW_ITEMS = 6;

    @Override
    public LearningPathStrategyType getStrategyType() {
        return LearningPathStrategyType.ADAPTIVE;
    }

    @Override
    public LearningPathResult generatePath(LearningPathContext context) {
        if (context == null) {
            return new LearningPathResult(null, Collections.emptyList(), "Empty context provided");
        }

        List<LearningPathItemCandidate> candidates = new ArrayList<>();
        Set<Long> scheduledWordIds = new HashSet<>();
        Instant now = Instant.now();

        // 1. Gather and prioritize review candidates
        List<MemoryWordResponse> allReviewCandidates = mergeAndPrioritizeReviews(
                context.getDueReviews(),
                context.getWeakWords()
        );

        // Determine adaptive review budget based on learner's current memory state
        int targetReviewCount;
        if (allReviewCandidates.size() >= 5) {
            targetReviewCount = Math.min(MAX_REVIEW_ITEMS, allReviewCandidates.size());
        } else if (!allReviewCandidates.isEmpty()) {
            targetReviewCount = allReviewCandidates.size();
        } else {
            targetReviewCount = 0;
        }

        // Add review items
        int reviewsAdded = 0;
        for (MemoryWordResponse review : allReviewCandidates) {
            if (reviewsAdded >= targetReviewCount) break;
            if (scheduledWordIds.contains(review.getWordId())) continue;

            LearningItemPriority priority = determineReviewPriority(review, now);
            String rationale = String.format("Review due (%s risk, %.1f%% mastery)",
                    review.getForgettingRisk(), review.getMasteryScore());

            candidates.add(LearningPathItemCandidate.ofReview(
                    review.getWordId(),
                    review.getWord(),
                    priority,
                    rationale
            ));
            scheduledWordIds.add(review.getWordId());
            reviewsAdded++;
        }

        // 2. Determine new word budget (reserve 1 slot for quiz)
        int maxNewWords = Math.max(0, MAX_DAILY_ITEMS - reviewsAdded - 1);
        int newWordsAdded = 0;

        // Check if learner qualifies for an adaptive CEFR stretch word (accuracy >= 90% and 0 weak words)
        boolean qualifiesForStretch = (context.getRecentAccuracy() != null && context.getRecentAccuracy() >= 90.0)
                && (context.getWeakWords() == null || context.getWeakWords().isEmpty())
                && (context.getAvailableStretchWords() != null && !context.getAvailableStretchWords().isEmpty());

        if (qualifiesForStretch && maxNewWords > 0) {
            for (VocabularyWordSummary stretchWord : context.getAvailableStretchWords()) {
                if (!context.getMasteredWordIds().contains(stretchWord.getWordId())
                        && !scheduledWordIds.contains(stretchWord.getWordId())) {
                    candidates.add(LearningPathItemCandidate.ofNewWord(
                            stretchWord.getWordId(),
                            stretchWord.getWord(),
                            LearningItemPriority.HIGH,
                            String.format("Adaptive stretch word for high mastery (CEFR %s)", stretchWord.getDifficultyLevel())
                    ));
                    scheduledWordIds.add(stretchWord.getWordId());
                    newWordsAdded++;
                    break; // Add at most 1 stretch word per day
                }
            }
        }

        // Add regular new vocabulary for learner's current target CEFR level
        if (context.getAvailableNewWords() != null) {
            for (VocabularyWordSummary newWord : context.getAvailableNewWords()) {
                if (newWordsAdded >= maxNewWords) break;
                if (context.getMasteredWordIds().contains(newWord.getWordId())) continue;
                if (scheduledWordIds.contains(newWord.getWordId())) continue;

                candidates.add(LearningPathItemCandidate.ofNewWord(
                        newWord.getWordId(),
                        newWord.getWord(),
                        LearningItemPriority.MEDIUM,
                        String.format("New vocabulary discovery (CEFR %s)", newWord.getDifficultyLevel())
                ));
                scheduledWordIds.add(newWord.getWordId());
                newWordsAdded++;
            }

            // If not enough brand new unlearned words exist, fill remaining budget from available words
            if (newWordsAdded < maxNewWords) {
                for (VocabularyWordSummary word : context.getAvailableNewWords()) {
                    if (newWordsAdded >= maxNewWords) break;
                    if (scheduledWordIds.contains(word.getWordId())) continue;

                    candidates.add(LearningPathItemCandidate.ofNewWord(
                            word.getWordId(),
                            word.getWord(),
                            LearningItemPriority.LOW,
                            String.format("Reinforcing vocabulary practice (CEFR %s)", word.getDifficultyLevel())
                    ));
                    scheduledWordIds.add(word.getWordId());
                    newWordsAdded++;
                }
            }
        }

        // 3. Append Consolidating Daily Quiz (if any learning items were planned)
        if (!candidates.isEmpty() || !allReviewCandidates.isEmpty()) {
            candidates.add(LearningPathItemCandidate.ofQuiz(
                    "Daily comprehensive retention & diagnostic quiz"
            ));
        }

        String rationaleSummary = String.format(
                "Adaptive curriculum: %d reviews (%d high risk), %d new words, 1 quiz for target level %s (Day %d)",
                reviewsAdded,
                allReviewCandidates.stream().filter(r -> r.getForgettingRisk() == ForgettingRisk.HIGH).count(),
                newWordsAdded,
                context.getTargetLevel(),
                context.getCurrentDay()
        );

        return new LearningPathResult(context.getTargetLevel(), candidates, rationaleSummary);
    }

    private List<MemoryWordResponse> mergeAndPrioritizeReviews(List<MemoryWordResponse> dueReviews,
                                                               List<MemoryWordResponse> weakWords) {
        Map<Long, MemoryWordResponse> map = new LinkedHashMap<>();
        Instant now = Instant.now();

        // Add due reviews first
        if (dueReviews != null) {
            for (MemoryWordResponse r : dueReviews) {
                map.put(r.getWordId(), r);
            }
        }

        // Add weak words next
        if (weakWords != null) {
            for (MemoryWordResponse w : weakWords) {
                map.putIfAbsent(w.getWordId(), w);
            }
        }

        List<MemoryWordResponse> list = new ArrayList<>(map.values());

        // Sort: 1) Overdue first, 2) Forgetting risk (HIGH > MEDIUM > LOW), 3) Mastery score ascending
        list.sort((a, b) -> {
            boolean aOverdue = a.getNextReviewAt() != null && a.getNextReviewAt().isBefore(now);
            boolean bOverdue = b.getNextReviewAt() != null && b.getNextReviewAt().isBefore(now);
            if (aOverdue != bOverdue) return aOverdue ? -1 : 1;

            int riskComparison = compareRisk(a.getForgettingRisk(), b.getForgettingRisk());
            if (riskComparison != 0) return riskComparison;

            return Double.compare(a.getMasteryScore(), b.getMasteryScore());
        });

        return list;
    }

    private int compareRisk(ForgettingRisk a, ForgettingRisk b) {
        int aScore = a == ForgettingRisk.HIGH ? 3 : (a == ForgettingRisk.MEDIUM ? 2 : 1);
        int bScore = b == ForgettingRisk.HIGH ? 3 : (b == ForgettingRisk.MEDIUM ? 2 : 1);
        return Integer.compare(bScore, aScore); // Descending (HIGH first)
    }

    private LearningItemPriority determineReviewPriority(MemoryWordResponse review, Instant now) {
        if (review.getForgettingRisk() == ForgettingRisk.HIGH) {
            return LearningItemPriority.HIGH;
        }
        if (review.getNextReviewAt() != null && review.getNextReviewAt().isBefore(now)) {
            return LearningItemPriority.HIGH;
        }
        if (review.getMasteryScore() < 60.0 || review.getForgettingRisk() == ForgettingRisk.MEDIUM) {
            return LearningItemPriority.MEDIUM;
        }
        return LearningItemPriority.LOW;
    }
}
