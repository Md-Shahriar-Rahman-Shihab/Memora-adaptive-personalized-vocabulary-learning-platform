package com.memora.modules.gamification.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.gamification.dto.LeaderboardEntryResponse;
import com.memora.modules.gamification.service.GamificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller exposing deterministic public global leaderboards based on learner XP and streaks.
 */
@RestController
@RequestMapping("/api/v1/leaderboard")
public class LeaderboardController {

    private final GamificationService gamificationService;

    public LeaderboardController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    /**
     * Retrieves the top learners globally.
     *
     * @param limit Maximum number of entries to return (default: 20, max: 100)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaderboardEntryResponse>>> getLeaderboard(
            @RequestParam(defaultValue = "20") int limit) {
        List<LeaderboardEntryResponse> leaderboard = gamificationService.getLeaderboard(limit);
        return ResponseEntity.ok(ApiResponse.success("Leaderboard retrieved successfully", leaderboard));
    }
}
