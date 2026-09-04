package com.memora.modules.gamification.config;

import com.memora.modules.gamification.domain.AchievementCode;
import com.memora.modules.gamification.entity.Achievement;
import com.memora.modules.gamification.repository.AchievementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds standard achievement milestones into the catalog if not already present.
 */
@Component
@Order(3)
public class AchievementDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AchievementDataSeeder.class);

    private final AchievementRepository achievementRepository;

    public AchievementDataSeeder(AchievementRepository achievementRepository) {
        this.achievementRepository = achievementRepository;
    }

    @Override
    public void run(String... args) {
        seedAchievements();
    }

    public void seedAchievements() {
        List<Achievement> standardAchievements = List.of(
                new Achievement(AchievementCode.FIRST_LESSON, "First Steps", "Completed your first vocabulary lesson", "fa-book-open"),
                new Achievement(AchievementCode.FIRST_QUIZ, "Quiz Initiate", "Completed your first vocabulary quiz", "fa-question-circle"),
                new Achievement(AchievementCode.WORD_STARTER, "Word Starter", "Learned 10 vocabulary words", "fa-seedling"),
                new Achievement(AchievementCode.VOCABULARY_EXPLORER, "Vocabulary Explorer", "Learned 50 vocabulary words", "fa-compass"),
                new Achievement(AchievementCode.CENTURY, "Century Club", "Learned 100 vocabulary words", "fa-trophy"),
                new Achievement(AchievementCode.PERFECT_SCORE, "Perfectionist", "Scored 100% on a vocabulary quiz", "fa-star"),
                new Achievement(AchievementCode.QUIZ_MASTER, "Quiz Master", "Completed 10 vocabulary quizzes", "fa-brain"),
                new Achievement(AchievementCode.STREAK_7, "Week of Dedication", "Maintained a 7-day learning streak", "fa-fire"),
                new Achievement(AchievementCode.STREAK_30, "Monthly Marathon", "Maintained a 30-day learning streak", "fa-bolt"),
                new Achievement(AchievementCode.MEMORY_MASTER, "Memory Master", "Mastered 20 vocabulary words in spaced repetition", "fa-crown")
        );

        for (Achievement a : standardAchievements) {
            if (!achievementRepository.existsByCode(a.getCode())) {
                achievementRepository.save(a);
                log.debug("Seeded achievement: {}", a.getCode());
            }
        }
    }
}
