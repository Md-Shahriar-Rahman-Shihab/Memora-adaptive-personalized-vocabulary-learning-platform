package com.memora.modules.partner.challenge.service;

import com.memora.common.exception.BadRequestException;
import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.gamification.domain.RewardActivityType;
import com.memora.modules.gamification.domain.RewardContext;
import com.memora.modules.gamification.dto.GamificationActivityResultResponse;
import com.memora.modules.gamification.service.GamificationService;
import com.memora.modules.partner.challenge.domain.ChallengeStatus;
import com.memora.modules.partner.challenge.dto.*;
import com.memora.modules.partner.challenge.entity.ChallengeAttempt;
import com.memora.modules.partner.challenge.entity.ChallengeQuestionAttempt;
import com.memora.modules.partner.challenge.entity.VocabularyChallenge;
import com.memora.modules.partner.challenge.repository.ChallengeAttemptRepository;
import com.memora.modules.partner.challenge.repository.VocabularyChallengeRepository;
import com.memora.modules.partner.domain.PartnerActivityType;
import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerRelationshipRepository;
import com.memora.modules.partner.service.PartnerActivityService;
import com.memora.modules.quiz.dto.EvaluationResult;
import com.memora.modules.quiz.domain.QuestionType;
import com.memora.modules.quiz.entity.*;
import com.memora.modules.quiz.factory.QuestionEvaluatorFactory;
import com.memora.modules.quiz.factory.QuestionFactory;
import com.memora.modules.quiz.repository.QuizRepository;
import com.memora.modules.quiz.strategy.QuestionEvaluatorStrategy;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation managing vocabulary challenges between accepted partners.
 * Reuses {@link QuestionFactory} for question generation, {@link QuestionEvaluatorFactory} for polymorphic
 * evaluation, and {@link GamificationService} for XP rewards.
 */
@Service
@Transactional(readOnly = true)
public class VocabularyChallengeServiceImpl implements VocabularyChallengeService {

    private static final Logger log = LoggerFactory.getLogger(VocabularyChallengeServiceImpl.class);

    private final VocabularyChallengeRepository challengeRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final PartnerRelationshipRepository partnerRelationshipRepository;
    private final UserRepository userRepository;
    private final VocabularyWordRepository vocabularyWordRepository;
    private final QuizRepository quizRepository;
    private final QuestionFactory questionFactory;
    private final QuestionEvaluatorFactory evaluatorFactory;
    private final GamificationService gamificationService;
    private final PartnerActivityService partnerActivityService;

    @Autowired
    public VocabularyChallengeServiceImpl(VocabularyChallengeRepository challengeRepository,
                                         ChallengeAttemptRepository challengeAttemptRepository,
                                         PartnerRelationshipRepository partnerRelationshipRepository,
                                         UserRepository userRepository,
                                         VocabularyWordRepository vocabularyWordRepository,
                                         QuizRepository quizRepository,
                                         QuestionFactory questionFactory,
                                         QuestionEvaluatorFactory evaluatorFactory,
                                         @Autowired(required = false) GamificationService gamificationService,
                                         @Autowired(required = false) PartnerActivityService partnerActivityService) {
        this.challengeRepository = challengeRepository;
        this.challengeAttemptRepository = challengeAttemptRepository;
        this.partnerRelationshipRepository = partnerRelationshipRepository;
        this.userRepository = userRepository;
        this.vocabularyWordRepository = vocabularyWordRepository;
        this.quizRepository = quizRepository;
        this.questionFactory = questionFactory;
        this.evaluatorFactory = evaluatorFactory;
        this.gamificationService = gamificationService;
        this.partnerActivityService = partnerActivityService;
    }

    public VocabularyChallengeServiceImpl(VocabularyChallengeRepository challengeRepository,
                                          ChallengeAttemptRepository challengeAttemptRepository,
                                          PartnerRelationshipRepository partnerRelationshipRepository,
                                          UserRepository userRepository,
                                          VocabularyWordRepository vocabularyWordRepository,
                                          QuizRepository quizRepository,
                                          QuestionFactory questionFactory,
                                          QuestionEvaluatorFactory evaluatorFactory,
                                          GamificationService gamificationService) {
        this(challengeRepository, challengeAttemptRepository, partnerRelationshipRepository, userRepository,
                vocabularyWordRepository, quizRepository, questionFactory, evaluatorFactory, gamificationService, null);
    }

