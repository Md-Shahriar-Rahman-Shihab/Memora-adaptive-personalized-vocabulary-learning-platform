package com.memora.modules.vocabulary.service;

import com.memora.common.exception.DuplicateVocabularyWordException;
import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.dto.VocabularyWordRequest;
import com.memora.modules.vocabulary.dto.VocabularyWordResponse;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.mapper.VocabularyWordMapper;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete implementation of {@link VocabularyService}.
 */
@Service
public class VocabularyServiceImpl implements VocabularyService {

    private final VocabularyWordRepository vocabularyWordRepository;
    private final VocabularyWordMapper vocabularyWordMapper;

    public VocabularyServiceImpl(VocabularyWordRepository vocabularyWordRepository, VocabularyWordMapper vocabularyWordMapper) {
        this.vocabularyWordRepository = vocabularyWordRepository;
        this.vocabularyWordMapper = vocabularyWordMapper;
    }

    @Override
    @Transactional
    public VocabularyWordResponse createWord(VocabularyWordRequest request) {
        String normalizedWord = request.getWord().toLowerCase().trim();
        if (vocabularyWordRepository.existsByWordIgnoreCase(normalizedWord)) {
            throw new DuplicateVocabularyWordException(request.getWord());
        }

        VocabularyWord entity = vocabularyWordMapper.toEntity(request);
        VocabularyWord savedEntity = vocabularyWordRepository.save(entity);
        return vocabularyWordMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public VocabularyWordResponse getWordById(Long id) {
        VocabularyWord entity = getEntityById(id);
        return vocabularyWordMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public VocabularyWordResponse getWordByText(String word) {
        String normalizedWord = word != null ? word.toLowerCase().trim() : "";
        VocabularyWord entity = vocabularyWordRepository.findByWordIgnoreCase(normalizedWord)
                .orElseThrow(() -> new ResourceNotFoundException("VocabularyWord", "word", word));
        return vocabularyWordMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VocabularyWordResponse> getAllVocabulary() {
        List<VocabularyWord> words = vocabularyWordRepository.findAll();
        return vocabularyWordMapper.toResponseList(words);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VocabularyWordResponse> searchVocabulary(String query) {
        if (query == null || query.isBlank()) {
            return getAllVocabulary();
        }
        List<VocabularyWord> results = vocabularyWordRepository.searchByWordOrMeaning(query.trim());
        return vocabularyWordMapper.toResponseList(results);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VocabularyWordResponse> getWordsByDifficulty(DifficultyLevel difficultyLevel) {
        List<VocabularyWord> words = vocabularyWordRepository.findByDifficultyLevel(difficultyLevel);
        return vocabularyWordMapper.toResponseList(words);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VocabularyWordResponse> getWordsByCategory(WordCategory category) {
        List<VocabularyWord> words = vocabularyWordRepository.findByCategory(category);
        return vocabularyWordMapper.toResponseList(words);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VocabularyWordResponse> getRandomWords(int limit) {
        List<VocabularyWord> allWords = vocabularyWordRepository.findAll();
        if (allWords.isEmpty()) {
            return Collections.emptyList();
        }
        List<VocabularyWord> shuffled = new ArrayList<>(allWords);
        Collections.shuffle(shuffled);
        List<VocabularyWord> selected = shuffled.subList(0, Math.min(limit, shuffled.size()));
        return vocabularyWordMapper.toResponseList(selected);
    }

    @Override
    @Transactional(readOnly = true)
    public VocabularyWord getEntityById(Long id) {
        return vocabularyWordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VocabularyWord", "id", id));
    }
}
