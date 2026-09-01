package com.memora.modules.quiz;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.factory.QuestionEvaluatorFactory;
import com.memora.modules.quiz.strategy.FillInTheBlankEvaluator;
import com.memora.modules.quiz.strategy.MultipleChoiceEvaluator;
import com.memora.modules.quiz.strategy.QuestionEvaluatorStrategy;
import com.memora.modules.quiz.strategy.TranslationEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionEvaluatorFactoryTest {

    private QuestionEvaluatorFactory factory;

    @BeforeEach
    void setUp() {
        List<QuestionEvaluatorStrategy> strategies = List.of(
                new MultipleChoiceEvaluator(),
                new TranslationEvaluator(),
                new FillInTheBlankEvaluator()
        );
        factory = new QuestionEvaluatorFactory(strategies);
    }

    @Test
    @DisplayName("Factory should return MultipleChoiceEvaluator for MULTIPLE_CHOICE")
    void returnsMcqEvaluator() {
        QuestionEvaluatorStrategy strategy = factory.getEvaluator(QuestionType.MULTIPLE_CHOICE);
        assertNotNull(strategy);
        assertInstanceOf(MultipleChoiceEvaluator.class, strategy);
    }

    @Test
    @DisplayName("Factory should return TranslationEvaluator for TRANSLATION")
    void returnsTranslationEvaluator() {
        QuestionEvaluatorStrategy strategy = factory.getEvaluator(QuestionType.TRANSLATION);
        assertNotNull(strategy);
        assertInstanceOf(TranslationEvaluator.class, strategy);
    }

    @Test
    @DisplayName("Factory should return FillInTheBlankEvaluator for FILL_IN_THE_BLANK")
    void returnsFillInTheBlankEvaluator() {
        QuestionEvaluatorStrategy strategy = factory.getEvaluator(QuestionType.FILL_IN_THE_BLANK);
        assertNotNull(strategy);
        assertInstanceOf(FillInTheBlankEvaluator.class, strategy);
    }

    @Test
    @DisplayName("Factory should throw IllegalArgumentException for unhandled or null type")
    void throwsForNullType() {
        assertThrows(IllegalArgumentException.class, () -> factory.getEvaluator(null));
    }
}
