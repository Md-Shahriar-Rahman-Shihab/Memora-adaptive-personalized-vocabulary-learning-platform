package com.memora.modules.partner.service;

import com.memora.modules.partner.dto.PartnerLeaderboardEntryResponse;

import java.util.List;

/**
 * Service contract defining operations for pair-only learning partner leaderboard.
 */
public interface PartnerLeaderboardService {

    /**
     * Retrieves the pair-only leaderboard for the authenticated user and their accepted partners.
     * Excludes unrelated users and non-accepted partner relationships.
     *
     * @param currentUserEmail Authenticated user's email
     * @return Sorted list of leaderboard entries ranked by Total XP descending
     */
    List<PartnerLeaderboardEntryResponse> getPartnerLeaderboard(String currentUserEmail);
}
