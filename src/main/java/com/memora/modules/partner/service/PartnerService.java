package com.memora.modules.partner.service;

import com.memora.modules.partner.dto.PartnerProgressResponse;
import com.memora.modules.partner.dto.PartnerRequestResponse;
import com.memora.modules.partner.dto.PartnerRequestsSummaryResponse;
import com.memora.modules.partner.dto.PartnerUserSummaryResponse;
import com.memora.modules.partner.dto.SendPartnerRequestDto;

import java.util.List;

/**
 * Service contract defining operations for learning partner relationships,
 * user search, request dispatch, request resolution, active partner queries,
 * and privacy-safe partner progress sharing.
 */
public interface PartnerService {

    /**
     * Searches for registered learners by name, excluding the current authenticated user.
     *
     * @param currentUserEmail Authenticated user's email
     * @param query Search keyword
     * @return Privacy-safe summary list of matching learners
     */
    List<PartnerUserSummaryResponse> searchUsers(String currentUserEmail, String query);

    /**
     * Searches for a registered learner by exact normalized email address,
     * excluding the authenticated user.
     *
     * @param currentUserEmail Authenticated user's email
     * @param targetEmail Email address to search
     * @return Privacy-safe user summary with relationship status
     */
    PartnerUserSummaryResponse searchByEmail(String currentUserEmail, String targetEmail);

    /**
     * Dispatches a learning partner request from the authenticated user to a target user.
     *
     * @param currentUserEmail Authenticated user's email
     * @param dto Request containing target user ID
     * @return Created or renewed partner request response
     */
    PartnerRequestResponse sendPartnerRequest(String currentUserEmail, SendPartnerRequestDto dto);

    /**
     * Accepts a pending partner request by ID. Only the recipient can accept.
     *
     * @param currentUserEmail Authenticated recipient's email
     * @param requestId Relationship ID
     * @return Summary response of the new learning partner
     */
    PartnerUserSummaryResponse acceptRequest(String currentUserEmail, Long requestId);

    /**
     * Rejects a pending partner request by ID. Only the recipient can reject.
     *
     * @param currentUserEmail Authenticated recipient's email
     * @param requestId Relationship ID
     * @return Updated partner request response
     */
    PartnerRequestResponse rejectRequest(String currentUserEmail, Long requestId);

    /**
     * Cancels an outgoing pending partner request by ID. Only the original sender can cancel.
     *
     * @param currentUserEmail Authenticated sender's email
     * @param requestId Relationship ID
     * @return Updated partner request response
     */
    PartnerRequestResponse cancelRequest(String currentUserEmail, Long requestId);

    /**
     * Retrieves all active accepted learning partners for the authenticated user.
     *
     * @param currentUserEmail Authenticated user's email
     * @return List of active learning partners
     */
    List<PartnerUserSummaryResponse> getActivePartners(String currentUserEmail);

    /**
     * Retrieves all pending incoming and outgoing partner requests for the authenticated user.
     *
     * @param currentUserEmail Authenticated user's email
     * @return Incoming and outgoing request lists
     */
    PartnerRequestsSummaryResponse getPartnerRequests(String currentUserEmail);

    /**
     * Retrieves the privacy-safe learning progress of an accepted learning partner.
     * Only callable if an active ACCEPTED relationship exists between the users.
     *
     * @param currentUserEmail Authenticated user's email
     * @param partnerId Target partner user ID
     * @return Privacy-safe learning progress summary
     */
    PartnerProgressResponse getPartnerProgress(String currentUserEmail, Long partnerId);
}
