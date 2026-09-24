package com.memora.modules.assessment.service;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.assessment.entity.Assessment;
import com.memora.modules.assessment.entity.AssessmentQuestion;
import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.entity.Question;
import com.memora.modules.quiz.factory.QuestionFactory;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates a balanced, multi-tier diagnostic question set distributed across CEFR levels A1-C1.
 * Utilizes existing curated vocabulary and delegates polymorphic question instantiation to {@link QuestionFactory}.
 */
@Component
public class AssessmentQuestionGenerator {

    public static final int QUESTIONS_PER_LEVEL = 2;

    private static final List<DifficultyLevel> ASSESSMENT_LEVELS = List.of(
            DifficultyLevel.A1,
            DifficultyLevel.A2,
            DifficultyLevel.B1,
            DifficultyLevel.B2,
            DifficultyLevel.C1
    );

    private static final List<QuestionType> QUESTION_TYPE_CYCLE = List.of(
            QuestionType.MULTIPLE_CHOICE,
            QuestionType.TRANSLATION,
            QuestionType.FILL_IN_THE_BLANK,
            QuestionType.MULTIPLE_CHOICE
    );

    private final VocabularyWordRepository vocabularyWordRepository;
    private final QuestionFactory questionFactory;

    public AssessmentQuestionGenerator(VocabularyWordRepository vocabularyWordRepository,
                                       QuestionFactory questionFactory) {
        this.vocabularyWordRepository = vocabularyWordRepository;
        this.questionFactory = questionFactory;
    }

    /**
     * Generates a 10-question diagnostic assessment evenly distributed across CEFR levels.
     *
     * @param assessment Owning Assessment entity
     * @return List of newly generated {@link AssessmentQuestion} entities
     */
    public List<AssessmentQuestion> generateAssessmentQuestions(Assessment assessment) {
        List<AssessmentQuestion> generatedQuestions = new ArrayList<>();
        int orderIndex = 1;

        for (DifficultyLevel level : ASSESSMENT_LEVELS) {
            List<VocabularyWord> levelWords = vocabularyWordRepository.findByDifficultyLevel(level);
            if (levelWords.isEmpty()) {
                levelWords = vocabularyWordRepository.findAll();
            }
            if (levelWords.isEmpty()) {
                throw new ResourceNotFoundException("Insufficient vocabulary data available to generate diagnostic assessment for level: " + level);
            }

            List<VocabularyWord> candidates = new ArrayList<>(levelWords);
            Collections.shuffle(candidates);

            int count = Math.min(QUESTIONS_PER_LEVEL, candidates.size());
            for (int i = 0; i < count; i++) {
                VocabularyWord word = candidates.get(i);
                QuestionType type = QUESTION_TYPE_CYCLE.get((orderIndex - 1) % QUESTION_TYPE_CYCLE.size());
                Question question = questionFactory.createQuestion(type, word, candidates);

                AssessmentQuestion assessmentQuestion = new AssessmentQuestion(
                        assessment,
                        question,
                        level,
                        orderIndex++
                );
                generatedQuestions.add(assessmentQuestion);
            }
        }

        return generatedQuestions;
    }
}
