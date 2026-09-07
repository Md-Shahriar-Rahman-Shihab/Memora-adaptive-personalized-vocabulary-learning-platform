package com.memora.modules.ai;

import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdaptiveInsightControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_EMAIL = "insight_learner@example.com";

    @BeforeEach
    void setUp() {
        if (!userRepository.existsByEmail(TEST_EMAIL)) {
            User user = new User();
            user.setName("Insight Learner");
            user.setEmail(TEST_EMAIL);
            user.setPasswordHash("hashedpassword123");
            user.setRole(Role.LEARNER);
            user.setCurrentLevel(VocabularyLevel.B1);
            user.setXp(250);
            user.setStreak(3);
            userRepository.save(user);
        }
    }

    @Test
    @DisplayName("Should return 401 Unauthorized for unauthenticated requests")
    void testUnauthenticatedGet() throws Exception {
        mockMvc.perform(get("/api/v1/insights/today"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = TEST_EMAIL, roles = {"LEARNER"})
    @DisplayName("Should return today's adaptive insights for authenticated user")
    void testGetTodayInsights() throws Exception {
        mockMvc.perform(get("/api/v1/insights/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.primaryInsight", notNullValue()))
                .andExpect(jsonPath("$.data.primaryInsight.title", notNullValue()))
                .andExpect(jsonPath("$.data.primaryInsight.message", notNullValue()))
                .andExpect(jsonPath("$.data.metrics", notNullValue()))
                .andExpect(jsonPath("$.data.metrics.currentStreak", is(3)))
                .andExpect(jsonPath("$.data.metrics.totalXp", is(250)));
    }
}
