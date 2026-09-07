package com.memora.modules.ai;

import com.memora.modules.ai.dto.AdaptiveInsightResponse;
import com.memora.modules.ai.service.AdaptiveInsightService;
import com.memora.modules.learningpath.repository.LearningPathRepository;
import com.memora.modules.memory.dto.MemoryWordResponse;
import com.memora.modules.memory.service.MemoryService;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdaptiveInsightServiceTest {

    @Mock
    private MemoryService memoryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserWordProgressRepository progressRepository;

    @Mock
    private LearningPathRepository learningPathRepository;

    @InjectMocks
    private AdaptiveInsightService insightService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setEmail("test@example.com");
        sampleUser.setStreak(5);
        sampleUser.setXp(450);
        sampleUser.setCurrentLevel(VocabularyLevel.B1);
    }

    @Test
    @DisplayName("Should prioritize due reviews when spaced repetition items are pending")
    void testDueReviewsPriority() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        MemoryWordResponse dueWord = new MemoryWordResponse(
                10L, "rapid", "fast", DifficultyLevel.A2, WordCategory.GENERAL, 75.0, ForgettingRisk.LOW,
                Instant.now().minusSeconds(3600), Instant.now(), 3, 3, 0, 3, 1500.0, 2
        );
        when(memoryService.getDueReviews("test@example.com")).thenReturn(List.of(dueWord));
        when(memoryService.getWeakWords("test@example.com")).thenReturn(Collections.emptyList());
        when(progressRepository.findByUser(sampleUser)).thenReturn(Collections.emptyList());
        when(learningPathRepository.findActivePathByUserId(1L)).thenReturn(Optional.empty());

        AdaptiveInsightResponse response = insightService.getTodayInsights("test@example.com");

        assertNotNull(response);
        assertNotNull(response.primaryInsight());
        assertEquals("REVIEW_PRIORITY", response.primaryInsight().type());
        assertEquals(1, response.metrics().dueReviewsCount());
        assertEquals(5, response.metrics().currentStreak());
        assertEquals(450, response.metrics().totalXp());
    }

    @Test
    @DisplayName("Should prioritize weak words when forgetting risk is elevated")
    void testWeakWordsReinforcement() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));

        MemoryWordResponse weakWord = new MemoryWordResponse(
                12L, "elaborate", "complex", DifficultyLevel.B2, WordCategory.ACADEMIC, 30.0, ForgettingRisk.HIGH,
                Instant.now(), Instant.now(), 4, 1, 3, 0, 3200.0, 1
        );
        when(memoryService.getDueReviews("test@example.com")).thenReturn(Collections.emptyList());
        when(memoryService.getWeakWords("test@example.com")).thenReturn(List.of(weakWord));
        when(progressRepository.findByUser(sampleUser)).thenReturn(Collections.emptyList());
        when(learningPathRepository.findActivePathByUserId(1L)).thenReturn(Optional.empty());

        AdaptiveInsightResponse response = insightService.getTodayInsights("test@example.com");

        assertNotNull(response);
        assertNotNull(response.primaryInsight());
        assertEquals("REINFORCEMENT", response.primaryInsight().type());
        assertEquals(1, response.metrics().weakWordsCount());
    }

    @Test
    @DisplayName("Should trigger challenge ready when retention rate is high with zero weak words")
    void testChallengeReady() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(memoryService.getDueReviews("test@example.com")).thenReturn(Collections.emptyList());
        when(memoryService.getWeakWords("test@example.com")).thenReturn(Collections.emptyList());
        when(learningPathRepository.findActivePathByUserId(1L)).thenReturn(Optional.empty());

        // 5 words with 100% correct attempts
        List<UserWordProgress> progress = List.of(
                createProgress(10, 10, 1200),
                createProgress(10, 10, 1400),
                createProgress(8, 8, 1100),
                createProgress(6, 6, 1300),
                createProgress(5, 5, 1200)
        );
        when(progressRepository.findByUser(sampleUser)).thenReturn(progress);

        AdaptiveInsightResponse response = insightService.getTodayInsights("test@example.com");

        assertNotNull(response);
        assertEquals("CHALLENGE_READY", response.primaryInsight().type());
        assertEquals(100.0, response.metrics().retentionRate());
    }

    private UserWordProgress createProgress(int total, int correct, double avgTime) {
        UserWordProgress p = new UserWordProgress();
        p.setTotalAttempts(total);
        p.setCorrectAttempts(correct);
        p.setAverageResponseTime(avgTime);
        return p;
    }
}
