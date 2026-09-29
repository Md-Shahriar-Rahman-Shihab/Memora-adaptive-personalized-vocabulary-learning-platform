package com.memora.modules.ai;

import com.memora.modules.ai.dto.*;
import com.memora.modules.ai.service.FallbackExplanationService;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FallbackExplanationServiceTest {

    @Mock
    private VocabularyWordRepository wordRepository;

    @Mock
    private UserWordProgressRepository progressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FallbackExplanationService fallbackService;

    private VocabularyWord sampleWord;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleWord = new VocabularyWord();
        sampleWord.setId(1L);
        sampleWord.setWord("meticulous");
        sampleWord.setMeaning("showing great attention to detail");
        sampleWord.setDefinition("very careful and precise");
        sampleWord.setCategory(WordCategory.ACADEMIC);
        sampleWord.setDifficultyLevel(DifficultyLevel.B2);
        sampleWord.setExampleSentence("The researcher was meticulous in documenting each observation.");

        sampleUser = new User();
        sampleUser.setId(10L);
        sampleUser.setEmail("learner@example.com");
        sampleUser.setCurrentLevel(VocabularyLevel.B2);
    }

    @Test
    @DisplayName("Should generate CEFR B2 explanation with nuance")
    void testExplainWordB2() {
        when(wordRepository.findByWordIgnoreCase("meticulous")).thenReturn(Optional.of(sampleWord));
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(sampleUser));

        AiExplanationRequest request = new AiExplanationRequest(null, "meticulous", "B2");
        AiExplanationResponse response = fallbackService.explainWord("learner@example.com", request);

        assertNotNull(response);
        assertEquals("meticulous", response.word());
        assertEquals("B2", response.cefrLevel());
        assertTrue(response.isFallback());
        assertEquals("fallback", response.provider());
        assertTrue(response.explanation().contains("meticulous"));
    }

    @Test
    @DisplayName("Should adapt explanation to simple terms for A1")
    void testExplainWordA1() {
        when(wordRepository.findByWordIgnoreCase("meticulous")).thenReturn(Optional.of(sampleWord));
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(sampleUser));

        AiExplanationRequest request = new AiExplanationRequest(null, "meticulous", "A1");
        AiExplanationResponse response = fallbackService.explainWord("learner@example.com", request);

        assertNotNull(response);
        assertEquals("A1", response.cefrLevel());
        assertTrue(response.explanation().contains("everyday word") || response.breakdown().contains("Simple definition"));
    }

    @Test
    @DisplayName("Should return existing example sentence from word entity")
    void testGenerateExample() {
        when(wordRepository.findByWordIgnoreCase("meticulous")).thenReturn(Optional.of(sampleWord));
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(sampleUser));

        AiExampleRequest request = new AiExampleRequest(null, "meticulous", "B2");
        AiExampleResponse response = fallbackService.generateExample("learner@example.com", request);

        assertNotNull(response);
        assertEquals("The researcher was meticulous in documenting each observation.", response.exampleSentence());
        assertTrue(response.isFallback());
    }

    @Test
    @DisplayName("Should generate memory tip with association hook")
    void testGenerateMemoryTip() {
        when(wordRepository.findByWordIgnoreCase("meticulous")).thenReturn(Optional.of(sampleWord));
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(sampleUser));

        AiMemoryTipRequest request = new AiMemoryTipRequest(null, "meticulous", "B2");
        AiMemoryTipResponse response = fallbackService.generateMemoryTip("learner@example.com", request);

        assertNotNull(response);
        assertTrue(response.memoryTip().contains("meticulous"));
        assertNotNull(response.association());
        assertTrue(response.isFallback());
    }

    @Test
    @DisplayName("Should provide practical usage collocations and register")
    void testExplainUsage() {
        when(wordRepository.findByWordIgnoreCase("meticulous")).thenReturn(Optional.of(sampleWord));
        when(userRepository.findByEmail("learner@example.com")).thenReturn(Optional.of(sampleUser));

        AiUsageRequest request = new AiUsageRequest(null, "meticulous", "B2");
        AiUsageResponse response = fallbackService.explainUsage("learner@example.com", request);

        assertNotNull(response);
        assertNotNull(response.collocations());
        assertFalse(response.collocations().isEmpty());
        assertEquals("Academic", response.register());
    }

    @Test
    @DisplayName("Should return curated sense-accurate relations for friend without including target word")
    void testWordRelationsFriend() {
        AiWordRelationsRequest request = new AiWordRelationsRequest(null, "friend", "A1", "noun", "a person whom one knows and with whom one has a bond of mutual affection");
        AiWordRelationsResponse response = fallbackService.getWordRelations("learner@example.com", request);

        assertNotNull(response);
        assertEquals("friend", response.word());
        assertTrue(response.synonyms().contains("companion"));
        assertTrue(response.synonyms().contains("ally"));
        assertFalse(response.synonyms().contains("friend"), "Target word must not be in synonyms");

        assertTrue(response.antonyms().contains("enemy"));
        assertTrue(response.antonyms().contains("foe"));
        assertFalse(response.antonyms().contains("friend"), "Target word must not be in antonyms");

        assertEquals("friend", response.wordFamily().get("noun"));
        assertEquals("friendly", response.wordFamily().get("adjective"));
    }

    @Test
    @DisplayName("Concrete physical objects like computer must have empty antonyms instead of fabricated opposites")
    void testWordRelationsConcreteObjectComputer() {
        AiWordRelationsRequest request = new AiWordRelationsRequest(null, "computer", "A2", "noun", "an electronic device for storing and processing data");
        AiWordRelationsResponse response = fallbackService.getWordRelations("learner@example.com", request);

        assertNotNull(response);
        assertEquals("computer", response.word());
        assertTrue(response.synonyms().contains("PC") || response.synonyms().contains("processor") || response.synonyms().contains("machine"));
        assertTrue(response.antonyms().isEmpty(), "Concrete noun 'computer' must have empty antonyms");
    }

    @Test
    @DisplayName("Unknown words without verified data should return clean empty collections without hallucinating")
    void testWordRelationsUnknownWordReturnsEmptyLists() {
        AiWordRelationsRequest request = new AiWordRelationsRequest(null, "xylophonic", "C2", "adjective", "pertaining to a xylophone");
        AiWordRelationsResponse response = fallbackService.getWordRelations("learner@example.com", request);

        assertNotNull(response);
        assertTrue(response.synonyms().isEmpty(), "Should not invent synonyms for unknown word");
        assertTrue(response.antonyms().isEmpty(), "Should not invent antonyms for unknown word");
        assertEquals("xylophonic", response.wordFamily().get("adjective"));
    }
}

