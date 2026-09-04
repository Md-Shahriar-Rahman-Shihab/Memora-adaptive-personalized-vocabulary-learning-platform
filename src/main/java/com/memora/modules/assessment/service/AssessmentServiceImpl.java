package com.memora.modules.assessment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.assessment.domain.AssessmentPerformance;
import com.memora.modules.assessment.domain.AssessmentStatus;
import com.memora.modules.assessment.domain.PlacementResult;
import com.memora.modules.assessment.dto.*;
import com.memora.modules.assessment.entity.Assessment;
import com.memora.modules.assessment.entity.AssessmentAnswer;
import com.memora.modules.assessment.entity.AssessmentQuestion;
import com.memora.modules.assessment.factory.PlacementStrategyFactory;
import com.memora.modules.assessment.repository.AssessmentAnswerRepository;
import com.memora.modules.assessment.repository.AssessmentQuestionRepository;
import com.memora.modules.assessment.repository.AssessmentRepository;
import com.memora.modules.assessment.strategy.PlacementAlgorithmStrategy;
import com.memora.modules.quiz.dto.EvaluationResult;
import com.memora.modules.quiz.entity.FillInTheBlankQuestion;
import com.memora.modules.quiz.entity.MultipleChoiceQuestion;
import com.memora.modules.quiz.entity.Question;
import com.memora.modules.quiz.factory.QuestionEvaluatorFactory;
import com.memora.modules.quiz.strategy.QuestionEvaluatorStrategy;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import com.memora.modules.gamification.service.GamificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * Core implementation of {@link AssessmentService}.
 * Orchestrates multi-tier question generation, answer collection, and strategy-based CEFR placement.
 * Completely isolates placement testing from memory SRS progression.
 */
@Service
@Transactional(readOnly = true)
public class AssessmentServiceImpl implements AssessmentService {

    private static final Logger log = LoggerFactory.getLogger(AssessmentServiceImpl.class);

    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository assessmentQuestionRepository;
    private final AssessmentAnswerRepository assessmentAnswerRepository;
    private final UserRepository userRepository;
    private final AssessmentQuestionGenerator questionGenerator;
    private final QuestionEvaluatorFactory evaluatorFactory;
    private final PlacementStrategyFactory placementStrategyFactory;
    private final ObjectMapper objectMapper;
    private final GamificationService gamificationService;

    @Autowired
    public AssessmentServiceImpl(AssessmentRepository assessmentRepository,
                                 AssessmentQuestionRepository assessmentQuestionRepository,
                                 AssessmentAnswerRepository assessmentAnswerRepository,
                                 UserRepository userRepository,
                                 AssessmentQuestionGenerator questionGenerator,
                                 QuestionEvaluatorFactory evaluatorFactory,
                                 PlacementStrategyFactory placementStrategyFactory,
                                 ObjectMapper objectMapper,
                                 GamificationService gamificationService) {
        this.assessmentRepository = assessmentRepository;
        this.assessmentQuestionRepository = assessmentQuestionRepository;
        this.assessmentAnswerRepository = assessmentAnswerRepository;
        this.userRepository = userRepository;
        this.questionGenerator = questionGenerator;
        this.evaluatorFactory = evaluatorFactory;
        this.placementStrategyFactory = placementStrategyFactory;
        this.objectMapper = objectMapper;
        this.gamificationService = gamificationService;
    }

    public AssessmentServiceImpl(AssessmentRepository assessmentRepository,
                                 AssessmentQuestionRepository assessmentQuestionRepository,
                                 AssessmentAnswerRepository assessmentAnswerRepository,
                                 UserRepository userRepository,
                                 AssessmentQuestionGenerator questionGenerator,
                                 QuestionEvaluatorFactory evaluatorFactory,
                                 PlacementStrategyFactory placementStrategyFactory,
                                 ObjectMapper objectMapper) {
        this(assessmentRepository, assessmentQuestionRepository, assessmentAnswerRepository, userRepository,
             questionGenerator, evaluatorFactory, placementStrategyFactory, objectMapper, null);
    }

    @Override
    @Transactional
    public AssessmentStartResponse startAssessment(String userEmail) {
        User user = findUserByEmail(userEmail);

        // Create new assessment session
        Assessment assessment = new Assessment(user, 20);
        List<AssessmentQuestion> questions = questionGenerator.generateAssessmentQuestions(assessment);
        assessment.setTotalQuestions(questions.size());
        for (AssessmentQuestion aq : questions) {
            assessment.addAssessmentQuestion(aq);
        }

        Assessment savedAssessment = assessmentRepository.save(assessment);
        List<AssessmentQuestionResponse> questionResponses = questions.stream()
                .map(this::mapToQuestionResponse)
                .toList();

        return new AssessmentStartResponse(
                savedAssessment.getId(),
                savedAssessment.getStatus(),
                savedAssessment.getTotalQuestions(),
                questionResponses
        );
    }

