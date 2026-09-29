package com.memora.modules.ai.controller;

import com.memora.common.exception.BadRequestException;
import com.memora.common.response.ApiResponse;
import com.memora.modules.ai.dto.*;
import com.memora.modules.ai.service.AIExplanationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing AI vocabulary explanation, example generation,
 * memory mnemonic tips, and contextual usage endpoints.
 *
 * All requests resolve user identity strictly from Spring Security Authentication.
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AiController {

    private final AIExplanationService aiExplanationService;

    public AiController(AIExplanationService aiExplanationService) {
        this.aiExplanationService = aiExplanationService;
    }

    /**
     * Generates a CEFR-adapted explanation for a vocabulary word.
     */
    @PostMapping("/word-explanation")
    public ResponseEntity<ApiResponse<AiExplanationResponse>> explainWord(
            @Valid @RequestBody AiExplanationRequest request,
            Authentication authentication) {
        if (!request.isValid()) {
            throw new BadRequestException("Either wordId or word text must be provided");
        }
        String email = authentication.getName();
        AiExplanationResponse response = aiExplanationService.explainWord(email, request);
        return ResponseEntity.ok(ApiResponse.success("AI vocabulary explanation generated successfully", response));
    }

    /**
     * Generates a natural example sentence tailored to the learner's level.
     */
    @PostMapping("/example")
    public ResponseEntity<ApiResponse<AiExampleResponse>> generateExample(
            @Valid @RequestBody AiExampleRequest request,
            Authentication authentication) {
        if (!request.isValid()) {
            throw new BadRequestException("Either wordId or word text must be provided");
        }
        String email = authentication.getName();
        AiExampleResponse response = aiExplanationService.generateExample(email, request);
        return ResponseEntity.ok(ApiResponse.success("AI example sentence generated successfully", response));
    }

    /**
     * Generates a mnemonic retention tip or memory association.
     */
    @PostMapping("/memory-tip")
    public ResponseEntity<ApiResponse<AiMemoryTipResponse>> generateMemoryTip(
            @Valid @RequestBody AiMemoryTipRequest request,
            Authentication authentication) {
        if (!request.isValid()) {
            throw new BadRequestException("Either wordId or word text must be provided");
        }
        String email = authentication.getName();
        AiMemoryTipResponse response = aiExplanationService.generateMemoryTip(email, request);
        return ResponseEntity.ok(ApiResponse.success("AI memory tip generated successfully", response));
    }

    /**
     * Explains practical contextual usage, formal/informal register, and collocations.
     */
    @PostMapping("/contextual-usage")
    public ResponseEntity<ApiResponse<AiUsageResponse>> explainUsage(
            @Valid @RequestBody AiUsageRequest request,
            Authentication authentication) {
        if (!request.isValid()) {
            throw new BadRequestException("Either wordId or word text must be provided");
        }
        String email = authentication.getName();
        AiUsageResponse response = aiExplanationService.explainUsage(email, request);
        return ResponseEntity.ok(ApiResponse.success("AI contextual usage generated successfully", response));
    }

    /**
     * Generates or derives verified linguistic relationships (synonyms, antonyms, word family).
     */
    @PostMapping("/word-relations")
    public ResponseEntity<ApiResponse<AiWordRelationsResponse>> getWordRelations(
            @Valid @RequestBody AiWordRelationsRequest request,
            Authentication authentication) {
        if (!request.isValid()) {
            throw new BadRequestException("Either wordId or word text must be provided");
        }
        String email = authentication != null ? authentication.getName() : "guest";
        AiWordRelationsResponse response = aiExplanationService.getWordRelations(email, request);
        return ResponseEntity.ok(ApiResponse.success("AI word relations generated successfully", response));
    }
}
