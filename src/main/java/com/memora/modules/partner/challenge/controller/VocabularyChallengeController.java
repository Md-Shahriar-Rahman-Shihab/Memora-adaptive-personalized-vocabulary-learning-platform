package com.memora.modules.partner.challenge.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.partner.challenge.domain.ChallengeStatus;
import com.memora.modules.partner.challenge.dto.*;
import com.memora.modules.partner.challenge.service.VocabularyChallengeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller managing competitive vocabulary challenges between accepted learning partners.
 * Enforces authentication, authorization, lifecycle transitions, and anti-cheating data privacy.
 */
@RestController
@RequestMapping("/api/v1/partners/challenges")
public class VocabularyChallengeController {

    private final VocabularyChallengeService challengeService;

    public VocabularyChallengeController(VocabularyChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    /**
     * Creates a new challenge targeting an accepted learning partner.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ChallengeResponse>> createChallenge(
            @Valid @RequestBody CreateChallengeRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        ChallengeResponse response = challengeService.createChallenge(email, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Challenge created successfully", response));
    }

    /**
     * Lists all challenges involving the authenticated user, optionally filtered by status.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChallengeResponse>>> getMyChallenges(
            @RequestParam(required = false) ChallengeStatus status,
            Authentication authentication) {
        String email = authentication.getName();
        List<ChallengeResponse> response = challengeService.getMyChallenges(email, status);
        return ResponseEntity.ok(ApiResponse.success("Challenges retrieved successfully", response));
    }

    /**
     * Retrieves specific challenge details for an authorized participant with anti-cheating protection.
     */
    @GetMapping("/{challengeId}")
    public ResponseEntity<ApiResponse<ChallengeResponse>> getChallenge(
            @PathVariable Long challengeId,
            Authentication authentication) {
        String email = authentication.getName();
        ChallengeResponse response = challengeService.getChallenge(email, challengeId);
        return ResponseEntity.ok(ApiResponse.success("Challenge details retrieved successfully", response));
    }

    /**
     * Accepts a pending challenge invitation.
     */
    @PostMapping("/{challengeId}/accept")
    public ResponseEntity<ApiResponse<ChallengeResponse>> acceptChallenge(
            @PathVariable Long challengeId,
            Authentication authentication) {
        String email = authentication.getName();
        ChallengeResponse response = challengeService.acceptChallenge(email, challengeId);
        return ResponseEntity.ok(ApiResponse.success("Challenge accepted successfully", response));
    }

    /**
     * Declines a pending challenge invitation.
     */
    @PostMapping("/{challengeId}/decline")
    public ResponseEntity<ApiResponse<ChallengeResponse>> declineChallenge(
            @PathVariable Long challengeId,
            Authentication authentication) {
        String email = authentication.getName();
        ChallengeResponse response = challengeService.declineChallenge(email, challengeId);
        return ResponseEntity.ok(ApiResponse.success("Challenge declined successfully", response));
    }

    /**
     * Cancels an outgoing pending challenge invitation.
     */
    @PostMapping("/{challengeId}/cancel")
    public ResponseEntity<ApiResponse<ChallengeResponse>> cancelChallenge(
            @PathVariable Long challengeId,
            Authentication authentication) {
        String email = authentication.getName();
        ChallengeResponse response = challengeService.cancelChallenge(email, challengeId);
        return ResponseEntity.ok(ApiResponse.success("Challenge cancelled successfully", response));
    }

    /**
     * Retrieves the deterministic question set for an active challenge without answer keys.
     */
    @GetMapping("/{challengeId}/questions")
    public ResponseEntity<ApiResponse<List<ChallengeQuestionResponse>>> getChallengeQuestions(
            @PathVariable Long challengeId,
            Authentication authentication) {
        String email = authentication.getName();
        List<ChallengeQuestionResponse> questions = challengeService.getChallengeQuestions(email, challengeId);
        return ResponseEntity.ok(ApiResponse.success("Challenge questions retrieved successfully", questions));
    }

    /**
     * Submits a participant's answers, evaluates scores, updates challenge completion, and awards XP.
     */
    @PostMapping("/{challengeId}/submit")
    public ResponseEntity<ApiResponse<ChallengeResultResponse>> submitChallenge(
            @PathVariable Long challengeId,
            @Valid @RequestBody SubmitChallengeRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        ChallengeResultResponse response = challengeService.submitChallenge(email, challengeId, request);
        return ResponseEntity.ok(ApiResponse.success("Challenge submitted successfully", response));
    }

    /**
     * Retrieves current challenge results and per-question review for an authorized participant.
     */
    @GetMapping("/{challengeId}/result")
    public ResponseEntity<ApiResponse<ChallengeResultResponse>> getChallengeResult(
            @PathVariable Long challengeId,
            Authentication authentication) {
        String email = authentication.getName();
        ChallengeResultResponse response = challengeService.getChallengeResult(email, challengeId);
        return ResponseEntity.ok(ApiResponse.success("Challenge result retrieved successfully", response));
    }
}
