package com.memora.modules.partner.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.partner.dto.PartnerActivityResponse;
import com.memora.modules.partner.dto.PartnerLeaderboardEntryResponse;
import com.memora.modules.partner.dto.PartnerProgressResponse;
import com.memora.modules.partner.dto.PartnerRequestResponse;
import com.memora.modules.partner.dto.PartnerRequestsSummaryResponse;
import com.memora.modules.partner.dto.PartnerUserSummaryResponse;
import com.memora.modules.partner.dto.SendPartnerRequestDto;
import com.memora.modules.partner.service.PartnerActivityService;
import com.memora.modules.partner.service.PartnerLeaderboardService;
import com.memora.modules.partner.service.PartnerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for learning partner operations, user discovery, request dispatch,
 * request lifecycle transitions, active partner queries, partner progress sharing,
 * pair-only leaderboard, and partner activity feed.
 */
@RestController
@RequestMapping("/api/v1/partners")
public class PartnerController {

    private final PartnerService partnerService;
    private final PartnerLeaderboardService partnerLeaderboardService;
    private final PartnerActivityService partnerActivityService;

    @Autowired
    public PartnerController(PartnerService partnerService,
                             @Autowired(required = false) PartnerLeaderboardService partnerLeaderboardService,
                             @Autowired(required = false) PartnerActivityService partnerActivityService) {
        this.partnerService = partnerService;
        this.partnerLeaderboardService = partnerLeaderboardService;
        this.partnerActivityService = partnerActivityService;
    }

    public PartnerController(PartnerService partnerService) {
        this(partnerService, null, null);
    }

    /**
     * Searches for a registered learner by exact normalized email address.
     */
    @GetMapping(value = "/search", params = "email")
    public ResponseEntity<ApiResponse<PartnerUserSummaryResponse>> searchByEmail(
            @RequestParam("email") String email,
            Authentication authentication) {
        String currentEmail = authentication.getName();
        PartnerUserSummaryResponse result = partnerService.searchByEmail(currentEmail, email);
        return ResponseEntity.ok(ApiResponse.success("User found successfully", result));
    }

    /**
     * Searches for registered learners by name, excluding the authenticated user.
     */
    @GetMapping(value = "/search", params = "!email")
    public ResponseEntity<ApiResponse<List<PartnerUserSummaryResponse>>> searchUsers(
            @RequestParam(required = false, defaultValue = "") String query,
            Authentication authentication) {
        String email = authentication.getName();
        List<PartnerUserSummaryResponse> results = partnerService.searchUsers(email, query);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", results));
    }

    /**
     * Retrieves the pair-only leaderboard for the authenticated user and their accepted partners.
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<PartnerLeaderboardEntryResponse>>> getPartnerLeaderboard(
            Authentication authentication) {
        String email = authentication.getName();
        List<PartnerLeaderboardEntryResponse> leaderboard = (partnerLeaderboardService != null)
                ? partnerLeaderboardService.getPartnerLeaderboard(email)
                : List.of();
        return ResponseEntity.ok(ApiResponse.success("Partner leaderboard retrieved successfully", leaderboard));
    }

    /**
     * Retrieves the chronological activity feed of accepted learning partners.
     */
    @GetMapping("/activity")
    public ResponseEntity<ApiResponse<List<PartnerActivityResponse>>> getPartnerActivities(
            @RequestParam(required = false, defaultValue = "20") int limit,
            Authentication authentication) {
        String email = authentication.getName();
        List<PartnerActivityResponse> activities = (partnerActivityService != null)
                ? partnerActivityService.getPartnerActivities(email, limit)
                : List.of();
        return ResponseEntity.ok(ApiResponse.success("Partner activity feed retrieved successfully", activities));
    }

    /**
     * Retrieves all active accepted learning partners for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PartnerUserSummaryResponse>>> getActivePartners(
            Authentication authentication) {
        String email = authentication.getName();
        List<PartnerUserSummaryResponse> partners = partnerService.getActivePartners(email);
        return ResponseEntity.ok(ApiResponse.success("Active partners retrieved successfully", partners));
    }

    /**
     * Retrieves all pending incoming and outgoing partner requests for the authenticated user.
     */
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<PartnerRequestsSummaryResponse>> getPartnerRequests(
            Authentication authentication) {
        String email = authentication.getName();
        PartnerRequestsSummaryResponse requests = partnerService.getPartnerRequests(email);
        return ResponseEntity.ok(ApiResponse.success("Partner requests retrieved successfully", requests));
    }

    /**
     * Sends or renews a learning partner connection request.
     */
    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<PartnerRequestResponse>> sendPartnerRequest(
            @Valid @RequestBody SendPartnerRequestDto dto,
            Authentication authentication) {
        String email = authentication.getName();
        PartnerRequestResponse response = partnerService.sendPartnerRequest(email, dto);
        return ResponseEntity.ok(ApiResponse.success("Partner request sent successfully", response));
    }

    /**
     * Accepts a pending partner request by ID. Only the intended recipient can accept.
     */
    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<ApiResponse<PartnerUserSummaryResponse>> acceptRequest(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        PartnerUserSummaryResponse response = partnerService.acceptRequest(email, id);
        return ResponseEntity.ok(ApiResponse.success("Partner request accepted successfully", response));
    }

    /**
     * Rejects a pending partner request by ID. Only the recipient can reject.
     */
    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<ApiResponse<PartnerRequestResponse>> rejectRequest(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        PartnerRequestResponse response = partnerService.rejectRequest(email, id);
        return ResponseEntity.ok(ApiResponse.success("Partner request rejected successfully", response));
    }

    /**
     * Cancels an outgoing pending partner request by ID. Only the original sender can cancel.
     */
    @PostMapping("/requests/{id}/cancel")
    public ResponseEntity<ApiResponse<PartnerRequestResponse>> cancelRequest(
            @PathVariable Long id,
            Authentication authentication) {
        String email = authentication.getName();
        PartnerRequestResponse response = partnerService.cancelRequest(email, id);
        return ResponseEntity.ok(ApiResponse.success("Partner request cancelled successfully", response));
    }

    /**
     * Retrieves the privacy-safe learning progress of an accepted learning partner.
     * Only accessible if an active ACCEPTED relationship exists between the users.
     */
    @GetMapping("/{partnerId}/progress")
    public ResponseEntity<ApiResponse<PartnerProgressResponse>> getPartnerProgress(
            @PathVariable Long partnerId,
            Authentication authentication) {
        String email = authentication.getName();
        PartnerProgressResponse response = partnerService.getPartnerProgress(email, partnerId);
        return ResponseEntity.ok(ApiResponse.success("Partner progress retrieved successfully", response));
    }
}
