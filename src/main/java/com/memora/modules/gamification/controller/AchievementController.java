package com.memora.modules.gamification.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.gamification.entity.Achievement;
import com.memora.modules.gamification.service.AchievementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller providing access to the catalog of available system achievements.
 */
@RestController
@RequestMapping("/api/v1/achievements")
public class AchievementController {

    private final AchievementService achievementService;

    public AchievementController(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    /**
     * Retrieves all active achievements in the catalog.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Achievement>>> getAllAchievements() {
        List<Achievement> achievements = achievementService.getAllActiveAchievements();
        return ResponseEntity.ok(ApiResponse.success(achievements));
    }
}
