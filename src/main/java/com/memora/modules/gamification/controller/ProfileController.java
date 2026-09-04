package com.memora.modules.gamification.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.gamification.dto.*;
import com.memora.modules.gamification.service.GamificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller providing authenticated learner profile, statistics, XP history, and badges.
 */
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final GamificationService gamificationService;

    public ProfileController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    /**
     * Retrieves the complete learner profile dashboard summary.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<LearnerProfileResponse>> getProfile(Authentication authentication) {
        String email = authentication.getName();
        LearnerProfileResponse profile = gamificationService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Learner profile retrieved successfully", profile));
    }

    /**
     * Retrieves the learner's auditable XP award transaction history.
     */
    @GetMapping("/xp-history")
    public ResponseEntity<ApiResponse<List<XpTransactionResponse>>> getXpHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        String email = authentication.getName();
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        List<XpTransactionResponse> history = gamificationService.getXpHistory(email, pageable);
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * Retrieves all achievements unlocked by the authenticated learner.
     */
    @GetMapping("/achievements")
    public ResponseEntity<ApiResponse<List<UserAchievementResponse>>> getEarnedAchievements(Authentication authentication) {
        String email = authentication.getName();
        List<UserAchievementResponse> achievements = gamificationService.getAchievements(email);
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }

    /**
     * Retrieves quick learner statistics summary.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<LearnerStatsResponse>> getStats(Authentication authentication) {
        String email = authentication.getName();
        LearnerStatsResponse stats = gamificationService.getStats(email);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
