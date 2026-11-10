package com.memora.modules.partner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.assessment.repository.AssessmentRepository;
import com.memora.modules.user.dto.RegistrationRequest;
import com.memora.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NewUserPartnerAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String TEST_EMAIL = "newuser.access.test@memora.com";

    @AfterEach
    void tearDown() {
        userRepository.findByEmail(TEST_EMAIL).ifPresent(user -> {
            assessmentRepository.deleteAll(assessmentRepository.findByUserIdOrderByStartedAtDesc(user.getId()));
            userRepository.delete(user);
        });
    }

    @Test
    @DisplayName("Lifecycle Test: Partner Access is LOCKED until placement assessment completion, then UNLOCKED")
    void testPartnerAccessLockedUntilPlacementComplete() throws Exception {
        // --- 0. Unauthenticated Access -> 401 Unauthorized ---
        mockMvc.perform(get("/api/v1/partners"))
                .andExpect(status().isUnauthorized());

        // --- 1. Register brand new user (Default LEARNER, A1, 0 XP, 0 streak) ---
        RegistrationRequest regRequest = new RegistrationRequest("New Brand User", TEST_EMAIL, "Password123!");
        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.user.role", is("LEARNER")))
                .andExpect(jsonPath("$.data.user.currentLevel", is("A1")))
                .andReturn();

        String token = objectMapper.readTree(regResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
        String authHeader = "Bearer " + token;

        // Verify user profile endpoint is accessible
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email", is(TEST_EMAIL)));

        // Verify initial onboarding state is ONBOARDING_REQUIRED
        mockMvc.perform(get("/api/v1/onboarding/state").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state", is("ONBOARDING_REQUIRED")));

        // --- 2. PHASE A: Placement Test NOT Started -> Partner Endpoints MUST RETURN 403 FORBIDDEN ---
        mockMvc.perform(get("/api/v1/partners").header("Authorization", authHeader))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Placement assessment must be completed")));

        mockMvc.perform(get("/api/v1/partners/requests").header("Authorization", authHeader))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Placement assessment must be completed")));

        mockMvc.perform(get("/api/v1/partners/challenges").header("Authorization", authHeader))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/partners/leaderboard").header("Authorization", authHeader))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/partners/activity").header("Authorization", authHeader))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/partners/search?query=test").header("Authorization", authHeader))
                .andExpect(status().isForbidden());

        // --- 3. PHASE B: Placement Test Started but INCOMPLETE -> Partner Endpoints MUST REMAIN 403 FORBIDDEN ---
        MvcResult startResult = mockMvc.perform(post("/api/v1/assessments/start")
                        .header("Authorization", authHeader))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.assessmentId").isNumber())
                .andReturn();

        long assessmentId = objectMapper.readTree(startResult.getResponse().getContentAsString())
                .path("data").path("assessmentId").asLong();

        // Verify onboarding state is now ASSESSMENT_IN_PROGRESS
        mockMvc.perform(get("/api/v1/onboarding/state").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state", is("ASSESSMENT_IN_PROGRESS")));

        // Incomplete test: Still forbidden
        mockMvc.perform(get("/api/v1/partners").header("Authorization", authHeader))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("Placement assessment must be completed")));

        // Answer all questions in the assessment
        com.fasterxml.jackson.databind.JsonNode questionsNode = objectMapper.readTree(startResult.getResponse().getContentAsString())
                .path("data").path("questions");
        for (com.fasterxml.jackson.databind.JsonNode q : questionsNode) {
            long qId = q.path("questionId").asLong();
            mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/questions/" + qId + "/answer")
                            .header("Authorization", authHeader)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"answer\":\"test answer\",\"responseTimeMs\":1200}"))
                    .andExpect(status().isOk());
        }

        // --- 4. PHASE C: Placement Test COMPLETED -> Partner Endpoints MUST BE UNLOCKED (HTTP 200 OK) ---
        mockMvc.perform(post("/api/v1/assessments/" + assessmentId + "/complete")
                        .header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // Verify onboarding state transitioned out of ONBOARDING_REQUIRED / ASSESSMENT_IN_PROGRESS
        mockMvc.perform(get("/api/v1/onboarding/state").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.state", is("LEARNING_PATH_REQUIRED")));

        // Now Partner section is UNLOCKED!
        mockMvc.perform(get("/api/v1/partners").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));

        mockMvc.perform(get("/api/v1/partners/requests").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.incoming", hasSize(0)))
                .andExpect(jsonPath("$.data.outgoing", hasSize(0)));

        mockMvc.perform(get("/api/v1/partners/challenges").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));

        mockMvc.perform(get("/api/v1/partners/leaderboard").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        mockMvc.perform(get("/api/v1/partners/activity").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));

        mockMvc.perform(get("/api/v1/partners/search?query=test").header("Authorization", authHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }
}
