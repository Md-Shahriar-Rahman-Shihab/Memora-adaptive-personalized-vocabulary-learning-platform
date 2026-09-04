package com.memora.modules.gamification.service;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.gamification.domain.*;
import com.memora.modules.gamification.dto.*;
import com.memora.modules.gamification.entity.*;
import com.memora.modules.gamification.factory.RewardStrategyFactory;
import com.memora.modules.gamification.repository.UserGamificationProfileRepository;
import com.memora.modules.gamification.repository.XpTransactionRepository;
import com.memora.modules.gamification.strategy.RewardStrategy;
import com.memora.modules.quiz.entity.QuizAttempt;
import com.memora.modules.quiz.repository.QuizAttemptRepository;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Core implementation of {@link GamificationService}.
 * Orchestrates XP awards, calendar streak tracking, achievement evaluations,
 * profile statistics aggregation, and leaderboard ranking.
 */
@Service
@Transactional
public class GamificationServiceImpl implements GamificationService {

    private static final Logger log = LoggerFactory.getLogger(GamificationServiceImpl.class);

    private final UserGamificationProfileRepository profileRepository;
    private final XpTransactionRepository xpTransactionRepository;
    private final UserRepository userRepository;
    private final UserWordProgressRepository userWordProgressRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final RewardStrategyFactory strategyFactory;
    private final StreakService streakService;
    private final AchievementService achievementService;

    public GamificationServiceImpl(UserGamificationProfileRepository profileRepository,
                                   XpTransactionRepository xpTransactionRepository,
                                   UserRepository userRepository,
                                   UserWordProgressRepository userWordProgressRepository,
                                   QuizAttemptRepository quizAttemptRepository,
                                   RewardStrategyFactory strategyFactory,
                                   StreakService streakService,
                                   AchievementService achievementService) {
        this.profileRepository = profileRepository;
        this.xpTransactionRepository = xpTransactionRepository;
        this.userRepository = userRepository;
        this.userWordProgressRepository = userWordProgressRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.strategyFactory = strategyFactory;
        this.streakService = streakService;
        this.achievementService = achievementService;
    }

