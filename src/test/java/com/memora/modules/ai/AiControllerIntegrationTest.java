package com.memora.modules.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.ai.dto.AiExplanationRequest;
import com.memora.modules.ai.dto.AiExampleRequest;
import com.memora.modules.ai.dto.AiMemoryTipRequest;
import com.memora.modules.ai.dto.AiUsageRequest;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    private static final String TEST_EMAIL = "ai_learner@example.com";
    private VocabularyWord testWord;

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByEmail(TEST_EMAIL)) {
            User user = new User();
            user.setName("AI Learner");
            user.setEmail(TEST_EMAIL);
            user.setPasswordHash("hashedpassword123");
            user.setRole(Role.LEARNER);
            user.setCurrentLevel(VocabularyLevel.B1);
            user.setXp(100);
            user.setStreak(2);
            userRepository.save(user);
        }

        testWord = vocabularyWordRepository.findByWordIgnoreCase("scrutinize")
                .orElseGet(() -> {
                    VocabularyWord word = new VocabularyWord();
                    word.setWord("scrutinize");
                    word.setMeaning("examine or inspect closely and thoroughly");
                    word.setDefinition("to look at in critical detail");
                    word.setCategory(WordCategory.ACADEMIC);
                    word.setDifficultyLevel(DifficultyLevel.B2);
                    word.setExampleSentence("The auditor will scrutinize all financial statements.");
                    return vocabularyWordRepository.save(word);
                });
    }

    @Test
    @DisplayName("Should return 401 Unauthorized when unauthenticated")
    void testUnauthenticatedAccess() throws Exception {
        AiExplanationRequest request = new AiExplanationRequest(testWord.getId(), null, "B2");

        mockMvc.perform(post("/api/v1/ai/word-explanation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"LEARNER"})
    @DisplayName("Should return AI vocabulary explanation")
    void testExplainWordEndpoint() throws Exception {
        AiExplanationRequest request = new AiExplanationRequest(testWord.getId(), null, "B2");

        mockMvc.perform(post("/api/v1/ai/word-explanation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.word", is("scrutinize")))
                .andExpect(jsonPath("$.data.explanation", notNullValue()))
                .andExpect(jsonPath("$.data.provider", notNullValue()));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"LEARNER"})
    @DisplayName("Should return AI example sentence")
    void testExampleEndpoint() throws Exception {
        AiExampleRequest request = new AiExampleRequest(testWord.getId(), null, "B2");

        mockMvc.perform(post("/api/v1/ai/example")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.word", is("scrutinize")))
                .andExpect(jsonPath("$.data.exampleSentence", notNullValue()));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"LEARNER"})
    @DisplayName("Should return AI memory retention tip")
    void testMemoryTipEndpoint() throws Exception {
        AiMemoryTipRequest request = new AiMemoryTipRequest(testWord.getId(), null, "B2");

        mockMvc.perform(post("/api/v1/ai/memory-tip")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.word", is("scrutinize")))
                .andExpect(jsonPath("$.data.memoryTip", notNullValue()));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"LEARNER"})
    @DisplayName("Should return AI contextual usage and collocations")
    void testUsageEndpoint() throws Exception {
        AiUsageRequest request = new AiUsageRequest(testWord.getId(), null, "B2");

        mockMvc.perform(post("/api/v1/ai/contextual-usage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.word", is("scrutinize")))
                .andExpect(jsonPath("$.data.collocations", notNullValue()));
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"LEARNER"})
    @DisplayName("Should return 400 Bad Request when neither wordId nor word is provided")
    void testInvalidRequest() throws Exception {
        AiExplanationRequest request = new AiExplanationRequest(null, null, "B2");

        mockMvc.perform(post("/api/v1/ai/word-explanation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
