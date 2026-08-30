package com.memora.modules.vocabulary.service;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.common.exception.UserNotFoundException;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.dto.UserWordProgressResponse;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.mapper.UserWordProgressMapper;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Concrete implementation of {@link UserWordProgressService}.
 */
@Service
public class UserWordProgressServiceImpl implements UserWordProgressService {

    private final UserWordProgressRepository userWordProgressRepository;
    private final UserRepository userRepository;
    private final VocabularyWordRepository vocabularyWordRepository;
    private final UserWordProgressMapper userWordProgressMapper;

    public UserWordProgressServiceImpl(
            UserWordProgressRepository userWordProgressRepository,
            UserRepository userRepository,
            VocabularyWordRepository vocabularyWordRepository,
            UserWordProgressMapper userWordProgressMapper) {
        this.userWordProgressRepository = userWordProgressRepository;
        this.userRepository = userRepository;
        this.vocabularyWordRepository = vocabularyWordRepository;
        this.userWordProgressMapper = userWordProgressMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserWordProgressResponse> getProgressForUser(String email) {
        User user = findUserByEmail(email);
        List<UserWordProgress> progressList = userWordProgressRepository.findByUser(user);
        return userWordProgressMapper.toResponseList(progressList);
    }

    @Override
    @Transactional(readOnly = true)
    public UserWordProgressResponse getProgressForWord(String email, Long wordId) {
        User user = findUserByEmail(email);
        VocabularyWord word = findWordById(wordId);

        UserWordProgress progress = userWordProgressRepository.findByUserAndVocabularyWord(user, word)
                .orElseThrow(() -> new ResourceNotFoundException("UserWordProgress", "vocabularyWordId", wordId));

        return userWordProgressMapper.toResponse(progress);
    }

    @Override
    @Transactional
    public UserWordProgressResponse initializeProgress(String email, Long wordId) {
        User user = findUserByEmail(email);
        VocabularyWord word = findWordById(wordId);

        UserWordProgress progress = userWordProgressRepository.findByUserAndVocabularyWord(user, word)
                .orElseGet(() -> {
                    UserWordProgress newProgress = new UserWordProgress(user, word);
                    return userWordProgressRepository.save(newProgress);
                });

        return userWordProgressMapper.toResponse(progress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserWordProgressResponse> getWeakWords(String email) {
        User user = findUserByEmail(email);
        List<UserWordProgress> weakWords = userWordProgressRepository.findWeakWords(user);
        return userWordProgressMapper.toResponseList(weakWords);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserWordProgressResponse> getDueReviews(String email) {
        User user = findUserByEmail(email);
        List<UserWordProgress> dueWords = userWordProgressRepository.findDueForReview(user, Instant.now());
        return userWordProgressMapper.toResponseList(dueWords);
    }

    private User findUserByEmail(String email) {
        String normalizedEmail = email != null ? email.toLowerCase().trim() : "";
        return userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private VocabularyWord findWordById(Long id) {
        return vocabularyWordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("VocabularyWord", "id", id));
    }
}
