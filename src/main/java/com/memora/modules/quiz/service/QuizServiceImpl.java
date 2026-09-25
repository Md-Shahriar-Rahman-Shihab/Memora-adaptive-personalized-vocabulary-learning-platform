package com.memora.modules.quiz.service;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.memory.domain.MemoryAlgorithmType;
import com.memora.modules.memory.dto.WordReviewRequest;
import com.memora.modules.memory.dto.WordReviewResponse;
import com.memora.modules.memory.service.MemoryService;
import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.dto.*;
import com.memora.modules.quiz.entity.*;
import com.memora.modules.quiz.factory.QuestionEvaluatorFactory;
import com.memora.modules.quiz.factory.QuestionFactory;
import com.memora.modules.quiz.repository.QuestionAttemptRepository;
import com.memora.modules.quiz.repository.QuestionRepository;
import com.memora.modules.quiz.repository.QuizAttemptRepository;
import com.memora.modules.quiz.repository.QuizRepository;
import com.memora.modules.quiz.strategy.QuestionEvaluatorStrategy;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import com.memora.modules.gamification.service.GamificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service implementation orchestrating the complete Quiz workflow.
 * Encapsulates object creation via {@link QuestionFactory}, evaluation via {@link QuestionEvaluatorFactory},
 * and memory retention synchronization via {@link MemoryService}.
 */
