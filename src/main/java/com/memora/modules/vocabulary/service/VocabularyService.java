package com.memora.modules.vocabulary.service;

import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.dto.VocabularyWordRequest;
import com.memora.modules.vocabulary.dto.VocabularyWordResponse;
import com.memora.modules.vocabulary.entity.VocabularyWord;

import java.util.List;

/**
 * Service interface defining vocabulary catalog queries and management operations.
 */
public interface VocabularyService {

    VocabularyWordResponse createWord(VocabularyWordRequest request);

    VocabularyWordResponse getWordById(Long id);

    VocabularyWordResponse getWordByText(String word);

    List<VocabularyWordResponse> getAllVocabulary();

    List<VocabularyWordResponse> searchVocabulary(String query);

    List<VocabularyWordResponse> getWordsByDifficulty(DifficultyLevel difficultyLevel);

    List<VocabularyWordResponse> getWordsByCategory(WordCategory category);

    List<VocabularyWordResponse> getRandomWords(int limit);

    VocabularyWord getEntityById(Long id);
}
