package com.memora.modules.quiz.factory;

import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.entity.FillInTheBlankQuestion;
import com.memora.modules.quiz.entity.MultipleChoiceQuestion;
import com.memora.modules.quiz.entity.Question;
import com.memora.modules.quiz.entity.TranslationQuestion;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Factory pattern encapsulating the instantiation and formulation of polymorphic {@link Question} instances.
 * Prevents callers from dealing directly with concrete subclass constructors.
 */
@Component
public class QuestionFactory {

    private static final int DEFAULT_POINTS = 10;

    /**
     * Creates a concrete Question subtype without distractor pool.
     */
    public Question createQuestion(QuestionType type, VocabularyWord word) {
        return createQuestion(type, word, Collections.emptyList());
    }

    /**
     * Creates a concrete Question subtype with optional distractor pool for MCQ options.
     */
    public Question createQuestion(QuestionType type, VocabularyWord word, List<VocabularyWord> distractorPool) {
        if (type == null) {
            throw new IllegalArgumentException("Question type must not be null");
        }
        if (word == null) {
            throw new IllegalArgumentException("Vocabulary word must not be null");
        }

        switch (type) {
            case MULTIPLE_CHOICE:
                return createMultipleChoiceQuestion(word, distractorPool);
            case TRANSLATION:
                return createTranslationQuestion(word);
            case FILL_IN_THE_BLANK:
                return createFillInTheBlankQuestion(word);
            default:
                throw new IllegalArgumentException("Unsupported question type: " + type);
        }
    }

    private MultipleChoiceQuestion createMultipleChoiceQuestion(VocabularyWord word, List<VocabularyWord> distractorPool) {
        String questionText = String.format("What is the meaning of '%s'?", word.getWord());
        String correctOption = word.getMeaning();

        List<String> options = new ArrayList<>();
        options.add(correctOption);

        if (distractorPool != null) {
            for (VocabularyWord distractor : distractorPool) {
                if (options.size() >= 4) break;
                if (!distractor.getWord().equalsIgnoreCase(word.getWord()) &&
                        !options.contains(distractor.getMeaning())) {
                    options.add(distractor.getMeaning());
                }
            }
        }

        // Fallback distractors if pool did not supply enough
        if (options.size() < 2) {
            options.add("To interact or express without clarity");
        }
        if (options.size() < 3) {
            options.add("A state of intense rapid movement");
        }
        if (options.size() < 4) {
            options.add("An opposite or unrelated concept");
        }

        Collections.shuffle(options);

        return new MultipleChoiceQuestion(word, questionText, DEFAULT_POINTS, options, correctOption);
    }

    private TranslationQuestion createTranslationQuestion(VocabularyWord word) {
        String questionText = String.format("Translate '%s' into its defined meaning.", word.getWord());
        return new TranslationQuestion(word, questionText, DEFAULT_POINTS, word.getMeaning());
    }

    private FillInTheBlankQuestion createFillInTheBlankQuestion(VocabularyWord word) {
        String sentence;
        if (word.getExampleSentence() != null && !word.getExampleSentence().trim().isEmpty() &&
                word.getExampleSentence().toLowerCase().contains(word.getWord().toLowerCase())) {
            sentence = word.getExampleSentence().replaceAll("(?i)\\b" + Pattern.quote(word.getWord()) + "\\b", "___");
        } else {
            sentence = String.format("The definition of ___ is: %s", word.getMeaning());
        }

        String questionText = "Fill in the blank with the appropriate vocabulary word.";
        return new FillInTheBlankQuestion(word, questionText, DEFAULT_POINTS, sentence, word.getWord());
    }
}
