package com.memora.modules.assessment;

import com.memora.modules.assessment.entity.Assessment;
import com.memora.modules.assessment.entity.AssessmentQuestion;
import com.memora.modules.assessment.service.AssessmentQuestionGenerator;
import com.memora.modules.quiz.factory.QuestionFactory;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentQuestionGeneratorTest {

    @Mock
    private VocabularyWordRepository vocabularyWordRepository;

    private QuestionFactory questionFactory;
    private AssessmentQuestionGenerator generator;
    private User testUser;
    private Assessment assessment;

    @BeforeEach
    void setUp() {
        questionFactory = new QuestionFactory();
        generator = new AssessmentQuestionGenerator(vocabularyWordRepository, questionFactory);
        testUser = new User("Jane", "jane@memora.com", "hash", VocabularyLevel.A1, Role.LEARNER);
        assessment = new Assessment(testUser, 10);
    }

    @Test
    @DisplayName("Generator should produce 10 questions distributed across A1, A2, B1, B2, and C1")
    void testGenerateAssessmentQuestionsDistribution() {
        // Mock words for each level
        for (DifficultyLevel level : List.of(DifficultyLevel.A1, DifficultyLevel.A2, DifficultyLevel.B1, DifficultyLevel.B2, DifficultyLevel.C1)) {
            List<VocabularyWord> words = List.of(
                    new VocabularyWord(level.name() + "_word1", "meaning1", "def1", null, "Sentence with " + level.name() + "_word1", level, WordCategory.GENERAL),
                    new VocabularyWord(level.name() + "_word2", "meaning2", "def2", null, "Sentence with " + level.name() + "_word2", level, WordCategory.GENERAL)
            );
            when(vocabularyWordRepository.findByDifficultyLevel(level)).thenReturn(words);
        }

        List<AssessmentQuestion> questions = generator.generateAssessmentQuestions(assessment);

        assertNotNull(questions);
        assertEquals(10, questions.size());

        // Verify distribution: 2 per level
        Map<DifficultyLevel, Long> countsByLevel = questions.stream()
                .collect(Collectors.groupingBy(AssessmentQuestion::getDifficultyLevel, Collectors.counting()));

        assertEquals(2L, countsByLevel.get(DifficultyLevel.A1));
        assertEquals(2L, countsByLevel.get(DifficultyLevel.A2));
        assertEquals(2L, countsByLevel.get(DifficultyLevel.B1));
        assertEquals(2L, countsByLevel.get(DifficultyLevel.B2));
        assertEquals(2L, countsByLevel.get(DifficultyLevel.C1));

        // Verify sequential ordering
        for (int i = 0; i < questions.size(); i++) {
            assertEquals(i + 1, questions.get(i).getOrderIndex());
            assertNotNull(questions.get(i).getQuestion());
        }
    }
}