    @Override
    public GamificationActivityResultResponse recordActivity(User user, RewardActivityType activityType, RewardContext context) {
        if (user == null) return null;

        // 1. Advance daily learning streak
        UserGamificationProfile profile = streakService.recordActivityStreak(user);

        // 2. Calculate activity XP reward
        RewardStrategy strategy = strategyFactory.getStrategy(activityType);
        int earnedXp = strategy != null ? strategy.calculateReward(context) : 0;

        if (earnedXp > 0) {
            profile.addXp(earnedXp);
            user.setXp(profile.getTotalXp());
            userRepository.save(user);
            profileRepository.save(profile);

            String description = buildXpDescription(activityType, context);
            String refId = context != null ? context.getReferenceId() : null;
            xpTransactionRepository.save(new XpTransaction(user, earnedXp, activityType, description, refId, profile.getTotalXp()));
        }

        // 3. Check for streak milestone bonus (e.g. 7-day or 30-day streak)
        if (profile.getCurrentStreak() == 7 || profile.getCurrentStreak() == 30) {
            RewardStrategy streakBonusStrategy = strategyFactory.getStrategy(RewardActivityType.STREAK_BONUS);
            if (streakBonusStrategy != null) {
                int streakBonus = streakBonusStrategy.calculateReward(RewardContext.forStreak(profile.getCurrentStreak()));
                if (streakBonus > 0) {
                    profile.addXp(streakBonus);
                    user.setXp(profile.getTotalXp());
                    userRepository.save(user);
                    profileRepository.save(profile);

                    String streakDesc = String.format("+%d XP — %d-day learning streak milestone bonus!", streakBonus, profile.getCurrentStreak());
                    xpTransactionRepository.save(new XpTransaction(user, streakBonus, RewardActivityType.STREAK_BONUS,
                            streakDesc, "streak-" + profile.getCurrentStreak(), profile.getTotalXp()));
                    earnedXp += streakBonus;
                }
            }
        }

        // 4. Gather metrics and evaluate achievements
        List<UserWordProgress> progressList = userWordProgressRepository.findByUser(user);
        int wordsLearned = progressList.size();
        int masteredWords = (int) progressList.stream()
                .filter(p -> p.getMasteryScore() >= 80.0 || p.getLeitnerBox() >= 4)
                .count();

        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByUserIdOrderByStartedAtDesc(user.getId());
        int quizzesCompleted = (int) quizAttempts.stream().filter(q -> q.getCompletedAt() != null).count();
        int lessonsCompleted = (int) xpTransactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .filter(t -> t.getActivityType() == RewardActivityType.LESSON)
                .count();

        boolean isPerfect = context != null && Boolean.TRUE.equals(context.getIsPerfect());

        AchievementEvaluationContext evalContext = new AchievementEvaluationContext(
                user,
                profile,
                wordsLearned,
                masteredWords,
                quizzesCompleted,
                lessonsCompleted,
                isPerfect,
                profile.getCurrentStreak(),
                activityType
        );

        List<Achievement> newlyAwarded = achievementService.evaluateAndAwardAchievements(evalContext);
        List<UserAchievementResponse> newAchievementDtos = newlyAwarded.stream()
                .map(a -> new UserAchievementResponse(a.getCode(), a.getName(), a.getDescription(), a.getIcon(), null))
                .toList();

        String message = String.format("Activity recorded: +%d XP earned, %d-day streak", earnedXp, profile.getCurrentStreak());
        return new GamificationActivityResultResponse(
                earnedXp,
                profile.getTotalXp(),
                profile.getCurrentStreak(),
                profile.getLongestStreak(),
                newAchievementDtos,
                message
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LearnerProfileResponse getProfile(String userEmail) {
        User user = findUserByEmail(userEmail);
        UserGamificationProfile profile = profileRepository.findByUserId(user.getId())
                .orElseGet(() -> new UserGamificationProfile(user));

        List<UserWordProgress> progressList = userWordProgressRepository.findByUser(user);
        int wordsLearned = progressList.size();
        int masteredWords = (int) progressList.stream()
                .filter(p -> p.getMasteryScore() >= 80.0 || p.getLeitnerBox() >= 4)
                .count();
        int reviewsCompleted = progressList.stream()
                .mapToInt(p -> p.getCorrectAttempts() + p.getIncorrectAttempts())
                .sum();

        List<QuizAttempt> quizAttempts = quizAttemptRepository.findByUserIdOrderByStartedAtDesc(user.getId());
        int quizzesCompleted = (int) quizAttempts.stream().filter(q -> q.getCompletedAt() != null).count();

        int totalCorrect = quizAttempts.stream().mapToInt(QuizAttempt::getCorrectAnswers).sum();
        int totalQuestions = quizAttempts.stream().mapToInt(QuizAttempt::getTotalQuestions).sum();

        double accuracy;
        if (totalQuestions > 0) {
            accuracy = Math.round(((double) totalCorrect / totalQuestions) * 1000.0) / 10.0;
        } else if (!progressList.isEmpty()) {
            double avgMastery = progressList.stream().mapToDouble(UserWordProgress::getMasteryScore).average().orElse(0.0);
            accuracy = Math.round(avgMastery * 10.0) / 10.0;
        } else {
            accuracy = 0.0;
        }

        long rank = profileRepository.calculateRank(profile.getTotalXp(), profile.getCurrentStreak());

        List<UserAchievement> userAchievements = achievementService.getLearnerAchievements(user);
        List<UserAchievementResponse> achievementResponses = userAchievements.stream()
                .map(ua -> new UserAchievementResponse(
                        ua.getAchievement().getCode(),
                        ua.getAchievement().getName(),
                        ua.getAchievement().getDescription(),
                        ua.getAchievement().getIcon(),
                        ua.getEarnedAt()
                ))
                .toList();

        double learningProgress = Math.min(100.0, Math.round((wordsLearned / 100.0) * 1000.0) / 10.0);

        return new LearnerProfileResponse(
                user.getName(),
                user.getEmail(),
                user.getCurrentLevel() != null ? user.getCurrentLevel().name() : "A1",
                profile.getTotalXp(),
                profile.getCurrentStreak(),
                profile.getLongestStreak(),
                wordsLearned,
                masteredWords,
                reviewsCompleted,
                quizzesCompleted,
                totalCorrect,
                totalQuestions,
                accuracy,
                learningProgress,
                rank,
                achievementResponses
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LearnerStatsResponse getStats(String userEmail) {
        LearnerProfileResponse profile = getProfile(userEmail);
        return new LearnerStatsResponse(
                profile.getXp(),
                profile.getCurrentStreak(),
                profile.getLongestStreak(),
                profile.getWordsLearned(),
                profile.getMasteredWords(),
                profile.getQuizzesCompleted(),
                profile.getReviewsCompleted(),
                profile.getAccuracy(),
                profile.getRank()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<XpTransactionResponse> getXpHistory(String userEmail, Pageable pageable) {
        User user = findUserByEmail(userEmail);
        List<XpTransaction> transactions = pageable != null
                ? xpTransactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                : xpTransactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        return transactions.stream()
                .map(t -> new XpTransactionResponse(
                        t.getId(),
                        t.getAmount(),
                        t.getActivityType(),
                        t.getDescription(),
                        t.getReferenceId(),
                        t.getBalanceAfter(),
                        t.getCreatedAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievementResponse> getAchievements(String userEmail) {
        User user = findUserByEmail(userEmail);
        List<UserAchievement> achievements = achievementService.getLearnerAchievements(user);
        return achievements.stream()
                .map(ua -> new UserAchievementResponse(
                        ua.getAchievement().getCode(),
                        ua.getAchievement().getName(),
                        ua.getAchievement().getDescription(),
                        ua.getAchievement().getIcon(),
                        ua.getEarnedAt()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryResponse> getLeaderboard(int limit) {
        int safeLimit = Math.min(Math.max(1, limit), 100);
        Pageable pageable = PageRequest.of(0, safeLimit);
        List<UserGamificationProfile> profiles = profileRepository.findLeaderboard(pageable);

        List<LeaderboardEntryResponse> entries = new ArrayList<>();
        long rank = 1;
        for (UserGamificationProfile p : profiles) {
            User u = p.getUser();
            String level = u.getCurrentLevel() != null ? u.getCurrentLevel().name() : "A1";
            entries.add(new LeaderboardEntryResponse(
                    rank++,
                    u.getName(),
                    p.getTotalXp(),
                    p.getCurrentStreak(),
                    level
            ));
        }

        return entries;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private String buildXpDescription(RewardActivityType type, RewardContext context) {
        if (type == null) return "Activity reward";
        return switch (type) {
            case QUIZ -> context != null && Boolean.TRUE.equals(context.getIsPerfect())
                    ? "Perfect quiz bonus & answers"
                    : "Correct quiz answer";
            case LESSON -> "Vocabulary lesson discovery item completed";
            case REVIEW -> "Vocabulary spaced repetition review completed";
            case DAILY_PATH -> "Daily learning path completed";
            case STREAK_BONUS -> "Learning streak milestone bonus";
            case ASSESSMENT -> "Diagnostic placement assessment completed";
        };
    }
}
