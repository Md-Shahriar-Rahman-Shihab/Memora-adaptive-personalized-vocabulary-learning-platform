package com.memora.modules.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.ai.cache.AiResponseCache;
import com.memora.modules.ai.config.AiConfig;
import com.memora.modules.ai.dto.*;
import com.memora.modules.ai.provider.AiGenerationResult;
import com.memora.modules.ai.provider.AiProvider;
import com.memora.modules.ai.provider.GeminiAiProvider;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Primary implementation of {@link AIExplanationService} interacting with AI providers
 * through {@link AiProvider} (orchestrated by {@code AiProviderRouter}), augmented with local caching,
 * quality prompt calibration, linguistic validation, and guaranteed automatic delegation to {@link FallbackExplanationService}.
 */
@Primary
@Service("geminiExplanationService")
public class GeminiExplanationService implements AIExplanationService {

    private static final Logger log = LoggerFactory.getLogger(GeminiExplanationService.class);

    private final AiConfig aiConfig;
    private final AiProvider aiProvider;
    private final FallbackExplanationService fallbackService;
    private final AiResponseCache responseCache;
    private final VocabularyWordRepository wordRepository;
    private final UserWordProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiExplanationService(
            AiConfig aiConfig,
            AiProvider aiProvider,
            FallbackExplanationService fallbackService,
            AiResponseCache responseCache,
            VocabularyWordRepository wordRepository,
            UserWordProgressRepository progressRepository,
            UserRepository userRepository) {
        this.aiConfig = aiConfig;
        this.aiProvider = aiProvider;
        this.fallbackService = fallbackService;
        this.responseCache = responseCache;
        this.wordRepository = wordRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    private boolean isProviderConfigured() {
        return aiConfig.isAiConfigured() || aiConfig.isGeminiConfigured();
    }

    @Override
    public AiExplanationResponse explainWord(String userEmail, AiExplanationRequest request) {
        WordMetadata meta = resolveMetadata(userEmail, request.wordId(), request.word(), request.cefrLevel());

        String cacheKey = "explain:" + meta.wordText().toLowerCase() + ":" + meta.cefrLevel() + ":" + meta.isStruggling();
        AiExplanationResponse cached = responseCache.get(cacheKey, AiExplanationResponse.class);
        if (cached != null) {
            return new AiExplanationResponse(
                    cached.word(),
                    cached.cefrLevel(),
                    cached.explanation(),
                    cached.breakdown(),
                    cached.provider(),
                    cached.model(),
                    cached.isFallback(),
                    true
            );
        }

        if (!isProviderConfigured()) {
            log.info("[AI-FLOW] AI provider is NOT configured. Delegating to FallbackExplanationService.");
            return fallbackService.explainWord(userEmail, request);
        }

        try {
            log.info("[AI-FLOW] AI provider IS configured. Building prompt for word: {} (CEFR {})", meta.wordText(), meta.cefrLevel());
            String prompt = AiPromptBuilder.buildExplanationPrompt(
                    meta.wordText(), meta.cefrLevel(), meta.meaning(), meta.category(), meta.isStruggling()
            );

            log.info("[AI-FLOW] Calling aiProvider.generate...");
            AiGenerationResult aiResult = aiProvider.generate(prompt);
            log.info("[AI-FLOW] aiProvider.generate completed. Provider: {}, Model: {}, Fallback: {}",
                    aiResult != null ? aiResult.provider() : "null",
                    aiResult != null ? aiResult.model() : "null",
                    aiResult != null ? aiResult.isFallback() : "null");

            if (aiResult == null || aiResult.isFallback() || aiResult.text() == null || aiResult.text().isBlank() || aiResult.text().startsWith("Deterministic educational")) {
                log.info("[AI-FLOW] Generated content was fallback/empty. Delegating to FallbackExplanationService.");
                return fallbackService.explainWord(userEmail, request);
            }

            String breakdown = String.format("CEFR %s • %s", meta.cefrLevel(), meta.category().replace('_', ' '));
            AiExplanationResponse response = new AiExplanationResponse(
                    meta.wordText(),
                    meta.cefrLevel(),
                    aiResult.text().trim(),
                    breakdown,
                    aiResult.provider(),
                    aiResult.model(),
                    false,
                    false
            );

            responseCache.put(cacheKey, response);
            return response;

        } catch (Exception e) {
            log.warn("AI explanation generation encountered an issue: {}. Using fallback service.", e.getMessage());
            return fallbackService.explainWord(userEmail, request);
        }
    }

    @Override
    public AiExampleResponse generateExample(String userEmail, AiExampleRequest request) {
        WordMetadata meta = resolveMetadata(userEmail, request.wordId(), request.word(), request.cefrLevel());

        String cacheKey = "example:" + meta.wordText().toLowerCase() + ":" + meta.cefrLevel();
        AiExampleResponse cached = responseCache.get(cacheKey, AiExampleResponse.class);
        if (cached != null) {
            return new AiExampleResponse(
                    cached.word(),
                    cached.cefrLevel(),
                    cached.exampleSentence(),
                    cached.context(),
                    cached.provider(),
                    cached.model(),
                    cached.isFallback(),
                    true
            );
        }

        if (!isProviderConfigured()) {
            return fallbackService.generateExample(userEmail, request);
        }

        try {
            String prompt = AiPromptBuilder.buildExamplePrompt(
                    meta.wordText(), meta.cefrLevel(), meta.meaning(), meta.category()
            );

            AiGenerationResult aiResult = aiProvider.generate(prompt);
            if (aiResult == null || aiResult.isFallback() || aiResult.text() == null || aiResult.text().isBlank() || aiResult.text().startsWith("Deterministic educational")) {
                return fallbackService.generateExample(userEmail, request);
            }

            String sentence = aiResult.text().trim().replaceAll("^\"|\"$", "").trim();
            String contextDesc = String.format("Natural usage in %s context for CEFR %s.",
                    meta.category().replace('_', ' ').toLowerCase(), meta.cefrLevel());

            AiExampleResponse response = new AiExampleResponse(
                    meta.wordText(),
                    meta.cefrLevel(),
                    sentence,
                    contextDesc,
                    aiResult.provider(),
                    aiResult.model(),
                    false,
                    false
            );

            responseCache.put(cacheKey, response);
            return response;

        } catch (Exception e) {
            log.warn("AI example generation failed: {}. Using fallback service.", e.getMessage());
            return fallbackService.generateExample(userEmail, request);
        }
    }

    @Override
    public AiMemoryTipResponse generateMemoryTip(String userEmail, AiMemoryTipRequest request) {
        WordMetadata meta = resolveMetadata(userEmail, request.wordId(), request.word(), request.cefrLevel());

        String cacheKey = "tip:" + meta.wordText().toLowerCase() + ":" + meta.cefrLevel() + ":" + meta.isStruggling();
        AiMemoryTipResponse cached = responseCache.get(cacheKey, AiMemoryTipResponse.class);
        if (cached != null) {
            return new AiMemoryTipResponse(
                    cached.word(),
                    cached.cefrLevel(),
                    cached.memoryTip(),
                    cached.association(),
                    cached.provider(),
                    cached.model(),
                    cached.isFallback(),
                    true
            );
        }

        if (!isProviderConfigured()) {
            return fallbackService.generateMemoryTip(userEmail, request);
        }

        try {
            String prompt = AiPromptBuilder.buildMemoryTipPrompt(
                    meta.wordText(), meta.cefrLevel(), meta.meaning(), meta.category(), meta.isStruggling()
            );

            AiGenerationResult aiResult = aiProvider.generate(prompt);
            if (aiResult == null || aiResult.isFallback() || aiResult.text() == null || aiResult.text().isBlank() || aiResult.text().startsWith("Deterministic educational")) {
                return fallbackService.generateMemoryTip(userEmail, request);
            }

            String association = String.format("Memory hook linked to '%s' (%s)", meta.meaning(), meta.category().replace('_', ' '));
            AiMemoryTipResponse response = new AiMemoryTipResponse(
                    meta.wordText(),
                    meta.cefrLevel(),
                    aiResult.text().trim(),
                    association,
                    aiResult.provider(),
                    aiResult.model(),
                    false,
                    false
            );

            responseCache.put(cacheKey, response);
            return response;

        } catch (Exception e) {
            log.warn("AI memory tip generation failed: {}. Using fallback service.", e.getMessage());
            return fallbackService.generateMemoryTip(userEmail, request);
        }
    }

    @Override
    public AiUsageResponse explainUsage(String userEmail, AiUsageRequest request) {
        WordMetadata meta = resolveMetadata(userEmail, request.wordId(), request.word(), request.cefrLevel());

        String cacheKey = "usage:" + meta.wordText().toLowerCase() + ":" + meta.cefrLevel();
        AiUsageResponse cached = responseCache.get(cacheKey, AiUsageResponse.class);
        if (cached != null) {
            return new AiUsageResponse(
                    cached.word(),
                    cached.cefrLevel(),
                    cached.usageNotes(),
                    cached.collocations(),
                    cached.register(),
                    cached.provider(),
                    cached.model(),
                    cached.isFallback(),
                    true
            );
        }

        if (!isProviderConfigured()) {
            return fallbackService.explainUsage(userEmail, request);
        }

        try {
            String prompt = AiPromptBuilder.buildContextualUsagePrompt(
                    meta.wordText(), meta.cefrLevel(), meta.meaning(), meta.category()
            );

            AiGenerationResult aiResult = aiProvider.generate(prompt);
            if (aiResult == null || aiResult.isFallback() || aiResult.text() == null || aiResult.text().isBlank() || aiResult.text().startsWith("Deterministic educational")) {
                return fallbackService.explainUsage(userEmail, request);
            }

            String rawJson = CollocationValidator.cleanJsonText(aiResult.text());
            String usageNotes = null;
            String rawRegister = null;
            List<String> rawCollocations = new ArrayList<>();

            try {
                JsonNode root = objectMapper.readTree(rawJson);
                if (root.has("usageNotes")) {
                    usageNotes = root.get("usageNotes").asText();
                }
                if (root.has("register")) {
                    rawRegister = root.get("register").asText();
                }
                if (root.has("collocations") && root.get("collocations").isArray()) {
                    for (JsonNode colNode : root.get("collocations")) {
                        rawCollocations.add(colNode.asText());
                    }
                }
            } catch (Exception parseEx) {
                log.debug("Could not parse contextual usage as JSON, extracting text directly: {}", parseEx.getMessage());
                usageNotes = aiResult.text().trim();
            }

            if (usageNotes == null || usageNotes.isBlank()) {
                usageNotes = aiResult.text().trim();
            }

            String register = CollocationValidator.normalizeRegister(rawRegister, meta.cefrLevel());
            List<String> validatedCollocations = CollocationValidator.validateCollocations(
                    rawCollocations, meta.wordText(), meta.cefrLevel()
            );

            AiUsageResponse response = new AiUsageResponse(
                    meta.wordText(),
                    meta.cefrLevel(),
                    usageNotes,
                    validatedCollocations,
                    register,
                    aiResult.provider(),
                    aiResult.model(),
                    false,
                    false
            );

            responseCache.put(cacheKey, response);
            return response;

        } catch (Exception e) {
            log.warn("AI usage explanation failed: {}. Using fallback service.", e.getMessage());
            return fallbackService.explainUsage(userEmail, request);
        }
    }

    private WordMetadata resolveMetadata(String userEmail, Long wordId, String rawWord, String requestedLevel) {
        Optional<VocabularyWord> wordOpt = Optional.empty();
        if (wordId != null && wordId > 0) {
            wordOpt = wordRepository.findById(wordId);
        }
        if (wordOpt.isEmpty() && rawWord != null && !rawWord.isBlank()) {
            wordOpt = wordRepository.findByWordIgnoreCase(rawWord.trim());
        }

        String wordText = rawWord != null && !rawWord.isBlank() ? rawWord.trim() : "word";
        String meaning = "vocabulary meaning";
        String category = "GENERAL";
        String cefrLevel = requestedLevel != null && !requestedLevel.isBlank() ? requestedLevel.trim().toUpperCase() : "B1";

        if (wordOpt.isPresent()) {
            VocabularyWord vw = wordOpt.get();
            wordText = vw.getWord();
            meaning = vw.getMeaning() != null ? vw.getMeaning() : meaning;
            category = vw.getCategory() != null ? vw.getCategory().name() : category;
            if (requestedLevel == null || requestedLevel.isBlank()) {
                cefrLevel = vw.getDifficultyLevel() != null ? vw.getDifficultyLevel().name() : cefrLevel;
            }
        }

        boolean isStruggling = false;
        if (userEmail != null && !userEmail.isBlank()) {
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if ((requestedLevel == null || requestedLevel.isBlank()) && wordOpt.isEmpty() && user.getCurrentLevel() != null) {
                    cefrLevel = user.getCurrentLevel().name();
                }

                if (wordOpt.isPresent()) {
                    Optional<UserWordProgress> progOpt = progressRepository.findByUserAndVocabularyWord(user, wordOpt.get());
                    if (progOpt.isPresent()) {
                        UserWordProgress prog = progOpt.get();
                        if (prog.getForgettingRisk() != null &&
                                ("HIGH".equalsIgnoreCase(prog.getForgettingRisk().name()) ||
                                 "CRITICAL".equalsIgnoreCase(prog.getForgettingRisk().name()))) {
                            isStruggling = true;
                        }
                    }
                }
            }
        }

        return new WordMetadata(wordText, meaning, category, cefrLevel, isStruggling);
    }

    private record WordMetadata(
            String wordText,
            String meaning,
            String category,
            String cefrLevel,
            boolean isStruggling
    ) {}
}