@Service
@Transactional(readOnly = true)
public class QuizServiceImpl implements QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizServiceImpl.class);

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuestionAttemptRepository questionAttemptRepository;
    private final UserRepository userRepository;
    private final VocabularyWordRepository vocabularyWordRepository;
    private final QuestionFactory questionFactory;
    private final QuestionEvaluatorFactory evaluatorFactory;
    private final MemoryService memoryService;
    private final GamificationService gamificationService;

    @Autowired(required = false)
    private com.memora.modules.learningpath.repository.LearningPathRepository learningPathRepository;

    @Autowired(required = false)
    private com.memora.modules.learningpath.repository.LearningPathItemRepository learningPathItemRepository;

    @Autowired
    public QuizServiceImpl(QuizRepository quizRepository,
                           QuestionRepository questionRepository,
                           QuizAttemptRepository quizAttemptRepository,
                           QuestionAttemptRepository questionAttemptRepository,
                           UserRepository userRepository,
                           VocabularyWordRepository vocabularyWordRepository,
                           QuestionFactory questionFactory,
                           QuestionEvaluatorFactory evaluatorFactory,
                           MemoryService memoryService,
                           GamificationService gamificationService) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.questionAttemptRepository = questionAttemptRepository;
        this.userRepository = userRepository;
        this.vocabularyWordRepository = vocabularyWordRepository;
        this.questionFactory = questionFactory;
        this.evaluatorFactory = evaluatorFactory;
        this.memoryService = memoryService;
        this.gamificationService = gamificationService;
    }

    public QuizServiceImpl(QuizRepository quizRepository,
                           QuestionRepository questionRepository,
                           QuizAttemptRepository quizAttemptRepository,
                           QuestionAttemptRepository questionAttemptRepository,
                           UserRepository userRepository,
                           VocabularyWordRepository vocabularyWordRepository,
                           QuestionFactory questionFactory,
                           QuestionEvaluatorFactory evaluatorFactory,
                           MemoryService memoryService) {
        this(quizRepository, questionRepository, quizAttemptRepository, questionAttemptRepository,
             userRepository, vocabularyWordRepository, questionFactory, evaluatorFactory, memoryService, null);
    }

    @Override
    @Transactional
    public QuizResponse generateQuiz(QuizGenerationRequest request) {
        DifficultyLevel level = (request != null && request.getDifficultyLevel() != null)
                ? request.getDifficultyLevel() : DifficultyLevel.A1;
        int count = (request != null && request.getQuestionCount() != null)
                ? request.getQuestionCount() : 5;

        List<QuestionType> allowedTypes = (request != null && request.getQuestionTypes() != null && !request.getQuestionTypes().isEmpty())
                ? request.getQuestionTypes() : List.of(QuestionType.values());

        List<VocabularyWord> words = vocabularyWordRepository.findByDifficultyLevel(level);
        if (words.isEmpty()) {
            words = vocabularyWordRepository.findAll();
        }
        if (words.isEmpty()) {
            throw new ResourceNotFoundException("No vocabulary words available to generate a quiz.");
        }

        List<VocabularyWord> candidateWords = new ArrayList<>(words);
        Collections.shuffle(candidateWords);
        int selectedCount = Math.min(count, candidateWords.size());
        List<VocabularyWord> selectedWords = candidateWords.subList(0, selectedCount);

        String title = String.format("%s Vocabulary Practice Quiz", level.name());
        Quiz quiz = new Quiz(title, level, selectedCount);

        for (int i = 0; i < selectedWords.size(); i++) {
            VocabularyWord word = selectedWords.get(i);
            QuestionType type = allowedTypes.get(i % allowedTypes.size());
            Question question = questionFactory.createQuestion(type, word, candidateWords);
            quiz.addQuestion(question);
        }

        Quiz savedQuiz = quizRepository.save(quiz);
        return mapToQuizResponse(savedQuiz);
    }

    @Override
    @Transactional
    public QuizResponse startQuiz(String userEmail, Long quizId) {
        User user = findUserByEmail(userEmail);
        Quiz quiz = findQuizById(quizId);

        QuizAttempt attempt = quizAttemptRepository.findActiveAttempt(quizId, user.getId())
                .orElseGet(() -> {
                    QuizAttempt newAttempt = new QuizAttempt(user, quiz, quiz.getQuestions().size());
                    return quizAttemptRepository.save(newAttempt);
                });

        List<Long> answeredQuestionIds = attempt.getQuestionAttempts().stream()
                .map(qa -> qa.getQuestion().getId())
                .toList();

        List<QuestionResponse> questionResponses = new ArrayList<>();
        if (quiz.getQuestions() != null) {
            for (Question q : quiz.getQuestions()) {
                questionResponses.add(mapToQuestionResponse(q));
            }
        }

        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDifficultyLevel(),
                quiz.getQuestionCount(),
                questionResponses,
                attempt.getId(),
                answeredQuestionIds
        );
    }

    @Override
    public QuizResponse getQuiz(Long quizId) {
        Quiz quiz = findQuizById(quizId);
        return mapToQuizResponse(quiz);
    }

    @Override
    @Transactional
    public AnswerResponse submitAnswer(String userEmail, Long quizId, Long questionId, AnswerSubmissionRequest request) {
        User user = findUserByEmail(userEmail);
        Quiz quiz = findQuizById(quizId);

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID: " + questionId));

        if (question.getQuiz() == null || !question.getQuiz().getId().equals(quizId)) {
            throw new IllegalArgumentException("Question ID " + questionId + " does not belong to quiz ID " + quizId);
        }

        QuizAttempt quizAttempt = quizAttemptRepository.findActiveAttempt(quizId, user.getId())
                .orElseGet(() -> {
                    QuizAttempt newAttempt = new QuizAttempt(user, quiz, quiz.getQuestions().size());
                    return quizAttemptRepository.save(newAttempt);
                });

        // Check if question was already answered in this attempt (idempotency guard)
        Optional<QuestionAttempt> existingAttempt = questionAttemptRepository
                .findByQuizAttemptIdAndQuestionId(quizAttempt.getId(), questionId);
        if (existingAttempt.isPresent()) {
            QuestionAttempt prev = existingAttempt.get();
            return new AnswerResponse(
                    prev.isCorrect(),
                    prev.getScore(),
                    prev.isCorrect() ? "Correct! Well done." : "Incorrect answer.",
                    0.0,
                    null,
                    null,
                    getQuestionCorrectAnswer(question),
                    0
            );
        }

        QuestionEvaluatorStrategy evaluator = evaluatorFactory.getEvaluator(question.getQuestionType());
        EvaluationResult evaluationResult = evaluator.evaluate(question, request.getAnswer());

        QuestionAttempt questionAttempt = new QuestionAttempt(
                quizAttempt,
                question,
                request.getAnswer(),
                evaluationResult.isCorrect(),
                evaluationResult.getScore(),
                request.getResponseTimeMs()
        );
        questionAttemptRepository.save(questionAttempt);
        quizAttempt.addQuestionAttempt(questionAttempt);
        quizAttemptRepository.save(quizAttempt);

        long safeResponseTime = Math.max(1L, request.getResponseTimeMs());
        WordReviewRequest reviewRequest = new WordReviewRequest(
                question.getVocabularyWord().getId(),
                evaluationResult.isCorrect(),
                safeResponseTime,
                MemoryAlgorithmType.SM2
        );
        WordReviewResponse reviewResponse = memoryService.recordReview(userEmail, reviewRequest);

        int answerXp = 0;
        if (gamificationService != null && evaluationResult.isCorrect()) {
            try {
                com.memora.modules.gamification.dto.GamificationActivityResultResponse gamResult =
                        gamificationService.recordActivity(user, RewardActivityType.QUIZ,
                                RewardContext.forQuizAnswer(questionAttempt.getId(), true));
                if (gamResult != null) {
                    answerXp = gamResult.getXpEarned();
                }
            } catch (Exception e) {
                log.warn("Gamification tracking failed for quiz answer: {}", e.getMessage());
            }
        }

        return new AnswerResponse(
                evaluationResult.isCorrect(),
                evaluationResult.getScore(),
                evaluationResult.getFeedback(),
                reviewResponse.getMasteryScore(),
                reviewResponse.getForgettingRisk(),
                reviewResponse.getNextReviewAt(),
                getQuestionCorrectAnswer(question),
                answerXp
        );
    }

    @Override
    @Transactional
    public QuizResultResponse completeQuiz(String userEmail, Long quizId) {
        User user = findUserByEmail(userEmail);
        Quiz quiz = findQuizById(quizId);

        QuizAttempt attempt = quizAttemptRepository.findActiveAttempt(quizId, user.getId())
                .orElseGet(() -> {
                    List<QuizAttempt> attempts = quizAttemptRepository.findLatestAttemptsForQuiz(quizId, user.getId());
                    if (attempts.isEmpty()) {
                        throw new ResourceNotFoundException("No quiz attempt found for quiz ID: " + quizId);
                    }
                    return attempts.get(0);
                });

        boolean alreadyCompleted = attempt.getCompletedAt() != null;
        if (!alreadyCompleted) {
            attempt.setCompletedAt(Instant.now());
            quizAttemptRepository.save(attempt);
        }

        double percentage = attempt.getTotalQuestions() > 0
                ? ((double) attempt.getCorrectAnswers() / attempt.getTotalQuestions()) * 100.0
                : 0.0;

        int totalXpEarned = 0;
        int currentStreak = 0;
        int totalXp = 0;
        List<String> newAchievements = new ArrayList<>();
        boolean pathCompleted = false;

        if (gamificationService != null && !alreadyCompleted) {
            try {
                boolean isPerfect = attempt.getTotalQuestions() > 0 && attempt.getCorrectAnswers() == attempt.getTotalQuestions();
                com.memora.modules.gamification.dto.GamificationActivityResultResponse gamResult =
                        gamificationService.recordActivity(user, RewardActivityType.QUIZ,
                                RewardContext.forQuizCompletion(quizId, attempt.getCorrectAnswers(), attempt.getTotalQuestions(), isPerfect));
                if (gamResult != null) {
                    totalXpEarned = gamResult.getXpEarned();
                    currentStreak = gamResult.getCurrentStreak();
                    totalXp = gamResult.getNewTotalXp();
                    if (gamResult.getNewAchievements() != null) {
                        for (com.memora.modules.gamification.dto.UserAchievementResponse ach : gamResult.getNewAchievements()) {
                            newAchievements.add(ach.getName());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Gamification tracking failed for quiz completion: {}", e.getMessage());
            }
        }

        // Sync with Learning Path if this quiz is part of user's active curriculum
        if (learningPathItemRepository != null && learningPathRepository != null && !alreadyCompleted) {
            try {
                List<com.memora.modules.learningpath.entity.LearningPathItem> pathItems = learningPathItemRepository.findByQuizId(quizId);
                if (pathItems.isEmpty()) {
                    // Fallback: find any pending QUIZ item in the user's active learning path
                    learningPathRepository.findActivePathByUserId(user.getId()).ifPresent(activePath -> {
                        List<com.memora.modules.learningpath.entity.LearningPathItem> allItems =
                                learningPathItemRepository.findByLearningPathIdOrderByOrderIndexAsc(activePath.getId());
                        for (com.memora.modules.learningpath.entity.LearningPathItem it : allItems) {
                            if (it.getItemType() == com.memora.modules.learningpath.domain.LearningItemType.QUIZ
                                    && it.getStatus() != com.memora.modules.learningpath.domain.LearningItemStatus.COMPLETED) {
                                pathItems.add(it);
                                break;
                            }
                        }
                    });
                }

                for (com.memora.modules.learningpath.entity.LearningPathItem item : pathItems) {
                    if (item.getStatus() == com.memora.modules.learningpath.domain.LearningItemStatus.COMPLETED) {
                        continue;
                    }
                    item.markCompleted();
                    learningPathItemRepository.save(item);

                    com.memora.modules.learningpath.entity.LearningPath path = item.getLearningPath();
                    path.incrementCompleted();
                    // Completing the consolidating Daily Retention Quiz finishes today's path!
                    path.setStatus(com.memora.modules.learningpath.domain.LearningPathStatus.COMPLETED);
                    learningPathRepository.save(path);
                    pathCompleted = true;
                    log.info("Concluded learning path ID: {} after daily retention quiz completion", path.getId());
                }
            } catch (Exception e) {
                log.error("Could not sync quiz completion with learning path: {}", e.getMessage(), e);
            }
        }

        return new QuizResultResponse(
                quizId,
                attempt.getTotalQuestions(),
                attempt.getCorrectAnswers(),
                attempt.getTotalScore(),
                Math.round(percentage * 100.0) / 100.0,
                totalXpEarned,
                currentStreak,
                totalXp,
                newAchievements,
                pathCompleted
        );
    }

    @Override
    public QuizResultResponse getQuizResult(String userEmail, Long quizId) {
        User user = findUserByEmail(userEmail);
        List<QuizAttempt> attempts = quizAttemptRepository.findLatestAttemptsForQuiz(quizId, user.getId());
        if (attempts.isEmpty()) {
            throw new ResourceNotFoundException("No quiz attempts found for quiz ID: " + quizId);
        }

        QuizAttempt attempt = attempts.get(0);
        double percentage = attempt.getTotalQuestions() > 0
                ? ((double) attempt.getCorrectAnswers() / attempt.getTotalQuestions()) * 100.0
                : 0.0;

        return new QuizResultResponse(
                quizId,
                attempt.getTotalQuestions(),
                attempt.getCorrectAnswers(),
                attempt.getTotalScore(),
                Math.round(percentage * 100.0) / 100.0
        );
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Quiz findQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with ID: " + id));
    }

    private String getQuestionCorrectAnswer(Question question) {
        if (question instanceof MultipleChoiceQuestion mcq) {
            return mcq.getCorrectOption();
        } else if (question instanceof TranslationQuestion tq) {
            return tq.getExpectedAnswer();
        } else if (question instanceof FillInTheBlankQuestion fib) {
            return fib.getExpectedAnswer();
        }
        return null;
    }

    private QuizResponse mapToQuizResponse(Quiz quiz) {
        List<QuestionResponse> questionResponses = new ArrayList<>();
        if (quiz.getQuestions() != null) {
            for (Question q : quiz.getQuestions()) {
                questionResponses.add(mapToQuestionResponse(q));
            }
        }
        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDifficultyLevel(),
                quiz.getQuestionCount(),
                questionResponses
        );
    }

    private QuestionResponse mapToQuestionResponse(Question question) {
        String sentence = null;
        List<String> options = null;

        if (question instanceof MultipleChoiceQuestion mcq) {
            options = mcq.getOptions();
        } else if (question instanceof FillInTheBlankQuestion fib) {
            sentence = fib.getSentence();
        }

        return new QuestionResponse(
                question.getId(),
                question.getQuestionType(),
                question.getVocabularyWord() != null ? question.getVocabularyWord().getWord() : null,
                question.getQuestionText(),
                sentence,
                options,
                question.getPoints(),
                question.getVocabularyWord() != null ? question.getVocabularyWord().getDifficultyLevel() : null
        );
    }
}
