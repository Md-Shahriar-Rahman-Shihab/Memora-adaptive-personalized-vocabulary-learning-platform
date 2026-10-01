package com.memora.modules.partner;

import com.memora.modules.partner.domain.PartnerActivityType;
import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerActivityRepository;
import com.memora.modules.partner.repository.PartnerRelationshipRepository;
import com.memora.modules.partner.service.PartnerActivityService;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PartnerPhaseEControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PartnerRelationshipRepository partnerRelationshipRepository;

    @Autowired
    private PartnerActivityRepository partnerActivityRepository;

    @Autowired
    private PartnerActivityService partnerActivityService;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;

    private static final String EMAIL_A = "alice.phase_e_ctrl@test.com";
    private static final String EMAIL_B = "bob.phase_e_ctrl@test.com";

    @BeforeEach
    void setUp() {
        cleanTestData();

        userA = userRepository.save(new User("Alice PhaseE", EMAIL_A, "hash1", VocabularyLevel.B1, Role.LEARNER));
        userA.setXp(500);
        userA = userRepository.save(userA);

        userB = userRepository.save(new User("Bob PhaseE", EMAIL_B, "hash2", VocabularyLevel.B2, Role.LEARNER));
        userB.setXp(1500);
        userB = userRepository.save(userB);
    }

    @AfterEach
    void tearDown() {
        cleanTestData();
    }

    private void cleanTestData() {
        partnerActivityRepository.deleteAll();
        partnerRelationshipRepository.deleteAll();
        userRepository.findByEmail(EMAIL_A).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_B).ifPresent(userRepository::delete);
    }

    @Test
    @DisplayName("7. Unauthenticated search request is rejected with 401")
    void searchByEmail_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/partners/search")
                        .param("email", EMAIL_B)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("Authenticated search returns 200 and privacy-safe user data")
    void searchByEmail_Authenticated_Returns200() throws Exception {
        mockMvc.perform(get("/api/v1/partners/search")
                        .param("email", EMAIL_B)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(userB.getId()))
                .andExpect(jsonPath("$.data.name").value("Bob PhaseE"))
                .andExpect(jsonPath("$.data.currentLevel").value("B2"))
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("Search self returns 400 Bad Request")
    void searchByEmail_Self_Returns400() throws Exception {
        mockMvc.perform(get("/api/v1/partners/search")
                        .param("email", EMAIL_A)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot add yourself as a learning partner."));
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("Search nonexistent email returns 404 Not Found")
    void searchByEmail_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/v1/partners/search")
                        .param("email", "nonexistent.learner@memora.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No Memora account found with this email."));
    }

    @Test
    @DisplayName("Unauthenticated partner leaderboard request is rejected with 401")
    void getLeaderboard_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/partners/leaderboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("Authenticated partner leaderboard returns 200 and ranked participants")
    void getLeaderboard_Authenticated_Returns200() throws Exception {
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.ACCEPTED));

        mockMvc.perform(get("/api/v1/partners/leaderboard")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].name").value("Bob PhaseE"))
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[0].xp").value(1500))
                .andExpect(jsonPath("$.data[1].name").value("Alice PhaseE"))
                .andExpect(jsonPath("$.data[1].rank").value(2))
                .andExpect(jsonPath("$.data[1].xp").value(500));
    }

    @Test
    @DisplayName("Unauthenticated partner activity feed request is rejected with 401")
    void getActivity_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/partners/activity")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("Authenticated partner activity returns 200 with recent events")
    void getActivity_Authenticated_Returns200() throws Exception {
        User u1 = userA.getId() < userB.getId() ? userA : userB;
        User u2 = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship rel = partnerRelationshipRepository.save(new PartnerRelationship(u1, u2, userA, PartnerRelationshipStatus.ACCEPTED));

        partnerActivityService.logActivity(rel, userB, PartnerActivityType.PARTNER_CONNECTED, "Bob joined Alice", null, 0);

        mockMvc.perform(get("/api/v1/partners/activity")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("Bob joined Alice"))
                .andExpect(jsonPath("$.data[0].activityType").value("PARTNER_CONNECTED"))
                .andExpect(jsonPath("$.data[0].actor.name").value("Bob PhaseE"));
    }
}
