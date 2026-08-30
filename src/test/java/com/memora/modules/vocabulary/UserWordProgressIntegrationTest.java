package com.memora.modules.vocabulary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.ForgettingRisk;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.dto.VocabularyWordRequest;
import com.memora.modules.vocabulary.dto.VocabularyWordResponse;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import com.memora.modules.vocabulary.service.VocabularyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserWordProgressIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private UserWordProgressRepository userWordProgressRepository;

    @Autowired
    private VocabularyService vocabularyService;

    private User userAlice;
    private User userBob;
    private VocabularyWord wordApple;
    private VocabularyWord wordBanana;

    @BeforeEach
    void setUp() {
        userWordProgressRepository.deleteAll();
        userRepository.deleteAll();
        vocabularyWordRepository.deleteAll();

        userAlice = userRepository.save(new User("Alice", "alice@memora.com", "pwHash", VocabularyLevel.A1, Role.LEARNER));
        userBob = userRepository.save(new User("Bob", "bob@memora.com", "pwHash", VocabularyLevel.A1, Role.LEARNER));

        VocabularyWordResponse r1 = vocabularyService.createWord(new VocabularyWordRequest("apple", "fruit", null, null, null, DifficultyLevel.A1, WordCategory.DAILY_LIFE));
        VocabularyWordResponse r2 = vocabularyService.createWord(new VocabularyWordRequest("banana", "yellow fruit", null, null, null, DifficultyLevel.A1, WordCategory.DAILY_LIFE));

        wordApple = vocabularyService.getEntityById(r1.getId());
        wordBanana = vocabularyService.getEntityById(r2.getId());

        // Alice progress: apple
        UserWordProgress pAlice = new UserWordProgress(userAlice, wordApple);
        pAlice.setTotalAttempts(5);
        pAlice.setCorrectAttempts(4);
        pAlice.setIncorrectAttempts(1);
        userWordProgressRepository.save(pAlice);

        // Bob progress: banana
        UserWordProgress pBob = new UserWordProgress(userBob, wordBanana);
        pBob.setTotalAttempts(10);
        pBob.setCorrectAttempts(10);
        pBob.setIncorrectAttempts(0);
        userWordProgressRepository.save(pBob);
    }

    @Test
    @DisplayName("GET /api/v1/progress/words without authentication should return 401 Unauthorized")
    void testProgressWithoutAuth() throws Exception {
        mockMvc.perform(get("/api/v1/progress/words"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @WithMockUser(username = "alice@memora.com", roles = {"LEARNER"})
    @DisplayName("GET /api/v1/progress/words for Alice should return ONLY Alice's progress (User Isolation)")
    void testAliceProgressIsolation() throws Exception {
        mockMvc.perform(get("/api/v1/progress/words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].word", is("apple")))
                .andExpect(jsonPath("$.data[0].totalAttempts", is(5)))
                .andExpect(jsonPath("$.data[0].accuracy", is(80.0)));
    }

    @Test
    @WithMockUser(username = "bob@memora.com", roles = {"LEARNER"})
    @DisplayName("GET /api/v1/progress/words for Bob should return ONLY Bob's progress")
    void testBobProgressIsolation() throws Exception {
        mockMvc.perform(get("/api/v1/progress/words"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].word", is("banana")))
                .andExpect(jsonPath("$.data[0].totalAttempts", is(10)))
                .andExpect(jsonPath("$.data[0].accuracy", is(100.0)));
    }

    @Test
    @WithMockUser(username = "alice@memora.com", roles = {"LEARNER"})
    @DisplayName("POST /api/v1/progress/words/{wordId}/init should initialize progress for user")
    void testInitializeWordProgress() throws Exception {
        mockMvc.perform(post("/api/v1/progress/words/" + wordBanana.getId() + "/init"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.word", is("banana")))
                .andExpect(jsonPath("$.data.totalAttempts", is(0)));
    }
}
