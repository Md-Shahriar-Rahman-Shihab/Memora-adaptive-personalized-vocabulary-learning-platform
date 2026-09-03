package com.memora.modules.learningpath.service;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.assessment.entity.Assessment;
import com.memora.modules.assessment.repository.AssessmentRepository;
import com.memora.modules.learningpath.domain.*;
import com.memora.modules.learningpath.dto.*;
import com.memora.modules.learningpath.entity.LearningPath;
import com.memora.modules.learningpath.entity.LearningPathItem;
import com.memora.modules.learningpath.factory.LearningPathStrategyFactory;
import com.memora.modules.learningpath.repository.LearningPathItemRepository;
import com.memora.modules.learningpath.repository.LearningPathRepository;
import com.memora.modules.learningpath.strategy.AdaptiveLearningPathStrategy;
import com.memora.modules.learningpath.strategy.LearningPathStrategy;
import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.dto.MemoryWordResponse;
import com.memora.modules.memory.dto.WordReviewRequest;
import com.memora.modules.memory.service.MemoryService;
import com.memora.modules.quiz.dto.QuizGenerationRequest;
import com.memora.modules.quiz.dto.QuizResponse;
import com.memora.modules.quiz.entity.Quiz;
import com.memora.modules.quiz.repository.QuizRepository;
import com.memora.modules.quiz.service.QuizService;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core implementation of {@link LearningPathService}.
 * Orchestrates adaptive curriculum generation without embedding placement, memory, or quiz calculations.
 */
@Service
@Transactional(readOnly = true)
public class LearningPathServiceImpl implements LearningPathService {

    private static final Logger log = LoggerFactory.getLogger(LearningPathServiceImpl.class);

    private final LearningPathRepository learningPathRepository;
    private final LearningPathItemRepository learningPathItemRepository;
    private final UserRepository userRepository;
    private final VocabularyWordRepository vocabularyWordRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final AssessmentRepository assessmentRepository;
    private final MemoryService memoryService;
    private final QuizService quizService;
    private final QuizRepository quizRepository;
    private final LearningPathStrategyFactory strategyFactory;

    public LearningPathServiceImpl(LearningPathRepository learningPathRepository,
                                   LearningPathItemRepository learningPathItemRepository,
                                   UserRepository userRepository,
                                   VocabularyWordRepository vocabularyWordRepository,
                                   UserWordProgressRepository userWordProgressRepository,
                                   AssessmentRepository assessmentRepository,
                                   MemoryService memoryService,
                                   QuizService quizService,
                                   QuizRepository quizRepository,
                                   LearningPathStrategyFactory strategyFactory) {
        this.learningPathRepository = learningPathRepository;
        this.learningPathItemRepository = learningPathItemRepository;
        this.userRepository = userRepository;
        this.vocabularyWordRepository = vocabularyWordRepository;
        this.userWordProgressRepository = userWordProgressRepository;
        this.assessmentRepository = assessmentRepository;
        this.memoryService = memoryService;
        this.quizService = quizService;
        this.quizRepository = quizRepository;
        this.strategyFactory = strategyFactory;
    }

    @Override
    @Transactional
    public LearningPathResponse startPath(String userEmail) {
        User user = findUserByEmail(userEmail);

        // 1. Return existing active path if present
        Optional<LearningPath> existingActive = learningPathRepository.findActivePathByUserId(user.getId());
        if (existingActive.isPresent()) {
            return mapToLearningPathResponse(existingActive.get());
        }

        // 2. Determine target CEFR level
        DifficultyLevel targetLevel = resolveTargetLevel(user);

        // 3. Gather memory metrics and vocabulary candidates
        LearningPathContext context = buildContext(user, targetLevel, 1);

        // 4. Resolve strategy & generate candidate curriculum
        LearningPathStrategy strategy = strategyFactory.getDefaultStrategy();
        LearningPathResult result = strategy.generatePath(context);

        // 5. Persist LearningPath & LearningPathItems
        LearningPath learningPath = new LearningPath(user, targetLevel, 1);
        populateLearningPathItems(learningPath, result.getItems(), targetLevel);

        LearningPath savedPath = learningPathRepository.save(learningPath);
        return mapToLearningPathResponse(savedPath);
    }

