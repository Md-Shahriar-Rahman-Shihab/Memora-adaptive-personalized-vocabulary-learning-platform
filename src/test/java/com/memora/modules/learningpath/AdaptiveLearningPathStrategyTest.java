package com.memora.modules.learningpath;

import com.memora.modules.learningpath.domain.*;
import com.memora.modules.learningpath.strategy.AdaptiveLearningPathStrategy;
import com.memora.modules.memory.dto.MemoryWordResponse;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import com.memora.modules.vocabulary.domain.WordCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AdaptiveLearningPathStrategyTest {

    private AdaptiveLearningPathStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new AdaptiveLearningPathStrategy();
    }

    @Test
    @DisplayName("Strategy should prioritize reviews when learner has multiple high-risk and due words")
    void testLearnerWithManyWeakAndDueWords() {
        Instant past = Instant.now().minus(2, ChronoUnit.DAYS);

        List<MemoryWordResponse> dueReviews = List.of(
                new MemoryWordResponse(1L, "meticulous", "careful", DifficultyLevel.B1, WordCategory.GENERAL, 40.0, ForgettingRisk.HIGH, past, past, 3, 1, 2, 0, 3000.0, 1),
                new MemoryWordResponse(2L, "ubiquitous", "everywhere", DifficultyLevel.B1, WordCategory.TECHNOLOGY, 50.0, ForgettingRisk.HIGH, past, past, 4, 2, 2, 0, 2800.0, 1),
                new MemoryWordResponse(3L, "pragmatic", "practical", DifficultyLevel.B1, WordCategory.BUSINESS, 60.0, ForgettingRisk.MEDIUM, past, past, 5, 3, 2, 1, 2200.0, 2)
        );

        List<MemoryWordResponse> weakWords = List.of(
                new MemoryWordResponse(4L, "ephemeral", "brief", DifficultyLevel.B1, WordCategory.SCIENCE, 30.0, ForgettingRisk.HIGH, null, null, 2, 0, 2, 0, 3500.0, 1),
                new MemoryWordResponse(5L, "candid", "frank", DifficultyLevel.B1, WordCategory.GENERAL, 55.0, ForgettingRisk.MEDIUM, null, null, 4, 2, 2, 0, 2400.0, 1)
        );

        List<VocabularyWordSummary> availableNewWords = List.of(
                new VocabularyWordSummary(10L, "lucid", "clear", DifficultyLevel.B1, WordCategory.ACADEMIC),
                new VocabularyWordSummary(11L, "serene", "calm", DifficultyLevel.B1, WordCategory.GENERAL),
                new VocabularyWordSummary(12L, "vibrant", "energetic", DifficultyLevel.B1, WordCategory.DAILY_LIFE),
                new VocabularyWordSummary(13L, "robust", "strong", DifficultyLevel.B1, WordCategory.TECHNOLOGY)
        );

        LearningPathContext context = new LearningPathContext(
                DifficultyLevel.B1,
                dueReviews,
                weakWords,
                Set.of(),
                availableNewWords,
                List.of(),
                50.0,
                1
        );

        LearningPathResult result = strategy.generatePath(context);

        assertNotNull(result);
        assertEquals(DifficultyLevel.B1, result.getTargetLevel());
        assertTrue(result.getTotalItems() <= AdaptiveLearningPathStrategy.MAX_DAILY_ITEMS);

        // Verify reviews were prioritized (5 reviews, new words, 1 quiz)
        long reviewCount = result.getItems().stream()
                .filter(i -> i.getItemType() == LearningItemType.REVIEW)
                .count();
        assertEquals(5, reviewCount);

        // Verify quiz is placed at the end
        assertEquals(LearningItemType.QUIZ, result.getItems().get(result.getTotalItems() - 1).getItemType());
    }

    @Test
    @DisplayName("Strategy should prioritize new vocabulary when learner has no due or weak words")
    void testLearnerWithNoDueWords() {
        List<VocabularyWordSummary> availableNewWords = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            availableNewWords.add(new VocabularyWordSummary(i, "word_" + i, "meaning_" + i, DifficultyLevel.A1, WordCategory.GENERAL));
        }

        LearningPathContext context = new LearningPathContext(
                DifficultyLevel.A1,
                List.of(),
                List.of(),
                Set.of(),
                availableNewWords,
                List.of(),
                null,
                1
        );

        LearningPathResult result = strategy.generatePath(context);

        assertNotNull(result);
        assertEquals(DifficultyLevel.A1, result.getTargetLevel());

        long newWordCount = result.getItems().stream()
                .filter(i -> i.getItemType() == LearningItemType.NEW_WORD)
                .count();
        assertEquals(9, newWordCount); // 9 new words + 1 quiz = 10 items max

        assertEquals(LearningItemType.QUIZ, result.getItems().get(result.getTotalItems() - 1).getItemType());
    }

    @Test
    @DisplayName("Learner with high mastery (>= 90%) should receive an adaptive stretch word from next CEFR tier")
    void testHighMasteryLearnerReceivesStretchWord() {
        List<VocabularyWordSummary> availableNewWords = List.of(
                new VocabularyWordSummary(1L, "a1_word", "meaning", DifficultyLevel.A1, WordCategory.GENERAL)
        );

        List<VocabularyWordSummary> stretchWords = List.of(
                new VocabularyWordSummary(20L, "a2_stretch_word", "stretch meaning", DifficultyLevel.A2, WordCategory.ACADEMIC)
        );

        LearningPathContext context = new LearningPathContext(
                DifficultyLevel.A1,
                List.of(),
                List.of(),
                Set.of(),
                availableNewWords,
                stretchWords,
                95.0, // High accuracy
                2
        );

        LearningPathResult result = strategy.generatePath(context);

        assertNotNull(result);
        boolean containsStretchWord = result.getItems().stream()
                .anyMatch(i -> i.getWordId() != null && i.getWordId().equals(20L) && i.getPriority() == LearningItemPriority.HIGH);

        assertTrue(containsStretchWord, "High mastery learner should receive adaptive stretch word");
    }

    @Test
    @DisplayName("Learning path item count should never exceed 10 items")
    void testItemCountUpperLimit() {
        List<MemoryWordResponse> dueReviews = new ArrayList<>();
        for (long i = 1; i <= 15; i++) {
            dueReviews.add(new MemoryWordResponse(i, "word_" + i, "meaning", DifficultyLevel.B2, WordCategory.GENERAL, 40.0, ForgettingRisk.HIGH, Instant.now().minus(1, ChronoUnit.DAYS), null, 3, 1, 2, 0, 2000.0, 1));
        }

        List<VocabularyWordSummary> newWords = new ArrayList<>();
        for (long i = 100; i <= 120; i++) {
            newWords.add(new VocabularyWordSummary(i, "new_" + i, "meaning", DifficultyLevel.B2, WordCategory.GENERAL));
        }

        LearningPathContext context = new LearningPathContext(
                DifficultyLevel.B2,
                dueReviews,
                List.of(),
                Set.of(),
                newWords,
                List.of(),
                70.0,
                1
        );

        LearningPathResult result = strategy.generatePath(context);

        assertNotNull(result);
        assertTrue(result.getTotalItems() <= 10);
    }
}
