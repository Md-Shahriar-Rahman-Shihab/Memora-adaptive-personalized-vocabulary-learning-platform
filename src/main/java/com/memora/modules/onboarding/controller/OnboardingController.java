package com.memora.modules.onboarding.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.onboarding.dto.OnboardingStateResponse;
import com.memora.modules.onboarding.service.OnboardingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller exposing smart onboarding flow state detection.
 * Learner identity is strictly derived from the Spring Security authentication context.
 */
@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    /**
     * Resolves the current learner's onboarding state machine status.
     *
     * @param authentication Spring Security principal
     * @return current onboarding state response
     */
    @GetMapping("/state")
    public ResponseEntity<ApiResponse<OnboardingStateResponse>> getOnboardingState(Authentication authentication) {
        String email = authentication.getName();
        OnboardingStateResponse response = onboardingService.getOnboardingState(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