    @Override
    public LearningPathResponse getCurrentPath(String userEmail) {
        User user = findUserByEmail(userEmail);
        LearningPath path = learningPathRepository.findActivePathByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No active learning path found for user: " + userEmail));
        return mapToLearningPathResponse(path);
    }

    @Override
    public TodayLearningPathResponse getTodayPath(String userEmail) {
        User user = findUserByEmail(userEmail);
        LearningPath path = learningPathRepository.findActivePathByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No active learning path found for user: " + userEmail));

        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathIdOrderByOrderIndexAsc(path.getId());
        List<LearningPathItemResponse> itemResponses = items.stream()
                .map(this::mapToItemResponse)
                .toList();

        return new TodayLearningPathResponse(
                LocalDate.now().toString(),
                path.getId(),
                path.getTargetLevel(),
                path.getCurrentDay(),
                path.getTotalItems(),
                path.getCompletedItems(),
                itemResponses
        );
    }

    @Override
    @Transactional
    public LearningPathItemResponse startItem(String userEmail, Long itemId) {
        LearningPathItem item = findItemById(itemId);
        validateOwnership(item.getLearningPath(), userEmail);

        item.markInProgress();
        LearningPathItem saved = learningPathItemRepository.save(item);
        return mapToItemResponse(saved);
    }

    @Override
    @Transactional
    public LearningItemCompletionResponse completeItem(String userEmail, Long itemId, LearningItemCompletionRequest request) {
        LearningPathItem item = findItemById(itemId);
        LearningPath path = item.getLearningPath();
        validateOwnership(path, userEmail);

        if (item.getStatus() == LearningItemStatus.COMPLETED) {
            return new LearningItemCompletionResponse(
                    item.getId(),
                    item.getStatus(),
                    item.getItemType(),
                    item.getCompletedAt(),
                    path.getStatus() == LearningPathStatus.COMPLETED,
                    path.getCompletedItems(),
                    path.getTotalItems(),
                    "Item is already completed"
            );
        }

        // Delegate review updates to MemoryService
        if (item.getItemType() == LearningItemType.REVIEW && item.getVocabularyWord() != null) {
            boolean isCorrect = request != null && request.getCorrect() != null ? request.getCorrect() : true;
            long latency = request != null && request.getResponseTimeMs() != null ? request.getResponseTimeMs() : 1500L;
            MemoryAlgorithmType algorithm = request != null && request.getAlgorithm() != null
                    ? request.getAlgorithm()
                    : MemoryAlgorithmType.SM2;

            try {
                memoryService.recordReview(userEmail, new WordReviewRequest(
                        item.getVocabularyWord().getId(),
                        isCorrect,
                        latency,
                        algorithm
                ));
            } catch (Exception e) {
                log.warn("Could not record review with MemoryService for wordId: {}", item.getVocabularyWord().getId(), e);
            }
        }

        item.markCompleted();
        learningPathItemRepository.save(item);

        path.incrementCompleted();
        learningPathRepository.save(path);

        return new LearningItemCompletionResponse(
                item.getId(),
                item.getStatus(),
                item.getItemType(),
                item.getCompletedAt(),
                path.getStatus() == LearningPathStatus.COMPLETED,
                path.getCompletedItems(),
                path.getTotalItems(),
                "Learning item completed successfully"
        );
    }