    @Override
    public AssessmentDetailResponse getAssessment(String userEmail, Long assessmentId) {
        Assessment assessment = findAssessmentById(assessmentId);
        validateOwnership(assessment, userEmail);

        List<AssessmentQuestion> questions = assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(assessmentId);
        int answeredCount = assessmentAnswerRepository.findByAssessmentId(assessmentId).size();
        int remainingCount = Math.max(0, assessment.getTotalQuestions() - answeredCount);

        List<AssessmentQuestionResponse> questionResponses = questions.stream()
                .map(this::mapToQuestionResponse)
                .toList();

        return new AssessmentDetailResponse(
                assessment.getId(),
                assessment.getStatus(),
                assessment.getTotalQuestions(),
                answeredCount,
                remainingCount,
                questionResponses
        );
    }

    @Override
    @Transactional
    public AssessmentAnswerResponse submitAnswer(String userEmail, Long assessmentId, Long questionId, AssessmentAnswerRequest request) {
        Assessment assessment = findAssessmentById(assessmentId);
        validateOwnership(assessment, userEmail);

        if (assessment.getStatus() != AssessmentStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Cannot submit answer: Assessment is already " + assessment.getStatus());
        }

        if (request.getResponseTimeMs() == null || request.getResponseTimeMs() < 0) {
            throw new IllegalArgumentException("Response time must be greater than or equal to 0 ms");
        }

        // Locate AssessmentQuestion by AssessmentQuestion ID or inner Question ID
        AssessmentQuestion aq = assessmentQuestionRepository.findByAssessmentIdAndQuestionId(assessmentId, questionId)
                .or(() -> assessmentQuestionRepository.findById(questionId)
                        .filter(q -> q.getAssessment().getId().equals(assessmentId)))
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with ID " + questionId + " in assessment ID " + assessmentId));

        if (assessmentAnswerRepository.existsByAssessmentIdAndAssessmentQuestionId(assessmentId, aq.getId())) {
            throw new IllegalArgumentException("An answer has already been submitted for question ID: " + questionId);
        }

        Question question = (Question) org.hibernate.Hibernate.unproxy(aq.getQuestion());
        QuestionEvaluatorStrategy evaluator = evaluatorFactory.getEvaluator(question.getQuestionType());
        EvaluationResult evaluationResult = evaluator.evaluate(question, request.getAnswer());

        AssessmentAnswer answer = new AssessmentAnswer(
                assessment,
                aq,
                request.getAnswer(),
                evaluationResult.isCorrect(),
                request.getResponseTimeMs()
        );

        assessmentAnswerRepository.save(answer);
        assessment.addAssessmentAnswer(answer);
        assessmentRepository.save(assessment);

        return new AssessmentAnswerResponse(
                evaluationResult.isCorrect(),
                evaluationResult.getFeedback(),
                request.getResponseTimeMs()
        );
    }

    @Override
    @Transactional
    public PlacementResultResponse completeAssessment(String userEmail, Long assessmentId) {
        Assessment assessment = findAssessmentById(assessmentId);
        validateOwnership(assessment, userEmail);

        if (assessment.getStatus() == AssessmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Assessment ID " + assessmentId + " has already been completed");
        }

        List<AssessmentQuestion> allQuestions = assessmentQuestionRepository.findByAssessmentIdOrderByOrderIndexAsc(assessmentId);
        List<AssessmentAnswer> answers = assessmentAnswerRepository.findByAssessmentId(assessmentId);

        if (answers.size() < allQuestions.size()) {
            throw new IllegalArgumentException(String.format(
                    "Cannot complete assessment: %d questions answered out of %d required.",
                    answers.size(), allQuestions.size()
            ));
        }

        // Map answers by AssessmentQuestion ID
        Map<Long, AssessmentAnswer> answerMap = new HashMap<>();
        for (AssessmentAnswer a : answers) {
            answerMap.put(a.getAssessmentQuestion().getId(), a);
        }

        // Group questions and answers by CEFR level
        Map<DifficultyLevel, List<AssessmentQuestion>> questionsByLevel = new EnumMap<>(DifficultyLevel.class);
        for (AssessmentQuestion q : allQuestions) {
            questionsByLevel.computeIfAbsent(q.getDifficultyLevel(), k -> new ArrayList<>()).add(q);
        }

        List<AssessmentPerformance> performances = new ArrayList<>();
        for (Map.Entry<DifficultyLevel, List<AssessmentQuestion>> entry : questionsByLevel.entrySet()) {
            DifficultyLevel level = entry.getKey();
            List<AssessmentQuestion> levelQs = entry.getValue();

            int levelTotal = levelQs.size();
            int levelCorrect = 0;
            long levelResponseTimeSum = 0;

            for (AssessmentQuestion q : levelQs) {
                AssessmentAnswer ans = answerMap.get(q.getId());
                if (ans != null) {
                    if (ans.isCorrect()) levelCorrect++;
                    levelResponseTimeSum += ans.getResponseTimeMs();
                }
            }

            double levelAccuracy = levelTotal > 0 ? ((double) levelCorrect / levelTotal) * 100.0 : 0.0;
            double avgResponseTime = levelTotal > 0 ? (double) levelResponseTimeSum / levelTotal : 0.0;

            performances.add(new AssessmentPerformance(level, levelTotal, levelCorrect, levelAccuracy, avgResponseTime));
        }

        // Delegate placement determination to Strategy
        PlacementAlgorithmStrategy placementStrategy = placementStrategyFactory.getDefaultStrategy();
        PlacementResult result = placementStrategy.calculate(performances);

        // Update and finalize assessment entity
        assessment.setStatus(AssessmentStatus.COMPLETED);
        assessment.setCompletedAt(Instant.now());
        assessment.setEstimatedLevel(result.getEstimatedLevel());
        assessment.setConfidenceScore(result.getConfidenceScore());
        assessment.setAccuracy(result.getAccuracy());

        try {
            assessment.setLevelPerformanceJson(objectMapper.writeValueAsString(result.getLevelPerformance()));
        } catch (Exception e) {
            log.warn("Failed to serialize level performance JSON for assessment ID: {}", assessmentId, e);
        }

        assessmentRepository.save(assessment);

        User user = assessment.getUser();
        try {
            user.setCurrentLevel(VocabularyLevel.valueOf(result.getEstimatedLevel().name()));
            userRepository.save(user);
        } catch (Exception e) {
            log.warn("Could not map DifficultyLevel {} to VocabularyLevel for user {}", result.getEstimatedLevel(), user.getEmail());
        }

        if (gamificationService != null) {
            try {
                gamificationService.recordActivity(user, RewardActivityType.ASSESSMENT, RewardContext.forAssessment(assessment.getId()));
            } catch (Exception e) {
                log.warn("Gamification tracking failed for assessment completion: {}", e.getMessage());
            }
        }

        return new PlacementResultResponse(
                assessment.getId(),
                result.getEstimatedLevel(),
                result.getConfidenceScore(),
                result.getTotalQuestions(),
                result.getCorrectAnswers(),
                result.getAccuracy(),
                result.getLevelPerformance()
        );
    }

