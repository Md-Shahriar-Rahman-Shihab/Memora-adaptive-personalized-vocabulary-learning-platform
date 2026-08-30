package com.memora.modules.vocabulary;

import com.memora.common.exception.DuplicateVocabularyWordException;
import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.dto.VocabularyWordRequest;
import com.memora.modules.vocabulary.dto.VocabularyWordResponse;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import com.memora.modules.vocabulary.service.VocabularyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VocabularyServiceTest {

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private UserWordProgressRepository userWordProgressRepository;

    @BeforeEach
    void setUp() {
        userWordProgressRepository.deleteAll();
        vocabularyWordRepository.deleteAll();
    }

    @Test
    @DisplayName("Create word should persist and return VocabularyWordResponse")
    void testCreateWord() {
        VocabularyWordRequest request = new VocabularyWordRequest(
                "lucid", "expressed clearly; easy to understand",
                "Clearly expressed and easy to comprehend.", "/ˈluː.sɪd/",
                "She gave a lucid explanation of the complex theorem.",
                DifficultyLevel.B2, WordCategory.ACADEMIC
        );

        VocabularyWordResponse response = vocabularyService.createWord(request);

        assertNotNull(response.getId());
        assertEquals("lucid", response.getWord());
        assertEquals(DifficultyLevel.B2, response.getDifficultyLevel());
        assertEquals(WordCategory.ACADEMIC, response.getCategory());
    }

    @Test
    @DisplayName("Create duplicate word should throw DuplicateVocabularyWordException")
    void testCreateDuplicateWord() {
        VocabularyWordRequest request1 = new VocabularyWordRequest(
                "lucid", "clear", "clear", null, null, DifficultyLevel.B2, WordCategory.ACADEMIC
        );
        vocabularyService.createWord(request1);

        VocabularyWordRequest request2 = new VocabularyWordRequest(
                "LUCID", "different meaning", null, null, null, DifficultyLevel.B2, WordCategory.ACADEMIC
        );

        assertThrows(DuplicateVocabularyWordException.class, () -> vocabularyService.createWord(request2));
    }

    @Test
    @DisplayName("Find word by text should return the correct entity")
    void testFindWordByText() {
        VocabularyWordRequest request = new VocabularyWordRequest(
                "diligent", "hardworking", "Having or showing care and conscientiousness.",
                null, null, DifficultyLevel.B1, WordCategory.ACADEMIC
        );
        vocabularyService.createWord(request);

        VocabularyWordResponse found = vocabularyService.getWordByText("diligent");
        assertNotNull(found);
        assertEquals("diligent", found.getWord());
        assertEquals(DifficultyLevel.B1, found.getDifficultyLevel());
    }

    @Test
    @DisplayName("Find non-existent word should throw ResourceNotFoundException")
    void testFindNonExistentWord() {
        assertThrows(ResourceNotFoundException.class, () -> vocabularyService.getWordByText("unknownword"));
    }

    @Test
    @DisplayName("Search words should match substring in word or meaning")
    void testSearchWords() {
        vocabularyService.createWord(new VocabularyWordRequest("tenacious", "persistent and determined", null, null, null, DifficultyLevel.B2, WordCategory.GENERAL));
        vocabularyService.createWord(new VocabularyWordRequest("reluctant", "hesitant", null, null, null, DifficultyLevel.B1, WordCategory.GENERAL));

        List<VocabularyWordResponse> searchByWord = vocabularyService.searchVocabulary("tenac");
        assertEquals(1, searchByWord.size());
        assertEquals("tenacious", searchByWord.get(0).getWord());

        List<VocabularyWordResponse> searchByMeaning = vocabularyService.searchVocabulary("determined");
        assertEquals(1, searchByMeaning.size());
        assertEquals("tenacious", searchByMeaning.get(0).getWord());
    }

    @Test
    @DisplayName("Filter words by difficulty level")
    void testFilterByDifficulty() {
        vocabularyService.createWord(new VocabularyWordRequest("cat", "animal", null, null, null, DifficultyLevel.A1, WordCategory.DAILY_LIFE));
        vocabularyService.createWord(new VocabularyWordRequest("dog", "animal", null, null, null, DifficultyLevel.A1, WordCategory.DAILY_LIFE));
        vocabularyService.createWord(new VocabularyWordRequest("profound", "deep", null, null, null, DifficultyLevel.B2, WordCategory.ACADEMIC));

        List<VocabularyWordResponse> a1Words = vocabularyService.getWordsByDifficulty(DifficultyLevel.A1);
        assertEquals(2, a1Words.size());

        List<VocabularyWordResponse> b2Words = vocabularyService.getWordsByDifficulty(DifficultyLevel.B2);
        assertEquals(1, b2Words.size());
    }
}
