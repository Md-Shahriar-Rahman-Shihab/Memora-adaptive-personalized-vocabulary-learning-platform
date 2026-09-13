package com.memora.modules.dictionary.controller;

import com.memora.common.response.ApiResponse;
import com.memora.modules.dictionary.dto.DictionaryResponse;
import com.memora.modules.dictionary.service.DictionaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing authenticated dictionary search endpoints backed by the
 * Merriam-Webster Collegiate Dictionary API.
 */
@RestController
@RequestMapping("/api/v1/dictionary")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Looks up definitions, pronunciation, audio, examples, and etymology for a given word.
     *
     * @param word Search word
     * @return Standard ApiResponse enclosing the DictionaryResponse
     */
    @GetMapping("/{word}")
    public ResponseEntity<ApiResponse<DictionaryResponse>> getWordDetails(@PathVariable String word) {
        DictionaryResponse response = dictionaryService.lookupWord(word);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
