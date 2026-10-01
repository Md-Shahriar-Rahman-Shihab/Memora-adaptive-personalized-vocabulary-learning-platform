package com.memora.modules.partner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.partner.dto.SendPartnerRequestDto;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerRelationshipRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PartnerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PartnerRelationshipRepository partnerRelationshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User userA;
    private User userB;
    private User userC;

    private static final String EMAIL_A = "alice.partnerctrl@test.com";
    private static final String EMAIL_B = "bob.partnerctrl@test.com";
    private static final String EMAIL_C = "charlie.partnerctrl@test.com";

    @BeforeEach
    void setUp() {
        cleanTestData();

        userA = userRepository.save(new User("Alice PartnerController", EMAIL_A, "hash1", VocabularyLevel.B1, Role.LEARNER));
        userB = userRepository.save(new User("Bob PartnerController", EMAIL_B, "hash2", VocabularyLevel.B2, Role.LEARNER));
        userC = userRepository.save(new User("Charlie PartnerController", EMAIL_C, "hash3", VocabularyLevel.C1, Role.LEARNER));
    }

    @AfterEach
    void tearDown() {
        cleanTestData();
    }

    private void cleanTestData() {
        partnerRelationshipRepository.deleteAll();
        userRepository.findByEmail(EMAIL_A).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_B).ifPresent(userRepository::delete);
        userRepository.findByEmail(EMAIL_C).ifPresent(userRepository::delete);
    }

    // --- Search Endpoint Tests ---

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("GET /api/v1/partners/search returns matching users without sensitive info")
    void testSearchUsers() throws Exception {
        mockMvc.perform(get("/api/v1/partners/search")
                        .param("query", "Bob PartnerController"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(userB.getId().intValue())))
                .andExpect(jsonPath("$.data[0].name", is("Bob PartnerController")))
                .andExpect(jsonPath("$.data[0].currentLevel", is("B2")))
                .andExpect(jsonPath("$.data[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data[0].email").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/partners/search requires authentication")
    void testSearchUsers_Unauthenticated_Fails() throws Exception {
        mockMvc.perform(get("/api/v1/partners/search")
                        .param("query", "Bob PartnerController"))
                .andExpect(status().isUnauthorized());
    }

    // --- Send Request Endpoint Tests ---

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("POST /api/v1/partners/requests successfully dispatches partner request")
    void testSendPartnerRequest_Success() throws Exception {
        SendPartnerRequestDto dto = new SendPartnerRequestDto(userB.getId());

        mockMvc.perform(post("/api/v1/partners/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andExpect(jsonPath("$.data.sender.id", is(userA.getId().intValue())))
                .andExpect(jsonPath("$.data.receiver.id", is(userB.getId().intValue())))
                .andExpect(jsonPath("$.data.incoming", is(false)));
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("POST /api/v1/partners/requests rejects self-request with 400")
    void testSendPartnerRequest_SelfRequest_BadRequest() throws Exception {
        SendPartnerRequestDto dto = new SendPartnerRequestDto(userA.getId());

        mockMvc.perform(post("/api/v1/partners/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot send partner request to yourself")));
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("POST /api/v1/partners/requests rejects nonexistent target user with 404")
    void testSendPartnerRequest_NonexistentUser_NotFound() throws Exception {
        SendPartnerRequestDto dto = new SendPartnerRequestDto(88888L);

        mockMvc.perform(post("/api/v1/partners/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    // --- Accept Request Endpoint Tests ---

    @Test
    @WithMockUser(username = EMAIL_B)
    @DisplayName("POST /api/v1/partners/requests/{id}/accept allows intended recipient to accept")
    void testAcceptPartnerRequest_Recipient_Success() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship rel = partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.PENDING));

        mockMvc.perform(post("/api/v1/partners/requests/{id}/accept", rel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(userA.getId().intValue())))
                .andExpect(jsonPath("$.data.relationshipStatus", is("ACCEPTED")));

        PartnerRelationship updated = partnerRelationshipRepository.findById(rel.getId()).orElseThrow();
        assertEquals(PartnerRelationshipStatus.ACCEPTED, updated.getStatus());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("POST /api/v1/partners/requests/{id}/accept forbids sender from accepting own request")
    void testAcceptPartnerRequest_Sender_Forbidden() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship rel = partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.PENDING));

        mockMvc.perform(post("/api/v1/partners/requests/{id}/accept", rel.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMAIL_C)
    @DisplayName("POST /api/v1/partners/requests/{id}/accept prevents horizontal privilege escalation (User C)")
    void testAcceptPartnerRequest_ThirdParty_Forbidden() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship rel = partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.PENDING));

        mockMvc.perform(post("/api/v1/partners/requests/{id}/accept", rel.getId()))
                .andExpect(status().isForbidden());
    }

    // --- Reject Request Endpoint Tests ---

    @Test
    @WithMockUser(username = EMAIL_B)
    @DisplayName("POST /api/v1/partners/requests/{id}/reject allows intended recipient to reject")
    void testRejectPartnerRequest_Recipient_Success() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship rel = partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.PENDING));

        mockMvc.perform(post("/api/v1/partners/requests/{id}/reject", rel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("REJECTED")));

        PartnerRelationship updated = partnerRelationshipRepository.findById(rel.getId()).orElseThrow();
        assertEquals(PartnerRelationshipStatus.REJECTED, updated.getStatus());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("POST /api/v1/partners/requests/{id}/reject forbids sender from rejecting own request")
    void testRejectPartnerRequest_Sender_Forbidden() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship rel = partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.PENDING));

        mockMvc.perform(post("/api/v1/partners/requests/{id}/reject", rel.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMAIL_C)
    @DisplayName("POST /api/v1/partners/requests/{id}/reject prevents horizontal privilege escalation (User C)")
    void testRejectPartnerRequest_ThirdParty_Forbidden() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship rel = partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.PENDING));

        mockMvc.perform(post("/api/v1/partners/requests/{id}/reject", rel.getId()))
                .andExpect(status().isForbidden());
    }

    // --- Cancel Request Endpoint Tests ---

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("POST /api/v1/partners/requests/{id}/cancel allows sender to cancel request")
    void testCancelPartnerRequest_Sender_Success() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship rel = partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.PENDING));

        mockMvc.perform(post("/api/v1/partners/requests/{id}/cancel", rel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("CANCELLED")));

        PartnerRelationship updated = partnerRelationshipRepository.findById(rel.getId()).orElseThrow();
        assertEquals(PartnerRelationshipStatus.CANCELLED, updated.getStatus());
    }

    @Test
    @WithMockUser(username = EMAIL_B)
    @DisplayName("POST /api/v1/partners/requests/{id}/cancel forbids recipient from cancelling sender's request")
    void testCancelPartnerRequest_Recipient_Forbidden() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship rel = partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.PENDING));

        mockMvc.perform(post("/api/v1/partners/requests/{id}/cancel", rel.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMAIL_C)
    @DisplayName("POST /api/v1/partners/requests/{id}/cancel prevents horizontal privilege escalation (User C)")
    void testCancelPartnerRequest_ThirdParty_Forbidden() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        PartnerRelationship rel = partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.PENDING));

        mockMvc.perform(post("/api/v1/partners/requests/{id}/cancel", rel.getId()))
                .andExpect(status().isForbidden());
    }

    // --- Query Endpoints Tests ---

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("GET /api/v1/partners returns active accepted partners")
    void testGetActivePartners() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.ACCEPTED));

        mockMvc.perform(get("/api/v1/partners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id", is(userB.getId().intValue())))
                .andExpect(jsonPath("$.data[0].relationshipStatus", is("ACCEPTED")));
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("GET /api/v1/partners/requests returns pending incoming and outgoing lists")
    void testGetPartnerRequests() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.PENDING));

        mockMvc.perform(get("/api/v1/partners/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.outgoing", hasSize(1)))
                .andExpect(jsonPath("$.data.incoming", hasSize(0)));
    }

    // --- Partner Progress Endpoint Tests (Phase C) ---

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("GET /api/v1/partners/{partnerId}/progress returns privacy-safe partner stats")
    void testGetPartnerProgress_Accepted_Success() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.ACCEPTED));

        mockMvc.perform(get("/api/v1/partners/{partnerId}/progress", userB.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(userB.getId().intValue())))
                .andExpect(jsonPath("$.data.name", is("Bob PartnerController")))
                .andExpect(jsonPath("$.data.currentLevel", is("B2")))
                .andExpect(jsonPath("$.data.email").doesNotExist())
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andExpect(jsonPath("$.data.assessments").doesNotExist())
                .andExpect(jsonPath("$.data.answers").doesNotExist());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("GET /api/v1/partners/{partnerId}/progress forbidden when relationship is pending")
    void testGetPartnerProgress_Pending_Forbidden() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.PENDING));

        mockMvc.perform(get("/api/v1/partners/{partnerId}/progress", userB.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("GET /api/v1/partners/{partnerId}/progress forbidden when relationship is rejected")
    void testGetPartnerProgress_Rejected_Forbidden() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.REJECTED));

        mockMvc.perform(get("/api/v1/partners/{partnerId}/progress", userB.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMAIL_A)
    @DisplayName("GET /api/v1/partners/{partnerId}/progress forbidden when relationship is cancelled")
    void testGetPartnerProgress_Cancelled_Forbidden() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.CANCELLED));

        mockMvc.perform(get("/api/v1/partners/{partnerId}/progress", userB.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMAIL_C)
    @DisplayName("GET /api/v1/partners/{partnerId}/progress forbidden for unrelated third user")
    void testGetPartnerProgress_UnrelatedThirdUser_Forbidden() throws Exception {
        User userOne = userA.getId() < userB.getId() ? userA : userB;
        User userTwo = userA.getId() < userB.getId() ? userB : userA;
        partnerRelationshipRepository.save(
                new PartnerRelationship(userOne, userTwo, userA, PartnerRelationshipStatus.ACCEPTED));

        mockMvc.perform(get("/api/v1/partners/{partnerId}/progress", userA.getId()))
                .andExpect(status().isForbidden());
    }
}
