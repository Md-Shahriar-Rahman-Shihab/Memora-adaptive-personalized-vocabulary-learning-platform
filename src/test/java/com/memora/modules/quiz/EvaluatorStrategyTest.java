package com.memora.modules.quiz;

import com.memora.modules.quiz.dto.EvaluationResult;
import com.memora.modules.quiz.entity.FillInTheBlankQuestion;
import com.memora.modules.quiz.entity.MultipleChoiceQuestion;
import com.memora.modules.quiz.entity.TranslationQuestion;
import com.memora.modules.quiz.strategy.FillInTheBlankEvaluator;
import com.memora.modules.quiz.strategy.MultipleChoiceEvaluator;
import com.memora.modules.quiz.strategy.TranslationEvaluator;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluatorStrategyTest {

    private MultipleChoiceEvaluator mcqEvaluator;
    private TranslationEvaluator translationEvaluator;
    private FillInTheBlankEvaluator fibEvaluator;
    private VocabularyWord word;

    @BeforeEach
    void setUp() {
        mcqEvaluator = new MultipleChoiceEvaluator();
        translationEvaluator = new TranslationEvaluator();
        fibEvaluator = new FillInTheBlankEvaluator();

        word = new VocabularyWord("lucid", "expressed clearly", "expressed clearly; easy to understand",
                "/ˈluː.sɪd/", "A lucid explanation helped everyone.", DifficultyLevel.B2, WordCategory.ACADEMIC);
    }

    @Test
    @DisplayName("MultipleChoiceEvaluator should award points for matching answer and zero for incorrect")
    void mcqEvaluation() {
        MultipleChoiceQuestion mcq = new MultipleChoiceQuestion(
                word, "What is the meaning of 'lucid'?", 10,
                List.of("expressed clearly", "dark and mysterious", "confusing"), "expressed clearly"
        );

        EvaluationResult correctRes = mcqEvaluator.evaluate(mcq, "expressed clearly");
        assertTrue(correctRes.isCorrect());
        assertEquals(10, correctRes.getScore());
        assertTrue(correctRes.getFeedback().toLowerCase().contains("correct"));

        EvaluationResult incorrectRes = mcqEvaluator.evaluate(mcq, "dark and mysterious");
        assertFalse(incorrectRes.isCorrect());
        assertEquals(0, incorrectRes.getScore());
        assertTrue(incorrectRes.getFeedback().toLowerCase().contains("incorrect"));

        EvaluationResult emptyRes = mcqEvaluator.evaluate(mcq, "");
        assertFalse(emptyRes.isCorrect());
        assertEquals(0, emptyRes.getScore());
    }

    @Test
    @DisplayName("TranslationEvaluator should support case-insensitive and trimmed evaluation")
    void translationEvaluation() {
        TranslationQuestion tq = new TranslationQuestion(word, "Translate 'lucid'", 10, "expressed clearly");

        EvaluationResult exactRes = translationEvaluator.evaluate(tq, "expressed clearly");
        assertTrue(exactRes.isCorrect());
        assertEquals(10, exactRes.getScore());

        EvaluationResult caseInsensitiveRes = translationEvaluator.evaluate(tq, "  EXPRESSED CLEARLY  ");
        assertTrue(caseInsensitiveRes.isCorrect());
        assertEquals(10, caseInsensitiveRes.getScore());

        EvaluationResult wrongRes = translationEvaluator.evaluate(tq, "opaque");
        assertFalse(wrongRes.isCorrect());
        assertEquals(0, wrongRes.getScore());
    }

    @Test
    @DisplayName("FillInTheBlankEvaluator should evaluate missing word correctly")
    void fibEvaluation() {
        FillInTheBlankQuestion fib = new FillInTheBlankQuestion(
                word, "Fill in the blank", 10, "A ___ explanation helped everyone.", "lucid"
        );

        EvaluationResult correctRes = fibEvaluator.evaluate(fib, "lucid");
        assertTrue(correctRes.isCorrect());
        assertEquals(10, correctRes.getScore());

        EvaluationResult caseInsensitiveRes = fibEvaluator.evaluate(fib, "LUCID ");
        assertTrue(caseInsensitiveRes.isCorrect());
        assertEquals(10, caseInsensitiveRes.getScore());

        EvaluationResult wrongRes = fibEvaluator.evaluate(fib, "cloudy");
        assertFalse(wrongRes.isCorrect());
        assertEquals(0, wrongRes.getScore());
    }
}
