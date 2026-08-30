package com.memora.modules.vocabulary.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.dto.VocabularyWordRequest;
import com.memora.modules.vocabulary.dto.VocabularyWordResponse;
import com.memora.modules.vocabulary.service.VocabularyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller exposing REST API endpoints for vocabulary catalog search and retrieval.
 */
@RestController
@RequestMapping("/api/v1/vocabulary")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VocabularyWordResponse>>> getAllVocabulary() {
        List<VocabularyWordResponse> words = vocabularyService.getAllVocabulary();
        return ResponseEntity.ok(ApiResponse.success(words));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VocabularyWordResponse>> getWordById(@PathVariable Long id) {
        VocabularyWordResponse response = vocabularyService.getWordById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VocabularyWordResponse>>> searchVocabulary(
            @RequestParam(required = false, defaultValue = "") String query) {
        List<VocabularyWordResponse> results = vocabularyService.searchVocabulary(query);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<ApiResponse<List<VocabularyWordResponse>>> getWordsByDifficulty(
            @PathVariable DifficultyLevel level) {
        List<VocabularyWordResponse> words = vocabularyService.getWordsByDifficulty(level);
        return ResponseEntity.ok(ApiResponse.success(words));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<VocabularyWordResponse>>> getWordsByCategory(
            @PathVariable WordCategory category) {
        List<VocabularyWordResponse> words = vocabularyService.getWordsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(words));
    }

    @GetMapping("/random")
    public ResponseEntity<ApiResponse<List<VocabularyWordResponse>>> getRandomVocabulary(
            @RequestParam(required = false, defaultValue = "10") int limit) {
        List<VocabularyWordResponse> randomWords = vocabularyService.getRandomWords(limit);
        return ResponseEntity.ok(ApiResponse.success(randomWords));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VocabularyWordResponse>> createWord(
            @Valid @RequestBody VocabularyWordRequest request) {
        VocabularyWordResponse response = vocabularyService.createWord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vocabulary word created successfully", response));
    }
}
