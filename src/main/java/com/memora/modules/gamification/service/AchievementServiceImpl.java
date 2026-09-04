package com.memora.modules.gamification.service;

import com.memora.modules.gamification.domain.AchievementEvaluationContext;
import com.memora.modules.gamification.entity.Achievement;
import com.memora.modules.gamification.entity.UserAchievement;
import com.memora.modules.gamification.repository.AchievementRepository;
import com.memora.modules.gamification.repository.UserAchievementRepository;
import com.memora.modules.gamification.rule.AchievementRuleEngine;
import com.memora.modules.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Core implementation of {@link AchievementService}.
 */
@Service
@Transactional
public class AchievementServiceImpl implements AchievementService {

    private static final Logger log = LoggerFactory.getLogger(AchievementServiceImpl.class);

    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final AchievementRuleEngine ruleEngine;

    public AchievementServiceImpl(AchievementRepository achievementRepository,
                                  UserAchievementRepository userAchievementRepository,
                                  AchievementRuleEngine ruleEngine) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
        this.ruleEngine = ruleEngine;
    }

    @Override
    public List<Achievement> evaluateAndAwardAchievements(AchievementEvaluationContext context) {
        if (context == null || context.getUser() == null) return Collections.emptyList();

        User user = context.getUser();
        List<String> eligibleCodes = ruleEngine.evaluateEligibleAchievements(context);
        List<Achievement> newlyAwarded = new ArrayList<>();

        for (String code : eligibleCodes) {
            // Check idempotency: learner must never receive the same achievement twice
            boolean alreadyEarned = userAchievementRepository.existsByUserIdAndAchievementCode(user.getId(), code);
            if (alreadyEarned) {
                continue;
            }

            Optional<Achievement> achievementOpt = achievementRepository.findByCode(code);
            if (achievementOpt.isPresent()) {
                Achievement achievement = achievementOpt.get();
                UserAchievement userAchievement = new UserAchievement(user, achievement);
                userAchievementRepository.save(userAchievement);
                newlyAwarded.add(achievement);
                log.info("Awarded achievement [{}] to user [{}]", code, user.getEmail());
            } else {
                log.warn("Achievement with code [{}] matched rule but was not found in catalog", code);
            }
        }

        return newlyAwarded;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserAchievement> getLearnerAchievements(User user) {
        if (user == null) return Collections.emptyList();
        return userAchievementRepository.findByUserIdOrderByEarnedAtDesc(user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Achievement> getAllActiveAchievements() {
        return achievementRepository.findByActiveTrue();
    }
}
