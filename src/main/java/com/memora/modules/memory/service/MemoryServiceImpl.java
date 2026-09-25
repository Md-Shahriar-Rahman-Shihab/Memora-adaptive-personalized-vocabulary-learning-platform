package com.memora.modules.memory.service;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import com.memora.modules.gamification.dto.GamificationActivityResultResponse;
import com.memora.modules.gamification.service.GamificationService;
import com.memora.modules.memory.domain.MemoryCalculationResult;
import com.memora.modules.memory.domain.MemoryInput;
import com.memora.modules.memory.dto.MemoryWordResponse;
import com.memora.modules.memory.dto.WordReviewRequest;
import com.memora.modules.memory.dto.WordReviewResponse;
import com.memora.modules.memory.mapper.MemoryMapper;
import com.memora.modules.memory.strategy.MemoryAlgorithmStrategy;
import com.memora.modules.memory.strategy.MemoryStrategyFactory;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Core implementation of {@link MemoryService}.
 *
 * Coordinates authentication identity, domain entity updates, incremental response time statistics,
 * strategy resolution via {@link MemoryStrategyFactory}, and decoupled retention calculations.
 */
@Service
public class MemoryServiceImpl implements MemoryService {

    private final UserRepository userRepository;
    private final VocabularyWordRepository vocabularyWordRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final MemoryStrategyFactory memoryStrategyFactory;
    private final MemoryMapper memoryMapper;
    private final GamificationService gamificationService;

    public MemoryServiceImpl(UserRepository userRepository,
                             VocabularyWordRepository vocabularyWordRepository,
                             UserWordProgressRepository userWordProgressRepository,
                             MemoryStrategyFactory memoryStrategyFactory,
                             MemoryMapper memoryMapper) {
        this(userRepository, vocabularyWordRepository, userWordProgressRepository, memoryStrategyFactory, memoryMapper, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public MemoryServiceImpl(UserRepository userRepository,
                             VocabularyWordRepository vocabularyWordRepository,
                             UserWordProgressRepository userWordProgressRepository,
                             MemoryStrategyFactory memoryStrategyFactory,
                             MemoryMapper memoryMapper,
                             @Nullable GamificationService gamificationService) {
        this.userRepository = userRepository;
        this.vocabularyWordRepository = vocabularyWordRepository;
        this.userWordProgressRepository = userWordProgressRepository;
        this.memoryStrategyFactory = memoryStrategyFactory;
        this.memoryMapper = memoryMapper;
        this.gamificationService = gamificationService;
    }

    @Override
    @Transactional
    public WordReviewResponse recordReview(String userEmail, WordReviewRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        VocabularyWord vocabularyWord = vocabularyWordRepository.findById(request.getVocabularyWordId())
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary word not found with ID: " + request.getVocabularyWordId()));

        UserWordProgress progress = userWordProgressRepository.findByUserAndVocabularyWord(user, vocabularyWord)
                .orElseGet(() -> new UserWordProgress(user, vocabularyWord));

        // 1. Update attempt statistics
        int prevTotal = progress.getTotalAttempts();
        int newTotal = prevTotal + 1;
        progress.setTotalAttempts(newTotal);

        if (Boolean.TRUE.equals(request.getCorrect())) {
            progress.setCorrectAttempts(progress.getCorrectAttempts() + 1);
            progress.setConsecutiveCorrect(progress.getConsecutiveCorrect() + 1);
            progress.setConsecutiveIncorrect(0);
        } else {
            progress.setIncorrectAttempts(progress.getIncorrectAttempts() + 1);
            progress.setConsecutiveIncorrect(progress.getConsecutiveIncorrect() + 1);
            progress.setConsecutiveCorrect(0);
        }

        // 2. Incremental average response time calculation
        double prevAvg = progress.getAverageResponseTime();
        double newAvg = ((prevAvg * prevTotal) + request.getResponseTimeMs()) / newTotal;
        progress.setAverageResponseTime(Math.round(newAvg * 100.0) / 100.0);

        // 3. Build decoupled MemoryInput
        MemoryInput input = MemoryInput.builder()
                .totalAttempts(progress.getTotalAttempts())
                .correctAttempts(progress.getCorrectAttempts())
                .incorrectAttempts(progress.getIncorrectAttempts())
                .averageResponseTime(progress.getAverageResponseTime())
                .consecutiveCorrect(progress.getConsecutiveCorrect())
                .consecutiveIncorrect(progress.getConsecutiveIncorrect())
                .lastReviewedAt(progress.getLastReviewedAt())
                .currentMasteryScore(progress.getMasteryScore())
                .currentForgettingRisk(progress.getForgettingRisk())
                .currentNextReviewAt(progress.getNextReviewAt())
                .currentLeitnerBox(progress.getLeitnerBox())
                .lastAttemptCorrect(request.getCorrect())
                .lastResponseTimeMs(request.getResponseTimeMs())
                .build();

        // 4. Resolve strategy polymorphically from factory
        MemoryAlgorithmStrategy strategy = memoryStrategyFactory.getStrategy(request.getAlgorithm());

        // 5. Execute algorithm calculation
        MemoryCalculationResult result = strategy.calculate(input);

        // Capture previous metrics for delta progress feedback
        double prevMastery = progress.getMasteryScore();
        ForgettingRisk prevRisk = progress.getForgettingRisk();

        // 6. Update entity state
        progress.setMasteryScore(result.getMasteryScore());
        progress.setForgettingRisk(result.getForgettingRisk());
        progress.setNextReviewAt(result.getNextReviewAt());
        progress.setLeitnerBox(result.getUpdatedLeitnerBox());
        progress.setLastReviewedAt(Instant.now());

        // 7. Persist updated progress
        UserWordProgress savedProgress = userWordProgressRepository.save(progress);

        // 8. Award XP and advance streak if gamification is enabled and requested
        Integer xpEarned = null;
        Integer currentStreak = null;
        Integer totalXp = null;
        if (gamificationService != null && !Boolean.FALSE.equals(request.getAwardXp())) {
            try {
                GamificationActivityResultResponse gamificationResult = gamificationService.recordActivity(
                        user,
                        RewardActivityType.REVIEW,
                        RewardContext.forReview(savedProgress.getId())
                );
                if (gamificationResult != null) {
                    xpEarned = gamificationResult.getXpEarned();
                    currentStreak = gamificationResult.getCurrentStreak();
                    totalXp = gamificationResult.getNewTotalXp();
                }
            } catch (Exception e) {
                // Non-blocking gamification fallback
            }
        }

        // 9. Return mapped response DTO
        return memoryMapper.toReviewResponse(
                savedProgress,
                request.getCorrect(),
                request.getAlgorithm(),
                result,
                xpEarned,
                currentStreak,
                totalXp,
                prevMastery,
                prevRisk
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemoryWordResponse> getDueReviews(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<UserWordProgress> dueList = userWordProgressRepository.findDueForReview(user, Instant.now());
        return memoryMapper.toMemoryWordResponseList(dueList);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemoryWordResponse> getWeakWords(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + userEmail));

        List<UserWordProgress> weakList = userWordProgressRepository.findWeakWords(user);
        return memoryMapper.toMemoryWordResponseList(weakList);
    }
}