    @Override
    @Transactional
    public LearningPathResponse regeneratePath(String userEmail) {
        User user = findUserByEmail(userEmail);
        LearningPath path = learningPathRepository.findActivePathByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No active learning path found to regenerate for user: " + userEmail));

        // 1. Delete remaining PENDING items (preserve COMPLETED and IN_PROGRESS)
        learningPathItemRepository.deleteByLearningPathIdAndStatus(path.getId(), LearningItemStatus.PENDING);

        List<LearningPathItem> preservedItems = learningPathItemRepository.findByLearningPathIdOrderByOrderIndexAsc(path.getId());
        int preservedCount = preservedItems.size();
        int remainingSlots = Math.max(0, AdaptiveLearningPathStrategy.MAX_DAILY_ITEMS - preservedCount);

        if (remainingSlots > 0) {
            DifficultyLevel targetLevel = path.getTargetLevel();
            LearningPathContext context = buildContext(user, targetLevel, path.getCurrentDay());

            LearningPathStrategy strategy = strategyFactory.getDefaultStrategy();
            LearningPathResult result = strategy.generatePath(context);

            int nextOrderIndex = preservedCount + 1;
            int added = 0;
            for (LearningPathItemCandidate candidate : result.getItems()) {
                if (added >= remainingSlots) break;

                // Avoid duplicating already preserved items
                if (candidate.getWordId() != null && preservedItems.stream()
                        .anyMatch(p -> p.getVocabularyWord() != null && p.getVocabularyWord().getId().equals(candidate.getWordId()))) {
                    continue;
                }

                LearningPathItem newItem;
                if (candidate.getItemType() == LearningItemType.QUIZ) {
                    Quiz quiz = createDailyQuiz(targetLevel);
                    newItem = new LearningPathItem(path, null, quiz, LearningItemType.QUIZ, candidate.getPriority(), nextOrderIndex++, candidate.getRationale());
                } else {
                    VocabularyWord word = vocabularyWordRepository.findById(candidate.getWordId()).orElse(null);
                    newItem = new LearningPathItem(path, word, null, candidate.getItemType(), candidate.getPriority(), nextOrderIndex++, candidate.getRationale());
                }

                learningPathItemRepository.save(newItem);
                path.addItem(newItem);
                added++;
            }
        }

        path.setTotalItems(learningPathItemRepository.findByLearningPathIdOrderByOrderIndexAsc(path.getId()).size());
        LearningPath savedPath = learningPathRepository.save(path);
        return mapToLearningPathResponse(savedPath);
    }

