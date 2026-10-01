package com.memora.modules.partner.challenge.service;

import com.memora.modules.partner.challenge.domain.ChallengeStatus;
import com.memora.modules.partner.challenge.dto.*;

import java.util.List;

/**
 * Service contract orchestrating competitive vocabulary challenges between accepted learning partners.
 */
public interface VocabularyChallengeService {

    /**
     * Initiates a new challenge with an accepted partner.
     */
    ChallengeResponse createChallenge(String currentUserEmail, CreateChallengeRequest request);

    /**
     * Lists all challenges involving the authenticated user, optionally filtered by status.
     */
    List<ChallengeResponse> getMyChallenges(String currentUserEmail, ChallengeStatus status);

    /**
     * Retrieves challenge details for an authorized participant with anti-cheating protection.
     */
    ChallengeResponse getChallenge(String currentUserEmail, Long challengeId);

    /**
     * Accepts a pending challenge (only callable by the challenged partner).
     */
    ChallengeResponse acceptChallenge(String currentUserEmail, Long challengeId);

    /**
     * Declines a pending challenge (only callable by the challenged partner).
     */
    ChallengeResponse declineChallenge(String currentUserEmail, Long challengeId);

    /**
     * Cancels a pending challenge (only callable by the challenger).
     */
    ChallengeResponse cancelChallenge(String currentUserEmail, Long challengeId);

    /**
     * Retrieves the deterministic question set for an accepted challenge without answer keys.
     */
    List<ChallengeQuestionResponse> getChallengeQuestions(String currentUserEmail, Long challengeId);

    /**
     * Evaluates and records a participant's challenge submission, updates progress, and awards XP.
     */
    ChallengeResultResponse submitChallenge(String currentUserEmail, Long challengeId, SubmitChallengeRequest request);

    /**
     * Retrieves the current result summary for a participant.
     */
    ChallengeResultResponse getChallengeResult(String currentUserEmail, Long challengeId);
}
