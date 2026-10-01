package com.memora.modules.partner;

import com.memora.common.exception.ResourceNotFoundException;
import com.memora.modules.partner.domain.PartnerRelationshipStatus;
import com.memora.modules.partner.dto.PartnerProgressResponse;
import com.memora.modules.partner.dto.PartnerRequestResponse;
import com.memora.modules.partner.dto.PartnerRequestsSummaryResponse;
import com.memora.modules.partner.dto.PartnerUserSummaryResponse;
import com.memora.modules.partner.dto.SendPartnerRequestDto;
import com.memora.modules.partner.entity.PartnerRelationship;
import com.memora.modules.partner.repository.PartnerRelationshipRepository;
import com.memora.modules.partner.service.PartnerService;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PartnerServiceTest {

    @Autowired
    private PartnerService partnerService;

    @Autowired
    private PartnerRelationshipRepository partnerRelationshipRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;
    private User userC;

    private static final String EMAIL_A = "alice.partnerservice@test.com";
    private static final String EMAIL_B = "bob.partnerservice@test.com";
    private static final String EMAIL_C = "charlie.partnerservice@test.com";

    @BeforeEach
    void setUp() {
        cleanTestData();

        userA = userRepository.save(new User("Alice PartnerService", EMAIL_A, "hash1", VocabularyLevel.B1, Role.LEARNER));
        userB = userRepository.save(new User("Bob PartnerService", EMAIL_B, "hash2", VocabularyLevel.B2, Role.LEARNER));
        userC = userRepository.save(new User("Charlie PartnerService", EMAIL_C, "hash3", VocabularyLevel.C1, Role.LEARNER));
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

    // --- Search Tests ---

    @Test
    @DisplayName("Search users excludes the authenticated user")
    void testSearchUsers_ExcludesCurrentUser() {
        // Alice searches for "Alice PartnerService" -> should exclude herself
        List<PartnerUserSummaryResponse> selfResults = partnerService.searchUsers(userA.getEmail(), "Alice PartnerService");
        assertTrue(selfResults.isEmpty(), "Self must be excluded from search");

        // Alice searches for "Bob PartnerService" -> should find Bob
        List<PartnerUserSummaryResponse> results = partnerService.searchUsers(userA.getEmail(), "Bob PartnerService");
        assertEquals(1, results.size());
        assertEquals(userB.getId(), results.get(0).getId());
    }

    @Test
    @DisplayName("Search users returns only privacy-safe fields")
    void testSearchUsers_ReturnsSafePublicDtoOnly() {
        List<PartnerUserSummaryResponse> results = partnerService.searchUsers(userA.getEmail(), "Bob PartnerService");

        assertEquals(1, results.size());
        PartnerUserSummaryResponse summary = results.get(0);
        assertEquals(userB.getId(), summary.getId());
        assertEquals("Bob PartnerService", summary.getName());
        assertEquals("B2", summary.getCurrentLevel());
        assertEquals(0, summary.getXp());
        assertEquals(0, summary.getStreak());
    }

    @Test
    @DisplayName("Search users with empty query returns empty list")
    void testSearchUsers_EmptyQuery_ReturnsEmptyList() {
        List<PartnerUserSummaryResponse> results = partnerService.searchUsers(userA.getEmail(), "   ");
        assertTrue(results.isEmpty());
    }

    // --- Send Request Tests ---

    @Test
    @DisplayName("Send partner request enforces canonical ordering (userOne.id < userTwo.id)")
    void testSendRequest_CanonicalOrdering() {
        User lower = userA.getId() < userB.getId() ? userA : userB;
        User higher = userA.getId() < userB.getId() ? userB : userA;

        // Case 1: lower sends to higher
        PartnerRequestResponse resp1 = partnerService.sendPartnerRequest(
                lower.getEmail(), new SendPartnerRequestDto(higher.getId()));
        assertNotNull(resp1.getId());
        assertEquals(PartnerRelationshipStatus.PENDING.name(), resp1.getStatus());

        PartnerRelationship rel1 = partnerRelationshipRepository.findById(resp1.getId()).orElseThrow();
        assertEquals(lower.getId(), rel1.getUserOne().getId());
        assertEquals(higher.getId(), rel1.getUserTwo().getId());
        assertEquals(lower.getId(), rel1.getRequestedBy().getId());

        // Clean up relationship
        partnerRelationshipRepository.deleteAll();

        // Case 2: higher sends to lower -> userOne must still be lower, userTwo must still be higher
        PartnerRequestResponse resp2 = partnerService.sendPartnerRequest(
                higher.getEmail(), new SendPartnerRequestDto(lower.getId()));
        assertNotNull(resp2.getId());

        PartnerRelationship rel2 = partnerRelationshipRepository.findById(resp2.getId()).orElseThrow();
        assertEquals(lower.getId(), rel2.getUserOne().getId());
        assertEquals(higher.getId(), rel2.getUserTwo().getId());
        assertEquals(higher.getId(), rel2.getRequestedBy().getId());
    }

    @Test
    @DisplayName("Send request to self is rejected with IllegalArgumentException")
    void testSendRequest_SelfRequest_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
                partnerService.sendPartnerRequest(userA.getEmail(), new SendPartnerRequestDto(userA.getId())));
    }

    @Test
    @DisplayName("Send request to nonexistent user throws ResourceNotFoundException")
    void testSendRequest_NonexistentUser_ThrowsException() {
        assertThrows(ResourceNotFoundException.class, () ->
                partnerService.sendPartnerRequest(userA.getEmail(), new SendPartnerRequestDto(99999L)));
    }

    @Test
    @DisplayName("Duplicate pending request from same sender is rejected")
    void testSendRequest_DuplicatePending_ThrowsException() {
        partnerService.sendPartnerRequest(userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        assertThrows(IllegalArgumentException.class, () ->
                partnerService.sendPartnerRequest(userA.getEmail(), new SendPartnerRequestDto(userB.getId())));
    }

    @Test
    @DisplayName("Reciprocal request from recipient while pending directs them to accept")
    void testSendRequest_ReciprocalPending_ThrowsException() {
        partnerService.sendPartnerRequest(userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                partnerService.sendPartnerRequest(userB.getEmail(), new SendPartnerRequestDto(userA.getId())));
        assertTrue(ex.getMessage().contains("Please accept"));
    }

    @Test
    @DisplayName("Sending request to an already accepted partner is rejected")
    void testSendRequest_AlreadyAccepted_ThrowsException() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));
        partnerService.acceptRequest(userB.getEmail(), req.getId());

        assertThrows(IllegalArgumentException.class, () ->
                partnerService.sendPartnerRequest(userA.getEmail(), new SendPartnerRequestDto(userB.getId())));
    }

    @Test
    @DisplayName("Cancelled relationship row is reused cleanly when sending a new request")
    void testSendRequest_ReusesCancelledRelationship() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));
        partnerService.cancelRequest(userA.getEmail(), req.getId());

        assertEquals(1, partnerRelationshipRepository.count());

        // B sends to A now: row should be reused, requestedBy updated to B
        PartnerRequestResponse reRequest = partnerService.sendPartnerRequest(
                userB.getEmail(), new SendPartnerRequestDto(userA.getId()));

        assertEquals(1, partnerRelationshipRepository.count(), "Must reuse existing row without duplicate record");
        assertEquals(req.getId(), reRequest.getId());
        assertEquals(PartnerRelationshipStatus.PENDING.name(), reRequest.getStatus());

        PartnerRelationship refreshed = partnerRelationshipRepository.findById(req.getId()).orElseThrow();
        assertEquals(userB.getId(), refreshed.getRequestedBy().getId());
        assertEquals(PartnerRelationshipStatus.PENDING, refreshed.getStatus());
    }

    @Test
    @DisplayName("Rejected relationship row is reused cleanly when sending a new request")
    void testSendRequest_ReusesRejectedRelationship() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));
        partnerService.rejectRequest(userB.getEmail(), req.getId());

        assertEquals(1, partnerRelationshipRepository.count());

        // A sends to B again: row reused, status updated to PENDING
        PartnerRequestResponse reRequest = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        assertEquals(1, partnerRelationshipRepository.count(), "Must reuse existing row without duplicate record");
        assertEquals(req.getId(), reRequest.getId());
        assertEquals(PartnerRelationshipStatus.PENDING.name(), reRequest.getStatus());

        PartnerRelationship refreshed = partnerRelationshipRepository.findById(req.getId()).orElseThrow();
        assertEquals(userA.getId(), refreshed.getRequestedBy().getId());
        assertEquals(PartnerRelationshipStatus.PENDING, refreshed.getStatus());
    }

    // --- Accept Request Tests ---

    @Test
    @DisplayName("Recipient can accept pending partner request")
    void testAcceptRequest_RecipientCanAccept() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        PartnerUserSummaryResponse accepted = partnerService.acceptRequest(userB.getEmail(), req.getId());
        assertNotNull(accepted);
        assertEquals(userA.getId(), accepted.getId());
        assertEquals(PartnerRelationshipStatus.ACCEPTED.name(), accepted.getRelationshipStatus());

        PartnerRelationship rel = partnerRelationshipRepository.findById(req.getId()).orElseThrow();
        assertEquals(PartnerRelationshipStatus.ACCEPTED, rel.getStatus());
    }

    @Test
    @DisplayName("Sender cannot accept their own outgoing request")
    void testAcceptRequest_SenderCannotAccept() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        assertThrows(AccessDeniedException.class, () ->
                partnerService.acceptRequest(userA.getEmail(), req.getId()));
    }

    @Test
    @DisplayName("Non-participant cannot accept request (horizontal privilege escalation check)")
    void testAcceptRequest_NonParticipantCannotAccept() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        assertThrows(AccessDeniedException.class, () ->
                partnerService.acceptRequest(userC.getEmail(), req.getId()));
    }

    @Test
    @DisplayName("Already accepted request cannot be accepted again")
    void testAcceptRequest_AlreadyAccepted_ThrowsException() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));
        partnerService.acceptRequest(userB.getEmail(), req.getId());

        assertThrows(IllegalArgumentException.class, () ->
                partnerService.acceptRequest(userB.getEmail(), req.getId()));
    }

    // --- Reject Request Tests ---

    @Test
    @DisplayName("Recipient can reject pending partner request")
    void testRejectRequest_RecipientCanReject() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        PartnerRequestResponse rejected = partnerService.rejectRequest(userB.getEmail(), req.getId());
        assertEquals(PartnerRelationshipStatus.REJECTED.name(), rejected.getStatus());

        PartnerRelationship rel = partnerRelationshipRepository.findById(req.getId()).orElseThrow();
        assertEquals(PartnerRelationshipStatus.REJECTED, rel.getStatus());
    }

    @Test
    @DisplayName("Sender cannot reject their own outgoing request")
    void testRejectRequest_SenderCannotReject() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        assertThrows(AccessDeniedException.class, () ->
                partnerService.rejectRequest(userA.getEmail(), req.getId()));
    }

    @Test
    @DisplayName("Non-participant cannot reject request")
    void testRejectRequest_NonParticipantCannotReject() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        assertThrows(AccessDeniedException.class, () ->
                partnerService.rejectRequest(userC.getEmail(), req.getId()));
    }

    // --- Cancel Request Tests ---

    @Test
    @DisplayName("Sender can cancel their pending partner request")
    void testCancelRequest_SenderCanCancel() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        PartnerRequestResponse cancelled = partnerService.cancelRequest(userA.getEmail(), req.getId());
        assertEquals(PartnerRelationshipStatus.CANCELLED.name(), cancelled.getStatus());

        PartnerRelationship rel = partnerRelationshipRepository.findById(req.getId()).orElseThrow();
        assertEquals(PartnerRelationshipStatus.CANCELLED, rel.getStatus());
    }

    @Test
    @DisplayName("Recipient cannot cancel sender's outgoing request")
    void testCancelRequest_RecipientCannotCancel() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        assertThrows(AccessDeniedException.class, () ->
                partnerService.cancelRequest(userB.getEmail(), req.getId()));
    }

    @Test
    @DisplayName("Non-participant cannot cancel request")
    void testCancelRequest_NonParticipantCannotCancel() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        assertThrows(AccessDeniedException.class, () ->
                partnerService.cancelRequest(userC.getEmail(), req.getId()));
    }

    // --- Active Partners & Requests Query Tests ---

    @Test
    @DisplayName("getActivePartners returns only accepted partners for current user")
    void testGetActivePartners() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));
        partnerService.acceptRequest(userB.getEmail(), req.getId());

        List<PartnerUserSummaryResponse> partnersA = partnerService.getActivePartners(userA.getEmail());
        assertEquals(1, partnersA.size());
        assertEquals(userB.getId(), partnersA.get(0).getId());

        List<PartnerUserSummaryResponse> partnersB = partnerService.getActivePartners(userB.getEmail());
        assertEquals(1, partnersB.size());
        assertEquals(userA.getId(), partnersB.get(0).getId());

        List<PartnerUserSummaryResponse> partnersC = partnerService.getActivePartners(userC.getEmail());
        assertEquals(0, partnersC.size());
    }

    @Test
    @DisplayName("getPartnerRequests segregates incoming and outgoing pending requests")
    void testGetPartnerRequests() {
        partnerService.sendPartnerRequest(userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        PartnerRequestsSummaryResponse summaryA = partnerService.getPartnerRequests(userA.getEmail());
        assertEquals(0, summaryA.getIncoming().size());
        assertEquals(1, summaryA.getOutgoing().size());
        assertFalse(summaryA.getOutgoing().get(0).isIncoming());

        PartnerRequestsSummaryResponse summaryB = partnerService.getPartnerRequests(userB.getEmail());
        assertEquals(1, summaryB.getIncoming().size());
        assertEquals(0, summaryB.getOutgoing().size());
        assertTrue(summaryB.getIncoming().get(0).isIncoming());
    }

    // --- Partner Progress Security Tests (Phase C) ---

    @Test
    @DisplayName("Accepted partners can view each other's progress bidirectionally")
    void testPartnerProgress_AcceptedPartners_AllowedBidirectional() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));
        partnerService.acceptRequest(userB.getEmail(), req.getId());

        // A -> B progress
        PartnerProgressResponse progressB = partnerService.getPartnerProgress(userA.getEmail(), userB.getId());
        assertNotNull(progressB);
        assertEquals(userB.getId(), progressB.getId());
        assertEquals("Bob PartnerService", progressB.getName());
        assertEquals("B2", progressB.getCurrentLevel());

        // B -> A progress
        PartnerProgressResponse progressA = partnerService.getPartnerProgress(userB.getEmail(), userA.getId());
        assertNotNull(progressA);
        assertEquals(userA.getId(), progressA.getId());
        assertEquals("Alice PartnerService", progressA.getName());
        assertEquals("B1", progressA.getCurrentLevel());
    }

    @Test
    @DisplayName("Pending request participants cannot view progress (forbidden)")
    void testPartnerProgress_PendingRequest_ForbiddenBidirectional() {
        partnerService.sendPartnerRequest(userA.getEmail(), new SendPartnerRequestDto(userB.getId()));

        assertThrows(AccessDeniedException.class, () ->
                partnerService.getPartnerProgress(userA.getEmail(), userB.getId()));
        assertThrows(AccessDeniedException.class, () ->
                partnerService.getPartnerProgress(userB.getEmail(), userA.getId()));
    }

    @Test
    @DisplayName("Rejected request participants cannot view progress (forbidden)")
    void testPartnerProgress_RejectedRequest_Forbidden() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));
        partnerService.rejectRequest(userB.getEmail(), req.getId());

        assertThrows(AccessDeniedException.class, () ->
                partnerService.getPartnerProgress(userA.getEmail(), userB.getId()));
        assertThrows(AccessDeniedException.class, () ->
                partnerService.getPartnerProgress(userB.getEmail(), userA.getId()));
    }

    @Test
    @DisplayName("Cancelled request participants cannot view progress (forbidden)")
    void testPartnerProgress_CancelledRequest_Forbidden() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));
        partnerService.cancelRequest(userA.getEmail(), req.getId());

        assertThrows(AccessDeniedException.class, () ->
                partnerService.getPartnerProgress(userA.getEmail(), userB.getId()));
        assertThrows(AccessDeniedException.class, () ->
                partnerService.getPartnerProgress(userB.getEmail(), userA.getId()));
    }

    @Test
    @DisplayName("Unrelated third user cannot view progress (forbidden)")
    void testPartnerProgress_UnrelatedThirdUser_Forbidden() {
        PartnerRequestResponse req = partnerService.sendPartnerRequest(
                userA.getEmail(), new SendPartnerRequestDto(userB.getId()));
        partnerService.acceptRequest(userB.getEmail(), req.getId());

        // User C tries to view User A's progress
        assertThrows(AccessDeniedException.class, () ->
                partnerService.getPartnerProgress(userC.getEmail(), userA.getId()));
        // User C tries to view User B's progress
        assertThrows(AccessDeniedException.class, () ->
                partnerService.getPartnerProgress(userC.getEmail(), userB.getId()));
    }

    @Test
    @DisplayName("Nonexistent partner ID throws ResourceNotFoundException")
    void testPartnerProgress_NonexistentPartner_ThrowsNotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                partnerService.getPartnerProgress(userA.getEmail(), 99999L));
    }
}