    @Override
    public List<LearningPathResponse> getPathHistory(String userEmail) {
        User user = findUserByEmail(userEmail);
        List<LearningPath> paths = learningPathRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return paths.stream()
                .map(this::mapToLearningPathResponse)
                .toList();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private LearningPathItem findItemById(Long id) {
        return learningPathItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning path item not found with ID: " + id));
    }

    private void validateOwnership(LearningPath path, String userEmail) {
        if (!path.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Unauthorized to access learning path ID: " + path.getId());
        }
    }

    private DifficultyLevel resolveTargetLevel(User user) {
        List<Assessment> assessments = assessmentRepository.findByUserIdOrderByStartedAtDesc(user.getId());
        for (Assessment a : assessments) {
            if (a.getEstimatedLevel() != null) {
                return a.getEstimatedLevel();
            }
        }

        if (user.getCurrentLevel() != null) {
            try {
                return DifficultyLevel.valueOf(user.getCurrentLevel().name());
            } catch (Exception ignored) {
            }
        }

        return DifficultyLevel.A1;
    }

    private LearningPathContext buildContext(User user, DifficultyLevel targetLevel, int currentDay) {
        List<MemoryWordResponse> dueReviews = memoryService.getDueReviews(user.getEmail());
        List<MemoryWordResponse> weakWords = memoryService.getWeakWords(user.getEmail());

        List<UserWordProgress> progressList = userWordProgressRepository.findByUser(user);
        Set<Long> masteredWordIds = progressList.stream()
                .map(p -> p.getVocabularyWord().getId())
                .collect(Collectors.toSet());

        Double recentAccuracy = null;
        if (!progressList.isEmpty()) {
            double totalMastery = progressList.stream().mapToDouble(UserWordProgress::getMasteryScore).average().orElse(0.0);
            recentAccuracy = totalMastery;
        }

        List<VocabularyWord> targetLevelWords = vocabularyWordRepository.findByDifficultyLevel(targetLevel);
        if (targetLevelWords.isEmpty()) {
            targetLevelWords = vocabularyWordRepository.findAll();
        }

        List<VocabularyWordSummary> availableNewWords = targetLevelWords.stream()
                .map(w -> new VocabularyWordSummary(w.getId(), w.getWord(), w.getMeaning(), w.getDifficultyLevel(), w.getCategory()))
                .toList();

        List<VocabularyWordSummary> availableStretchWords = Collections.emptyList();
        if (targetLevel.ordinal() < DifficultyLevel.values().length - 1) {
            DifficultyLevel nextLevel = DifficultyLevel.values()[targetLevel.ordinal() + 1];
            List<VocabularyWord> stretchWords = vocabularyWordRepository.findByDifficultyLevel(nextLevel);
            availableStretchWords = stretchWords.stream()
                    .map(w -> new VocabularyWordSummary(w.getId(), w.getWord(), w.getMeaning(), w.getDifficultyLevel(), w.getCategory()))
                    .toList();
        }

        return new LearningPathContext(
                targetLevel,
                dueReviews,
                weakWords,
                masteredWordIds,
                availableNewWords,
                availableStretchWords,
                recentAccuracy,
                currentDay
        );
    }

    private void populateLearningPathItems(LearningPath learningPath,
                                           List<LearningPathItemCandidate> candidates,
                                           DifficultyLevel targetLevel) {
        int orderIndex = 1;
        for (LearningPathItemCandidate candidate : candidates) {
            if (candidate.getItemType() == LearningItemType.QUIZ) {
                Quiz quiz = createDailyQuiz(targetLevel);
                LearningPathItem item = new LearningPathItem(
                        learningPath,
                        null,
                        quiz,
                        LearningItemType.QUIZ,
                        candidate.getPriority(),
                        orderIndex++,
                        candidate.getRationale()
                );
                learningPath.addItem(item);
            } else if (candidate.getWordId() != null) {
                VocabularyWord word = vocabularyWordRepository.findById(candidate.getWordId()).orElse(null);
                LearningPathItem item = new LearningPathItem(
                        learningPath,
                        word,
                        null,
                        candidate.getItemType(),
                        candidate.getPriority(),
                        orderIndex++,
                        candidate.getRationale()
                );
                learningPath.addItem(item);
            }
        }
    }

    private Quiz createDailyQuiz(DifficultyLevel level) {
        try {
            QuizResponse response = quizService.generateQuiz(new QuizGenerationRequest(level, 3, null));
            return quizRepository.findById(response.getId()).orElse(null);
        } catch (Exception e) {
            log.warn("Could not generate consolidating daily quiz for level: {}", level, e);
            return null;
        }
    }

    private LearningPathResponse mapToLearningPathResponse(LearningPath path) {
        List<LearningPathItem> items = learningPathItemRepository.findByLearningPathIdOrderByOrderIndexAsc(path.getId());
        List<LearningPathItemResponse> itemResponses = items.stream()
                .map(this::mapToItemResponse)
                .toList();

        return new LearningPathResponse(
                path.getId(),
                path.getStatus(),
                path.getTargetLevel(),
                path.getCurrentDay(),
                path.getTotalItems(),
                path.getCompletedItems(),
                itemResponses,
                path.getCreatedAt()
        );
    }

    private LearningPathItemResponse mapToItemResponse(LearningPathItem item) {
        VocabularyWord word = item.getVocabularyWord();
        return new LearningPathItemResponse(
                item.getId(),
                item.getItemType(),
                word != null ? word.getId() : null,
                word != null ? word.getWord() : null,
                word != null ? word.getMeaning() : null,
                item.getQuiz() != null ? item.getQuiz().getId() : null,
                item.getPriority(),
                item.getStatus(),
                item.getOrderIndex(),
                item.getNotes(),
                item.getScheduledAt(),
                item.getCompletedAt()
        );
    }
}