    @Override
    public PlacementResultResponse getAssessmentResult(String userEmail, Long assessmentId) {
        Assessment assessment = findAssessmentById(assessmentId);
        validateOwnership(assessment, userEmail);

        if (assessment.getStatus() != AssessmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Assessment ID " + assessmentId + " is not yet completed");
        }

        Map<DifficultyLevel, Double> levelPerf = parseLevelPerformance(assessment.getLevelPerformanceJson());

        return new PlacementResultResponse(
                assessment.getId(),
                assessment.getEstimatedLevel(),
                assessment.getConfidenceScore() != null ? assessment.getConfidenceScore() : 0.0,
                assessment.getTotalQuestions(),
                assessment.getCorrectAnswers(),
                assessment.getAccuracy() != null ? assessment.getAccuracy() : 0.0,
                levelPerf
        );
    }

    @Override
    public List<PlacementResultResponse> getAssessmentHistory(String userEmail) {
        User user = findUserByEmail(userEmail);
        List<Assessment> assessments = assessmentRepository.findByUserIdOrderByStartedAtDesc(user.getId());

        return assessments.stream()
                .filter(a -> a.getStatus() == AssessmentStatus.COMPLETED)
                .map(a -> new PlacementResultResponse(
                        a.getId(),
                        a.getEstimatedLevel(),
                        a.getConfidenceScore() != null ? a.getConfidenceScore() : 0.0,
                        a.getTotalQuestions(),
                        a.getCorrectAnswers(),
                        a.getAccuracy() != null ? a.getAccuracy() : 0.0,
                        parseLevelPerformance(a.getLevelPerformanceJson())
                ))
                .toList();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Assessment findAssessmentById(Long id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found with ID: " + id));
    }

    private void validateOwnership(Assessment assessment, String userEmail) {
        if (!assessment.getUser().getEmail().equalsIgnoreCase(userEmail)) {
            throw new AccessDeniedException("Unauthorized to access assessment ID: " + assessment.getId());
        }
    }

    private AssessmentQuestionResponse mapToQuestionResponse(AssessmentQuestion aq) {
        Question q = (Question) org.hibernate.Hibernate.unproxy(aq.getQuestion());
        String sentence = null;
        List<String> options = null;

        if (q instanceof MultipleChoiceQuestion mcq) {
            options = mcq.getOptions();
        } else if (q instanceof FillInTheBlankQuestion fib) {
            sentence = fib.getSentence();
        }

        return new AssessmentQuestionResponse(
                q.getId(),
                q.getQuestionType(),
                q.getQuestionText(),
                sentence,
                options,
                aq.getDifficultyLevel()
        );
    }

    private Map<DifficultyLevel, Double> parseLevelPerformance(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<DifficultyLevel, Double>>() {});
        } catch (Exception e) {
            log.warn("Failed to deserialize levelPerformanceJson: {}", json);
            return Collections.emptyMap();
        }
    }
}
