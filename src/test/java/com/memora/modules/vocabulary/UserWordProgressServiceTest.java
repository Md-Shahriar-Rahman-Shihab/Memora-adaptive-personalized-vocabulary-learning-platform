package com.memora.modules.vocabulary;

import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.dto.UserWordProgressResponse;
import com.memora.modules.vocabulary.dto.VocabularyWordRequest;
import com.memora.modules.vocabulary.dto.VocabularyWordResponse;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import com.memora.modules.vocabulary.service.UserWordProgressService;
import com.memora.modules.vocabulary.service.VocabularyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserWordProgressServiceTest {

    @Autowired
    private UserWordProgressService userWordProgressService;

    @Autowired
    private UserWordProgressRepository userWordProgressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private VocabularyService vocabularyService;

    private User testUser;
    private VocabularyWord testWord;

    @BeforeEach
    void setUp() {
        userWordProgressRepository.deleteAll();
        userRepository.deleteAll();
        vocabularyWordRepository.deleteAll();

        testUser = userRepository.save(new User("Learner One", "learner1@memora.com", "hash123", VocabularyLevel.A1, Role.LEARNER));
        VocabularyWordResponse wordResponse = vocabularyService.createWord(new VocabularyWordRequest(
                "resilient", "able to withstand or recover quickly", "Strong and flexible.",
                null, null, DifficultyLevel.B1, WordCategory.GENERAL
        ));
        testWord = vocabularyService.getEntityById(wordResponse.getId());
    }

    @Test
    @DisplayName("Initialize progress should create a default progress record with 0 attempts")
    void testInitializeProgress() {
        UserWordProgressResponse response = userWordProgressService.initializeProgress("learner1@memora.com", testWord.getId());

        assertNotNull(response);
        assertEquals(testWord.getId(), response.getVocabularyWordId());
        assertEquals("resilient", response.getWord());
        assertEquals(0, response.getTotalAttempts());
        assertEquals(0, response.getCorrectAttempts());
        assertEquals(0, response.getIncorrectAttempts());
        assertEquals(0.0, response.getAccuracy());
        assertEquals(ForgettingRisk.LOW, response.getForgettingRisk());
    }

    @Test
    @DisplayName("Retrieve progress for user and calculate accuracy dynamically")
    void testRetrieveProgressWithAccuracy() {
        // Create progress with 10 total attempts, 8 correct
        UserWordProgress progress = new UserWordProgress(testUser, testWord);
        progress.setTotalAttempts(10);
        progress.setCorrectAttempts(8);
        progress.setIncorrectAttempts(2);
        progress.setAverageResponseTime(1450.0);
        userWordProgressRepository.save(progress);

        UserWordProgressResponse response = userWordProgressService.getProgressForWord("learner1@memora.com", testWord.getId());

        assertEquals(10, response.getTotalAttempts());
        assertEquals(8, response.getCorrectAttempts());
        assertEquals(2, response.getIncorrectAttempts());
        assertEquals(80.0, response.getAccuracy());
        assertEquals(1450.0, response.getAverageResponseTime());
    }

    @Test
    @DisplayName("Retrieve weak words based on forgetting risk or high mistake rates")
    void testRetrieveWeakWords() {
        // Word 1: weak word (incorrect > correct)
        UserWordProgress progress1 = new UserWordProgress(testUser, testWord);
        progress1.setTotalAttempts(5);
        progress1.setCorrectAttempts(1);
        progress1.setIncorrectAttempts(4);
        progress1.setForgettingRisk(ForgettingRisk.HIGH);
        userWordProgressRepository.save(progress1);

        // Word 2: strong word
        VocabularyWordResponse word2Resp = vocabularyService.createWord(new VocabularyWordRequest("sunny", "bright with sunlight", null, null, null, DifficultyLevel.A1, WordCategory.DAILY_LIFE));
        VocabularyWord word2 = vocabularyService.getEntityById(word2Resp.getId());
        UserWordProgress progress2 = new UserWordProgress(testUser, word2);
        progress2.setTotalAttempts(10);
        progress2.setCorrectAttempts(10);
        progress2.setIncorrectAttempts(0);
        progress2.setForgettingRisk(ForgettingRisk.LOW);
        userWordProgressRepository.save(progress2);

        List<UserWordProgressResponse> weakWords = userWordProgressService.getWeakWords("learner1@memora.com");
        assertEquals(1, weakWords.size());
        assertEquals("resilient", weakWords.get(0).getWord());
    }

    @Test
    @DisplayName("Retrieve due reviews when nextReviewAt is past or present")
    void testRetrieveDueReviews() {
        // Due word (nextReviewAt 1 hour ago)
        UserWordProgress dueProgress = new UserWordProgress(testUser, testWord);
        dueProgress.setNextReviewAt(Instant.now().minus(1, ChronoUnit.HOURS));
        userWordProgressRepository.save(dueProgress);

        // Future word (nextReviewAt in 2 days)
        VocabularyWordResponse word2Resp = vocabularyService.createWord(new VocabularyWordRequest("ocean", "large body of water", null, null, null, DifficultyLevel.A1, WordCategory.GENERAL));
        VocabularyWord word2 = vocabularyService.getEntityById(word2Resp.getId());
        UserWordProgress futureProgress = new UserWordProgress(testUser, word2);
        futureProgress.setNextReviewAt(Instant.now().plus(2, ChronoUnit.DAYS));
        userWordProgressRepository.save(futureProgress);

        List<UserWordProgressResponse> dueReviews = userWordProgressService.getDueReviews("learner1@memora.com");
        assertEquals(1, dueReviews.size());
        assertEquals("resilient", dueReviews.get(0).getWord());
    }
}