    @Override
    @Transactional
    public ChallengeResponse createChallenge(String currentUserEmail, CreateChallengeRequest request) {
        User challenger = findUserByEmail(currentUserEmail);
        if (request == null || request.getPartnerId() == null) {
            throw new BadRequestException("Partner ID is required to create a challenge");
        }

        if (challenger.getId().equals(request.getPartnerId())) {
            throw new BadRequestException("You cannot challenge yourself");
        }

        User challengedUser = userRepository.findById(request.getPartnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Target user not found"));

        // 1. Verify accepted partner relationship
        PartnerRelationship relationship = partnerRelationshipRepository
                .findActiveRelationshipBetweenUsers(challenger.getId(), challengedUser.getId())
                .orElseThrow(() -> new AccessDeniedException("You can only challenge accepted learning partners"));

        if (relationship.getStatus() != PartnerRelationshipStatus.ACCEPTED) {
            throw new AccessDeniedException("You can only challenge accepted learning partners");
        }

        // 2. Prevent duplicate active challenges
        List<VocabularyChallenge> activeChallenges = challengeRepository.findActiveBetween(
                challenger.getId(), challengedUser.getId(),
                List.of(ChallengeStatus.PENDING, ChallengeStatus.ACCEPTED, ChallengeStatus.IN_PROGRESS)
        );
        if (!activeChallenges.isEmpty()) {
            throw new BadRequestException("An active challenge already exists between you and this partner");
        }

        // 3. Resolve CEFR level
        DifficultyLevel level = parseDifficultyLevel(request.getCefrLevel(), challenger);

        // 4. Resolve question count (default 10, min 3, max 20)
        int requestedCount = request.getQuestionCount() != null ? request.getQuestionCount() : 10;
        int questionCount = Math.max(3, Math.min(20, requestedCount));

        // 5. Select vocabulary words
        List<VocabularyWord> words = vocabularyWordRepository.findByDifficultyLevel(level);
        if (words.size() < questionCount) {
            List<VocabularyWord> allWords = vocabularyWordRepository.findAll();
            if (allWords.size() > words.size()) {
                words = allWords;
            }
        }
        if (words.isEmpty()) {
            throw new ResourceNotFoundException("No vocabulary words available to generate challenge");
        }

        List<VocabularyWord> candidateWords = new ArrayList<>(words);
        Collections.shuffle(candidateWords);
        int actualCount = Math.min(questionCount, candidateWords.size());
        List<VocabularyWord> selectedWords = candidateWords.subList(0, actualCount);

        // 6. Generate deterministic Quiz container using existing QuestionFactory
        String title = String.format("Challenge: %s vs %s", challenger.getName(), challengedUser.getName());
        Quiz quiz = new Quiz(title, level, actualCount);
        List<QuestionType> types = List.of(QuestionType.MULTIPLE_CHOICE, QuestionType.FILL_IN_THE_BLANK, QuestionType.TRANSLATION);

        for (int i = 0; i < selectedWords.size(); i++) {
            VocabularyWord word = selectedWords.get(i);
            QuestionType type = types.get(i % types.size());
            Question question = questionFactory.createQuestion(type, word, candidateWords);
            quiz.addQuestion(question);
        }

        Quiz savedQuiz = quizRepository.save(quiz);

        // 7. Save challenge
        VocabularyChallenge challenge = new VocabularyChallenge(
                relationship, challenger, challengedUser, level, actualCount, savedQuiz
        );
        VocabularyChallenge saved = challengeRepository.save(challenge);

        if (partnerActivityService != null) {
            partnerActivityService.logActivity(
                    relationship,
                    challenger,
                    PartnerActivityType.CHALLENGE_CREATED,
                    challenger.getName() + " challenged " + challengedUser.getName() + " to a vocabulary duel",
                    level.name() + " Level • " + actualCount + " Questions",
                    0,
                    saved.getId()
            );
        }

        return mapToChallengeResponse(saved, challenger.getId());
    }

    @Override
    public List<ChallengeResponse> getMyChallenges(String currentUserEmail, ChallengeStatus status) {
        User user = findUserByEmail(currentUserEmail);
        List<VocabularyChallenge> challenges = (status == null)
                ? challengeRepository.findAllForUser(user.getId())
                : challengeRepository.findForUserByStatus(user.getId(), status);

        return challenges.stream()
                .map(c -> mapToChallengeResponse(c, user.getId()))
                .toList();
    }

    @Override
    public ChallengeResponse getChallenge(String currentUserEmail, Long challengeId) {
        User user = findUserByEmail(currentUserEmail);
        VocabularyChallenge challenge = findChallengeById(challengeId);

        if (!isParticipant(challenge, user.getId())) {
            throw new AccessDeniedException("You are not authorized to view this challenge");
        }

        return mapToChallengeResponse(challenge, user.getId());
    }

    @Override
    @Transactional
    public ChallengeResponse acceptChallenge(String currentUserEmail, Long challengeId) {
        User user = findUserByEmail(currentUserEmail);
        VocabularyChallenge challenge = findChallengeById(challengeId);

        if (!challenge.getChallengedUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the challenged partner can accept this challenge");
        }

        if (challenge.getStatus() != ChallengeStatus.PENDING) {
            throw new BadRequestException("Challenge cannot be accepted in state: " + challenge.getStatus());
        }

        // Verify partner relationship is still active
        if (challenge.getRelationship().getStatus() != PartnerRelationshipStatus.ACCEPTED) {
            throw new AccessDeniedException("Underlying partner relationship is no longer active");
        }

        challenge.setStatus(ChallengeStatus.ACCEPTED);
        VocabularyChallenge updated = challengeRepository.save(challenge);

        if (partnerActivityService != null) {
            partnerActivityService.logActivity(
                    challenge.getRelationship(),
                    user,
                    PartnerActivityType.CHALLENGE_ACCEPTED,
                    user.getName() + " accepted " + challenge.getChallenger().getName() + "'s vocabulary duel",
                    "Duel is now active",
                    0,
                    challenge.getId()
            );
        }

        return mapToChallengeResponse(updated, user.getId());
    }

    @Override
    @Transactional
    public ChallengeResponse declineChallenge(String currentUserEmail, Long challengeId) {
        User user = findUserByEmail(currentUserEmail);
        VocabularyChallenge challenge = findChallengeById(challengeId);

        if (!challenge.getChallengedUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the challenged partner can decline this challenge");
        }

        if (challenge.getStatus() != ChallengeStatus.PENDING) {
            throw new BadRequestException("Challenge cannot be declined in state: " + challenge.getStatus());
        }

        challenge.setStatus(ChallengeStatus.DECLINED);
        VocabularyChallenge updated = challengeRepository.save(challenge);
        return mapToChallengeResponse(updated, user.getId());
    }

    @Override
    @Transactional
    public ChallengeResponse cancelChallenge(String currentUserEmail, Long challengeId) {
        User user = findUserByEmail(currentUserEmail);
        VocabularyChallenge challenge = findChallengeById(challengeId);

        if (!challenge.getChallenger().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the challenger can cancel this challenge");
        }

        if (challenge.getStatus() != ChallengeStatus.PENDING) {
            throw new BadRequestException("Only pending challenges can be cancelled");
        }

        challenge.setStatus(ChallengeStatus.CANCELLED);
        VocabularyChallenge updated = challengeRepository.save(challenge);
        return mapToChallengeResponse(updated, user.getId());
    }

    @Override
    public List<ChallengeQuestionResponse> getChallengeQuestions(String currentUserEmail, Long challengeId) {
        User user = findUserByEmail(currentUserEmail);
        VocabularyChallenge challenge = findChallengeById(challengeId);

        if (!isParticipant(challenge, user.getId())) {
            throw new AccessDeniedException("You are not authorized to view questions for this challenge");
        }

        if (challenge.getStatus() != ChallengeStatus.ACCEPTED &&
            challenge.getStatus() != ChallengeStatus.IN_PROGRESS &&
            challenge.getStatus() != ChallengeStatus.COMPLETED) {
            throw new BadRequestException("Questions are only available for accepted or active challenges");
        }

        Quiz quiz = challenge.getQuiz();
        if (quiz == null || quiz.getQuestions() == null) {
            return Collections.emptyList();
        }

        return quiz.getQuestions().stream()
                .map(this::mapToChallengeQuestionResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChallengeResultResponse submitChallenge(String currentUserEmail, Long challengeId, SubmitChallengeRequest request) {
        User user = findUserByEmail(currentUserEmail);
        VocabularyChallenge challenge = findChallengeById(challengeId);

        if (!isParticipant(challenge, user.getId())) {
            throw new AccessDeniedException("You are not authorized to submit to this challenge");
        }

        if (challenge.getStatus() != ChallengeStatus.ACCEPTED && challenge.getStatus() != ChallengeStatus.IN_PROGRESS) {
            throw new BadRequestException("Challenge is not in an active submission state (current: " + challenge.getStatus() + ")");
        }

        // Idempotency guard: prevent duplicate submission
        Optional<ChallengeAttempt> existingAttempt = challengeAttemptRepository.findByChallengeIdAndUserId(challenge.getId(), user.getId());
        if (existingAttempt.isPresent() && existingAttempt.get().getCompletedAt() != null) {
            throw new BadRequestException("You have already submitted your answers for this challenge");
        }

        Quiz quiz = challenge.getQuiz();
        List<Question> questions = (quiz != null && quiz.getQuestions() != null) ? quiz.getQuestions() : Collections.emptyList();
        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        ChallengeAttempt attempt = existingAttempt.orElseGet(() ->
                new ChallengeAttempt(challenge, user, questions.size())
        );

        List<ChallengeAnswerRequest> answers = (request != null && request.getAnswers() != null)
                ? request.getAnswers() : Collections.emptyList();

        List<ChallengeQuestionResultDto> breakdown = new ArrayList<>();
        int totalScore = 0;
        int correctCount = 0;

        for (ChallengeAnswerRequest answerReq : answers) {
            Question question = questionMap.get(answerReq.getQuestionId());
            if (question == null) continue;

            QuestionEvaluatorStrategy evaluator = evaluatorFactory.getEvaluator(question.getQuestionType());
            EvaluationResult evaluation = evaluator.evaluate(question, answerReq.getAnswer());

            boolean isCorrect = evaluation.isCorrect();
            int points = evaluation.getScore();
            if (isCorrect) {
                correctCount++;
                totalScore += points;
            }

            long latency = answerReq.getResponseTimeMs() != null ? Math.max(0L, answerReq.getResponseTimeMs()) : 0L;
            ChallengeQuestionAttempt cqa = new ChallengeQuestionAttempt(
                    attempt, question, answerReq.getAnswer(), isCorrect, points, latency
            );
            attempt.addQuestionAttempt(cqa);

            String expectedAnswer = getQuestionCorrectAnswer(question);
            breakdown.add(new ChallengeQuestionResultDto(
                    question.getId(),
                    question.getQuestionText(),
                    answerReq.getAnswer(),
                    expectedAnswer,
                    isCorrect,
                    points
            ));
        }

        attempt.setScore(totalScore);
        attempt.setCorrectAnswers(correctCount);
        attempt.setCompletedAt(Instant.now());
        challengeAttemptRepository.save(attempt);

        // Update challenge state
        boolean isChallenger = challenge.getChallenger().getId().equals(user.getId());
        if (isChallenger) {
            challenge.setChallengerScore(totalScore);
            challenge.setChallengerCompletedAt(Instant.now());
        } else {
            challenge.setChallengedScore(totalScore);
            challenge.setChallengedCompletedAt(Instant.now());
        }

        boolean bothCompleted = (challenge.getChallengerCompletedAt() != null && challenge.getChallengedCompletedAt() != null);
        if (bothCompleted) {
            challenge.setStatus(ChallengeStatus.COMPLETED);
            int scoreA = challenge.getChallengerScore() != null ? challenge.getChallengerScore() : 0;
            int scoreB = challenge.getChallengedScore() != null ? challenge.getChallengedScore() : 0;
            if (scoreA > scoreB) {
                challenge.setWinner(challenge.getChallenger());
            } else if (scoreB > scoreA) {
                challenge.setWinner(challenge.getChallengedUser());
            } else {
                challenge.setWinner(null); // Draw!
            }
        } else {
            challenge.setStatus(ChallengeStatus.IN_PROGRESS);
        }

        challengeRepository.save(challenge);

        // Gamification XP award (idempotent, awarded once upon submission)
        int xpEarned = 0;
        if (gamificationService != null) {
            try {
                boolean isPerfect = correctCount == questions.size() && questions.size() > 0;
                RewardContext rewardContext = RewardContext.forQuizCompletion(
                        challenge.getId(), correctCount, questions.size(), isPerfect
                );
                GamificationActivityResultResponse gamResult = gamificationService.recordActivity(user, RewardActivityType.QUIZ, rewardContext);
                if (gamResult != null) {
                    xpEarned = gamResult.getXpEarned();
                }
            } catch (Exception e) {
                log.warn("Gamification tracking failed for challenge submission: {}", e.getMessage());
            }
        }

        // Partner Activity feed logging (idempotent)
        if (partnerActivityService != null) {
            partnerActivityService.logActivity(
                    challenge.getRelationship(),
                    user,
                    PartnerActivityType.CHALLENGE_COMPLETED,
                    user.getName() + " completed their challenge attempt",
                    correctCount + "/" + questions.size() + " correct (" + totalScore + " pts)",
                    xpEarned,
                    challenge.getId()
            );

            if (bothCompleted) {
                if (challenge.isDraw() || challenge.getWinner() == null) {
                    partnerActivityService.logActivity(
                            challenge.getRelationship(),
                            challenge.getChallenger(),
                            PartnerActivityType.CHALLENGE_DRAW,
                            challenge.getChallenger().getName() + " and " + challenge.getChallengedUser().getName() + " drew their vocabulary duel",
                            "Both scored " + challenge.getChallengerScore() + " pts",
                            0,
                            challenge.getId()
                    );
                } else {
                    User winner = challenge.getWinner();
                    partnerActivityService.logActivity(
                            challenge.getRelationship(),
                            winner,
                            PartnerActivityType.CHALLENGE_WON,
                            winner.getName() + " won a vocabulary duel",
                            "+10 XP challenge bonus earned",
                            10,
                            challenge.getId()
                    );
                }
            }
        }

        return buildChallengeResultResponse(challenge, user.getId(), attempt, breakdown, xpEarned);
    }

    @Override
    public ChallengeResultResponse getChallengeResult(String currentUserEmail, Long challengeId) {
        User user = findUserByEmail(currentUserEmail);
        VocabularyChallenge challenge = findChallengeById(challengeId);

        if (!isParticipant(challenge, user.getId())) {
            throw new AccessDeniedException("You are not authorized to view results for this challenge");
        }

        ChallengeAttempt myAttempt = challengeAttemptRepository.findByChallengeIdAndUserId(challenge.getId(), user.getId())
                .orElse(null);

        List<ChallengeQuestionResultDto> breakdown = new ArrayList<>();
        if (myAttempt != null && myAttempt.getQuestionAttempts() != null) {
            for (ChallengeQuestionAttempt cqa : myAttempt.getQuestionAttempts()) {
                Question q = cqa.getQuestion();
                breakdown.add(new ChallengeQuestionResultDto(
                        q.getId(),
                        q.getQuestionText(),
                        cqa.getUserAnswer(),
                        getQuestionCorrectAnswer(q),
                        cqa.isCorrect(),
                        cqa.getScore()
                ));
            }
        }

        return buildChallengeResultResponse(challenge, user.getId(), myAttempt, breakdown, 0);
    }

    private ChallengeResultResponse buildChallengeResultResponse(VocabularyChallenge challenge,
                                                                Long currentUserId,
                                                                ChallengeAttempt myAttempt,
                                                                List<ChallengeQuestionResultDto> breakdown,
                                                                int xpEarned) {
        boolean bothCompleted = challenge.getStatus() == ChallengeStatus.COMPLETED;
        boolean isChallenger = challenge.getChallenger().getId().equals(currentUserId);

        Integer myScore = myAttempt != null ? myAttempt.getScore() : (isChallenger ? challenge.getChallengerScore() : challenge.getChallengedScore());
        Integer myCorrectCount = myAttempt != null ? myAttempt.getCorrectAnswers() : null;
        Integer totalQuestions = challenge.getQuestionCount();

        // Anti-cheating: partner details are strictly hidden until completion
        Integer partnerScore = null;
        Integer partnerCorrectCount = null;
        Long winnerId = null;
        String winnerName = null;
        boolean isDraw = false;

        if (bothCompleted) {
            partnerScore = isChallenger ? challenge.getChallengedScore() : challenge.getChallengerScore();
            Long partnerId = isChallenger ? challenge.getChallengedUser().getId() : challenge.getChallenger().getId();
            partnerCorrectCount = challengeAttemptRepository.findByChallengeIdAndUserId(challenge.getId(), partnerId)
                    .map(ChallengeAttempt::getCorrectAnswers)
                    .orElse(null);

            if (challenge.getWinner() != null) {
                winnerId = challenge.getWinner().getId();
                winnerName = challenge.getWinner().getName();
            } else {
                isDraw = true;
            }
        }

        boolean waitingForPartner = !bothCompleted && (myAttempt != null && myAttempt.getCompletedAt() != null);

        return new ChallengeResultResponse(
                challenge.getId(),
                challenge.getStatus().name(),
                bothCompleted,
                waitingForPartner,
                myScore,
                myCorrectCount,
                totalQuestions,
                partnerScore,
                partnerCorrectCount,
                winnerId,
                winnerName,
                isDraw,
                xpEarned,
                breakdown
        );
    }

    private ChallengeResponse mapToChallengeResponse(VocabularyChallenge challenge, Long currentUserId) {
        boolean isChallenger = challenge.getChallenger().getId().equals(currentUserId);
        boolean currentUserCompleted = isChallenger ? challenge.getChallengerCompletedAt() != null : challenge.getChallengedCompletedAt() != null;
        boolean partnerCompleted = isChallenger ? challenge.getChallengedCompletedAt() != null : challenge.getChallengerCompletedAt() != null;

        Integer myScore = isChallenger ? challenge.getChallengerScore() : challenge.getChallengedScore();
        Instant myCompletedAt = isChallenger ? challenge.getChallengerCompletedAt() : challenge.getChallengedCompletedAt();
        Instant partnerCompletedAt = isChallenger ? challenge.getChallengedCompletedAt() : challenge.getChallengerCompletedAt();

        // Anti-cheating suppression: partner score & winner only visible once COMPLETED
        Integer partnerScore = null;
        Long winnerId = null;
        String winnerName = null;
        boolean isDraw = false;

        if (challenge.getStatus() == ChallengeStatus.COMPLETED) {
            partnerScore = isChallenger ? challenge.getChallengedScore() : challenge.getChallengerScore();
            if (challenge.getWinner() != null) {
                winnerId = challenge.getWinner().getId();
                winnerName = challenge.getWinner().getName();
            } else {
                isDraw = true;
            }
        }

        return new ChallengeResponse(
                challenge.getId(),
                challenge.getRelationship().getId(),
                challenge.getChallenger().getId(),
                challenge.getChallenger().getName(),
                challenge.getChallengedUser().getId(),
                challenge.getChallengedUser().getName(),
                challenge.getStatus().name(),
                challenge.getCefrLevel().name(),
                challenge.getQuestionCount(),
                currentUserCompleted,
                partnerCompleted,
                myScore,
                partnerScore,
                myCompletedAt,
                partnerCompletedAt,
                winnerId,
                winnerName,
                isDraw,
                challenge.getCreatedAt()
        );
    }

    private ChallengeQuestionResponse mapToChallengeQuestionResponse(Question question) {
        List<String> options = null;
        String sentence = null;

        if (question instanceof MultipleChoiceQuestion mcq) {
            options = mcq.getOptions() != null ? new ArrayList<>(mcq.getOptions()) : Collections.emptyList();
        } else if (question instanceof FillInTheBlankQuestion fitb) {
            sentence = fitb.getSentence();
        }

        return new ChallengeQuestionResponse(
                question.getId(),
                question.getVocabularyWord().getId(),
                question.getVocabularyWord().getWord(),
                question.getQuestionType().name(),
                question.getPoints(),
                question.getQuestionText(),
                options,
                sentence
        );
    }

    private String getQuestionCorrectAnswer(Question question) {
        if (question instanceof MultipleChoiceQuestion mcq) {
            return mcq.getCorrectOption();
        } else if (question instanceof FillInTheBlankQuestion fitb) {
            return fitb.getExpectedAnswer();
        } else if (question instanceof TranslationQuestion tq) {
            return tq.getExpectedAnswer();
        }
        return "";
    }

    private boolean isParticipant(VocabularyChallenge challenge, Long userId) {
        return challenge.getChallenger().getId().equals(userId) || challenge.getChallengedUser().getId().equals(userId);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private VocabularyChallenge findChallengeById(Long challengeId) {
        return challengeRepository.findByIdWithDetails(challengeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary challenge not found with ID: " + challengeId));
    }

    private DifficultyLevel parseDifficultyLevel(String requestedLevel, User challenger) {
        if (requestedLevel != null && !requestedLevel.trim().isEmpty()) {
            try {
                return DifficultyLevel.valueOf(requestedLevel.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (challenger.getCurrentLevel() != null) {
            try {
                return DifficultyLevel.valueOf(challenger.getCurrentLevel().name());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return DifficultyLevel.A1;
    }
}
