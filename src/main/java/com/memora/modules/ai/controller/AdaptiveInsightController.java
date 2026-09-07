package com.memora.modules.ai.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.ai.dto.AdaptiveInsightResponse;
import com.memora.modules.ai.service.AdaptiveInsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller serving deterministic adaptive learning insights,
 * calculated strictly from server-side memory records, quiz history, and learning paths.
 */
@RestController
@RequestMapping("/api/v1/insights")
public class AdaptiveInsightController {

    private final AdaptiveInsightService insightService;

    public AdaptiveInsightController(AdaptiveInsightService insightService) {
        this.insightService = insightService;
    }

    /**
     * Retrieves deterministic adaptive insights and prioritized recommendations for today.
     *
     * @param authentication Authenticated security context
     * @return {@link AdaptiveInsightResponse} wrapped in {@link ApiResponse}
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<AdaptiveInsightResponse>> getTodayInsights(Authentication authentication) {
        String email = authentication.getName();
        AdaptiveInsightResponse response = insightService.getTodayInsights(email);
        return ResponseEntity.ok(ApiResponse.success("Today's adaptive insights retrieved successfully", response));
    }
}
