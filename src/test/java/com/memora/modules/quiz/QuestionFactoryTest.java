package com.memora.modules.quiz;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.entity.FillInTheBlankQuestion;
import com.memora.modules.quiz.entity.MultipleChoiceQuestion;
import com.memora.modules.quiz.entity.Question;
import com.memora.modules.quiz.entity.TranslationQuestion;
import com.memora.modules.quiz.factory.QuestionFactory;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuestionFactoryTest {

    private QuestionFactory questionFactory;
    private VocabularyWord happyWord;
    private VocabularyWord sadWord;
    private VocabularyWord angryWord;

    @BeforeEach
    void setUp() {
        questionFactory = new QuestionFactory();
        happyWord = new VocabularyWord("happy", "feeling pleasure", "feeling or showing pleasure",
                "/ˈhæpi/", "She was happy to see her friend.", DifficultyLevel.A1, WordCategory.GENERAL);
        sadWord = new VocabularyWord("sad", "feeling sorrow", "feeling unhappy",
                "/sæd/", "He felt sad after the loss.", DifficultyLevel.A1, WordCategory.GENERAL);
        angryWord = new VocabularyWord("angry", "feeling mad", "feeling or showing anger",
                "/ˈæŋɡri/", "The angry customer complained.", DifficultyLevel.A1, WordCategory.GENERAL);
    }

    @Test
    @DisplayName("QuestionFactory should create a MultipleChoiceQuestion with valid options")
    void createMultipleChoiceQuestion() {
        Question question = questionFactory.createQuestion(QuestionType.MULTIPLE_CHOICE, happyWord, List.of(sadWord, angryWord));

        assertNotNull(question);
        assertInstanceOf(MultipleChoiceQuestion.class, question);
        MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) question;

        assertEquals(QuestionType.MULTIPLE_CHOICE, mcq.getQuestionType());
        assertEquals("feeling pleasure", mcq.getCorrectOption());
        assertTrue(mcq.getOptions().contains("feeling pleasure"));
        assertTrue(mcq.getOptions().size() >= 2);
        assertTrue(mcq.getQuestionText().contains("happy"));
    }

    @Test
    @DisplayName("QuestionFactory should create a TranslationQuestion with expected meaning")
    void createTranslationQuestion() {
        Question question = questionFactory.createQuestion(QuestionType.TRANSLATION, happyWord);

        assertNotNull(question);
        assertInstanceOf(TranslationQuestion.class, question);
        TranslationQuestion tq = (TranslationQuestion) question;

        assertEquals(QuestionType.TRANSLATION, tq.getQuestionType());
        assertEquals("feeling pleasure", tq.getExpectedAnswer());
        assertTrue(tq.getQuestionText().contains("happy"));
    }

    @Test
    @DisplayName("QuestionFactory should create a FillInTheBlankQuestion with blanked sentence")
    void createFillInTheBlankQuestion() {
        Question question = questionFactory.createQuestion(QuestionType.FILL_IN_THE_BLANK, happyWord);

        assertNotNull(question);
        assertInstanceOf(FillInTheBlankQuestion.class, question);
        FillInTheBlankQuestion fib = (FillInTheBlankQuestion) question;

        assertEquals(QuestionType.FILL_IN_THE_BLANK, fib.getQuestionType());
        assertEquals("happy", fib.getExpectedAnswer());
        assertTrue(fib.getSentence().contains("___"));
        assertFalse(fib.getSentence().toLowerCase().contains("happy"));
    }

    @Test
    @DisplayName("QuestionFactory should throw IllegalArgumentException when inputs are null")
    void handlesNullInputs() {
        assertThrows(IllegalArgumentException.class, () -> questionFactory.createQuestion(null, happyWord));
        assertThrows(IllegalArgumentException.class, () -> questionFactory.createQuestion(QuestionType.MULTIPLE_CHOICE, null));
    }
}
