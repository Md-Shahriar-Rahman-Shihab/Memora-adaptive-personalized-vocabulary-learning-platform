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

import java.util.*;

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

    @Override
    public AiWordRelationsResponse getWordRelations(String userEmail, AiWordRelationsRequest request) {
        WordMetadata meta = resolveMetadata(userEmail, request.wordId(), request.word(), request.cefrLevel());

        String pos = request.partOfSpeech() != null && !request.partOfSpeech().isBlank()
                ? request.partOfSpeech().trim().toLowerCase(java.util.Locale.ENGLISH)
                : meta.category().toLowerCase(java.util.Locale.ENGLISH);

        String definition = request.definition() != null && !request.definition().isBlank()
                ? request.definition().trim()
                : meta.meaning();

        String cacheKey = "relations:" + meta.wordText().toLowerCase(java.util.Locale.ENGLISH)
                + ":" + (pos != null && !pos.isBlank() ? pos : "all")
                + ":" + meta.cefrLevel().toLowerCase(java.util.Locale.ENGLISH);

        AiWordRelationsResponse cached = responseCache.get(cacheKey, AiWordRelationsResponse.class);
        if (cached != null) {
            return new AiWordRelationsResponse(
                    cached.word(),
                    cached.synonyms(),
                    cached.antonyms(),
                    cached.wordFamily(),
                    cached.provider(),
                    cached.model(),
                    cached.isFallback(),
                    true
            );
        }

        if (!isProviderConfigured()) {
            return fallbackService.getWordRelations(userEmail, request);
        }

        try {
            String posInstruction = (pos != null && !pos.isBlank() && !"general".equalsIgnoreCase(pos))
                    ? "Target Part of Speech: " + pos + "\n"
                    : "";
            String defInstruction = (definition != null && !definition.isBlank() && !"vocabulary meaning".equalsIgnoreCase(definition))
                    ? "Target Sense / Definition: " + definition + "\n"
                    : "";

            String prompt = """
                    You are an expert English lexicographer and linguist.
                    For the target English word "%s" (CEFR level: %s), provide verified linguistic relationships.
                    %s%s
                    Return ONLY a JSON object in this exact format:
                    {
                      "synonyms": ["synonym1", "synonym2", "synonym3", "synonym4"],
                      "antonyms": ["antonym1", "antonym2", "antonym3"],
                      "wordFamily": {
                        "noun": "...",
                        "verb": "...",
                        "adjective": "...",
                        "adverb": "..."
                      }
                    }
                    Strict Rules:
                    1. Sense and part of speech: All synonyms and antonyms MUST strictly match the specified part of speech and definition sense.
                    2. Genuine synonyms and antonyms only: Do NOT label morphological derivatives as synonyms (e.g. do not list a noun form as a synonym of an adjective).
                    3. Do NOT mechanically fabricate words by adding or removing prefixes like un-, in-, dis- unless they are standard, real dictionary words.
                    4. Concrete nouns and words without natural opposites (e.g., "computer", "desk", "water", "tree") MUST have an empty array for antonyms: "antonyms": [].
                    5. Never include the target word itself in synonyms or antonyms.
                    6. Provide 3 to 6 high-quality, genuine synonyms.
                    7. Each list item must be a single word or established compound word. Do NOT include phrases, sentences, or explanatory notes.
                    8. Word family: Fill in only genuine, standard English derivations. If a form does not exist in standard English, omit its key or set to null.
                    9. Return ONLY valid JSON, without markdown formatting or commentary.
                    """.formatted(meta.wordText(), meta.cefrLevel(), posInstruction, defInstruction);

            AiGenerationResult aiResult = aiProvider.generate(prompt);
            if (aiResult == null || aiResult.isFallback() || aiResult.text() == null || aiResult.text().isBlank()) {
                return fallbackService.getWordRelations(userEmail, request);
            }

            String rawJson = CollocationValidator.cleanJsonText(aiResult.text());
            Set<String> cleanSynonyms = new LinkedHashSet<>();
            Set<String> cleanAntonyms = new LinkedHashSet<>();
            Map<String, String> wordFamily = new LinkedHashMap<>();
            String targetLower = meta.wordText().toLowerCase(java.util.Locale.ENGLISH).trim();

            try {
                JsonNode root = objectMapper.readTree(rawJson);
                if (root.has("synonyms") && root.get("synonyms").isArray()) {
                    for (JsonNode syn : root.get("synonyms")) {
                        String s = sanitizeRelationItem(syn.asText(""), targetLower);
                        if (isValidRelationItem(s, targetLower)) {
                            cleanSynonyms.add(s);
                            if (cleanSynonyms.size() >= 6) break;
                        }
                    }
                }
                if (root.has("antonyms") && root.get("antonyms").isArray()) {
                    for (JsonNode ant : root.get("antonyms")) {
                        String a = sanitizeRelationItem(ant.asText(""), targetLower);
                        if (isValidRelationItem(a, targetLower) && !cleanSynonyms.contains(a)) {
                            cleanAntonyms.add(a);
                            if (cleanAntonyms.size() >= 6) break;
                        }
                    }
                }
                if (root.has("wordFamily") && root.get("wordFamily").isObject()) {
                    JsonNode famNode = root.get("wordFamily");
                    for (String posKey : List.of("noun", "verb", "adjective", "adverb")) {
                        if (famNode.hasNonNull(posKey)) {
                            String val = sanitizeRelationItem(famNode.get(posKey).asText(""), null);
                            if (val != null && isValidFamilyItem(val)) {
                                wordFamily.put(posKey, val);
                            }
                        }
                    }
                }
            } catch (Exception parseEx) {
                log.debug("Failed parsing AI word relations JSON: {}", parseEx.getMessage());
                return fallbackService.getWordRelations(userEmail, request);
            }

            // If both synonyms and wordFamily are completely empty from AI, fall back safely
            if (cleanSynonyms.isEmpty() && wordFamily.isEmpty()) {
                return fallbackService.getWordRelations(userEmail, request);
            }

            AiWordRelationsResponse response = new AiWordRelationsResponse(
                    meta.wordText(),
                    new ArrayList<>(cleanSynonyms),
                    new ArrayList<>(cleanAntonyms),
                    wordFamily,
                    aiResult.provider(),
                    aiResult.model(),
                    false,
                    false
            );

            responseCache.put(cacheKey, response);
            return response;

        } catch (Exception e) {
            log.warn("AI word relations generation failed: {}. Falling back.", e.getMessage());
            return fallbackService.getWordRelations(userEmail, request);
        }
    }

    private String sanitizeRelationItem(String raw, String targetWord) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.replaceAll("\\s*\\([^)]*\\)", "")
                .replaceAll("^[0-9]+[.)]\\s*", "")
                .replaceAll("^[-*•]\\s*", "")
                .replaceAll("[^\\p{L}\\s'’-]", "")
                .trim()
                .toLowerCase(java.util.Locale.ENGLISH);

        if (cleaned.isEmpty()) return null;
        if (targetWord != null && cleaned.equalsIgnoreCase(targetWord)) return null;
        return cleaned;
    }

    private boolean isValidRelationItem(String item, String targetWord) {
        if (item == null || item.isBlank()) return false;
        if (targetWord != null && item.equalsIgnoreCase(targetWord)) return false;
        if (item.length() < 2 || item.length() > 30) return false;
        if (item.split("\\s+").length > 2) return false;
        if (item.equalsIgnoreCase("none") || item.equalsIgnoreCase("n/a")
                || item.contains("no antonym") || item.contains("not applicable")) {
            return false;
        }
        return true;
    }

    private boolean isValidFamilyItem(String item) {
        if (item == null || item.isBlank()) return false;
        if (item.length() < 2 || item.length() > 30) return false;
        return item.matches("^[\\p{L}'’-]+$");
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
