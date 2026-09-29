package com.memora.modules.ai.service;

import com.memora.modules.ai.dto.*;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.entity.UserWordProgress;
import com.memora.modules.vocabulary.entity.VocabularyWord;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Deterministic, offline-capable implementation of {@link AIExplanationService}.
 * Produces CEFR-tailored educational explanations, contextual examples,
 * mnemonic memory hooks, and usage collocations derived from verified dictionary metadata.
 */
@Service("fallbackExplanationService")
public class FallbackExplanationService implements AIExplanationService {

    private static final Logger log = LoggerFactory.getLogger(FallbackExplanationService.class);

    private final VocabularyWordRepository wordRepository;
    private final UserWordProgressRepository progressRepository;
    private final UserRepository userRepository;

    public FallbackExplanationService(
            VocabularyWordRepository wordRepository,
            UserWordProgressRepository progressRepository,
            UserRepository userRepository) {
        this.wordRepository = wordRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AiExplanationResponse explainWord(String userEmail, AiExplanationRequest request) {
        WordContext context = resolveWordContext(userEmail, request.wordId(), request.word(), request.cefrLevel());

        String explanation;
        String breakdown;

        if (isBeginnerLevel(context.cefrLevel)) {
            explanation = String.format("'%s' means %s. It is an everyday word used in basic conversation.",
                    context.wordText, context.meaning.toLowerCase());
            breakdown = String.format("Simple definition: %s. Use it when speaking simply and clearly.",
                    context.definition);
        } else {
            explanation = String.format("'%s' is a CEFR %s word denoting '%s'. %s",
                    context.wordText, context.cefrLevel, context.meaning, context.definition);
            breakdown = String.format("Category: %s. Nuance: Expresses '%s' with heightened semantic accuracy.",
                    context.category.replace('_', ' '), context.meaning.toLowerCase());
        }

        return new AiExplanationResponse(
                context.wordText,
                context.cefrLevel,
                explanation,
                breakdown,
                "fallback",
                null,
                true,
                false
        );
    }

    @Override
    public AiExampleResponse generateExample(String userEmail, AiExampleRequest request) {
        WordContext context = resolveWordContext(userEmail, request.wordId(), request.word(), request.cefrLevel());

        String example;
        if (context.exampleSentence != null && !context.exampleSentence.isBlank()) {
            example = context.exampleSentence;
        } else if (isBeginnerLevel(context.cefrLevel)) {
            example = String.format("She used a %s approach to solve the daily puzzle.", context.wordText.toLowerCase());
        } else {
            example = String.format("The research team adopted a %s methodology to ensure comprehensive evaluation.",
                    context.wordText.toLowerCase());
        }

        String contextDescription = String.format("Appropriate for CEFR %s conversations regarding %s.",
                context.cefrLevel, context.category.replace('_', ' ').toLowerCase());

        return new AiExampleResponse(
                context.wordText,
                context.cefrLevel,
                example,
                contextDescription,
                "fallback",
                null,
                true,
                false
        );
    }

    @Override
    public AiMemoryTipResponse generateMemoryTip(String userEmail, AiMemoryTipRequest request) {
        WordContext context = resolveWordContext(userEmail, request.wordId(), request.word(), request.cefrLevel());

        String memoryTip;
        String association;

        if (context.isStruggling) {
            memoryTip = String.format(
                    "High retention priority: Associate '%s' with a vivid picture of '%s'. Say it aloud 3 times: '%s' = '%s'.",
                    context.wordText, context.meaning, context.wordText, context.meaning
            );
            association = String.format("Visual Cue: An eye-catching symbol representing %s in action.", context.meaning.toLowerCase());
        } else {
            memoryTip = String.format(
                    "Retention Hook: Break '%s' into recognizable syllables and link it to the core idea of '%s'.",
                    context.wordText, context.meaning
            );
            association = String.format("Semantic link: Key concept '%s' under the %s category.",
                    context.meaning, context.category.replace('_', ' '));
        }

        return new AiMemoryTipResponse(
                context.wordText,
                context.cefrLevel,
                memoryTip,
                association,
                "fallback",
                null,
                true,
                false
        );
    }

    @Override
    public AiUsageResponse explainUsage(String userEmail, AiUsageRequest request) {
        WordContext context = resolveWordContext(userEmail, request.wordId(), request.word(), request.cefrLevel());

        String rawRegister;
        if ("ACADEMIC".equalsIgnoreCase(context.category)) {
            rawRegister = "Academic";
        } else if (isBeginnerLevel(context.cefrLevel)) {
            rawRegister = "General Conversational";
        } else {
            rawRegister = "Formal";
        }
        String register = CollocationValidator.normalizeRegister(rawRegister, context.cefrLevel);
        String usageNotes = String.format(
                "'%s' is most commonly used as a %s descriptor in %s contexts.",
                context.wordText,
                isBeginnerLevel(context.cefrLevel) ? "conversational" : "formal",
                context.category.replace('_', ' ').toLowerCase()
        );

        List<String> collocations = CollocationValidator.validateCollocations(null, context.wordText, context.cefrLevel);

        return new AiUsageResponse(
                context.wordText,
                context.cefrLevel,
                usageNotes,
                collocations,
                register,
                "fallback",
                null,
                true,
                false
        );
    }

    private boolean isBeginnerLevel(String level) {
        return "A1".equalsIgnoreCase(level) || "A2".equalsIgnoreCase(level);
    }

    private WordContext resolveWordContext(String userEmail, Long wordId, String rawWord, String requestedLevel) {
        Optional<VocabularyWord> wordOpt = Optional.empty();
        if (wordId != null && wordId > 0) {
            wordOpt = wordRepository.findById(wordId);
        }
        if (wordOpt.isEmpty() && rawWord != null && !rawWord.isBlank()) {
            wordOpt = wordRepository.findByWordIgnoreCase(rawWord.trim());
        }

        String wordText = rawWord != null && !rawWord.isBlank() ? rawWord.trim() : "word";
        String meaning = "vocabulary meaning";
        String definition = "detailed vocabulary definition";
        String category = "GENERAL";
        String exampleSentence = null;
        String cefrLevel = requestedLevel != null && !requestedLevel.isBlank() ? requestedLevel.trim().toUpperCase() : "B1";

        if (wordOpt.isPresent()) {
            VocabularyWord vw = wordOpt.get();
            wordText = vw.getWord();
            meaning = vw.getMeaning() != null ? vw.getMeaning() : meaning;
            definition = vw.getDefinition() != null ? vw.getDefinition() : meaning;
            category = vw.getCategory() != null ? vw.getCategory().name() : category;
            exampleSentence = vw.getExampleSentence();
            if (requestedLevel == null || requestedLevel.isBlank()) {
                cefrLevel = vw.getDifficultyLevel() != null ? vw.getDifficultyLevel().name() : cefrLevel;
            }
        }

        // Check user level and struggling state
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

        return new WordContext(wordText, meaning, definition, category, exampleSentence, cefrLevel, isStruggling);
    }

    @Override
    public AiWordRelationsResponse getWordRelations(String userEmail, AiWordRelationsRequest request) {
        WordContext context = resolveWordContext(userEmail, request.wordId(), request.word(), request.cefrLevel());
        String target = context.wordText.toLowerCase(java.util.Locale.ENGLISH).trim();

        List<String> synonyms = new ArrayList<>();
        List<String> antonyms = new ArrayList<>();
        java.util.Map<String, String> family = new java.util.LinkedHashMap<>();

        switch (target) {
            case "meticulous" -> {
                synonyms.addAll(List.of("careful", "precise", "thorough", "painstaking", "scrupulous"));
                antonyms.addAll(List.of("careless", "sloppy", "negligent"));
                family.put("noun", "meticulousness");
                family.put("adjective", "meticulous");
                family.put("adverb", "meticulously");
            }
            case "meticulously" -> {
                synonyms.addAll(List.of("carefully", "thoroughly", "precisely", "scrupulously"));
                antonyms.addAll(List.of("carelessly", "sloppily"));
                family.put("noun", "meticulousness");
                family.put("adjective", "meticulous");
                family.put("adverb", "meticulously");
            }
            case "meticulousness" -> {
                synonyms.addAll(List.of("precision", "thoroughness", "carefulness"));
                antonyms.addAll(List.of("carelessness", "negligence"));
                family.put("noun", "meticulousness");
                family.put("adjective", "meticulous");
                family.put("adverb", "meticulously");
            }
            case "happy" -> {
                synonyms.addAll(List.of("cheerful", "joyful", "delighted", "content"));
                antonyms.addAll(List.of("sad", "unhappy", "sorrowful", "depressed"));
                family.put("noun", "happiness");
                family.put("adjective", "happy");
                family.put("adverb", "happily");
            }
            case "resilience" -> {
                synonyms.addAll(List.of("fortitude", "toughness", "endurance", "flexibility"));
                antonyms.addAll(List.of("fragility", "vulnerability", "weakness"));
                family.put("noun", "resilience");
                family.put("adjective", "resilient");
                family.put("adverb", "resiliently");
            }
            case "eloquent" -> {
                synonyms.addAll(List.of("articulate", "fluent", "expressive", "persuasive"));
                antonyms.addAll(List.of("inarticulate", "tongue-tied", "unexpressive"));
                family.put("noun", "eloquence");
                family.put("adjective", "eloquent");
                family.put("adverb", "eloquently");
            }
            case "pragmatic" -> {
                synonyms.addAll(List.of("practical", "realistic", "sensible", "utilitarian"));
                antonyms.addAll(List.of("idealistic", "impractical", "unrealistic"));
                family.put("noun", "pragmatism");
                family.put("adjective", "pragmatic");
                family.put("adverb", "pragmatically");
            }
            case "ephemeral" -> {
                synonyms.addAll(List.of("transient", "fleeting", "short-lived", "momentary"));
                antonyms.addAll(List.of("permanent", "eternal", "everlasting", "perpetual"));
                family.put("noun", "ephemerality");
                family.put("adjective", "ephemeral");
                family.put("adverb", "ephemerally");
            }
            case "serendipity" -> {
                synonyms.addAll(List.of("chance", "fortune", "fluke", "happy coincidence"));
                antonyms.addAll(List.of("misfortune", "bad luck"));
                family.put("noun", "serendipity");
                family.put("adjective", "serendipitous");
                family.put("adverb", "serendipitously");
            }
            default -> {
                if (context.meaning != null && !context.meaning.isBlank()) {
                    String[] parts = context.meaning.split("[,;]");
                    for (String p : parts) {
                        String clean = p.trim().toLowerCase(java.util.Locale.ENGLISH);
                        if (!clean.isEmpty() && !clean.equalsIgnoreCase(target) && clean.length() < 25) {
                            synonyms.add(clean);
                        }
                    }
                }
            }
        }

        return new AiWordRelationsResponse(
                context.wordText,
                synonyms,
                antonyms,
                family,
                "fallback",
                null,
                true,
                false
        );
    }

    private record WordContext(
            String wordText,
            String meaning,
            String definition,
            String category,
            String exampleSentence,
            String cefrLevel,
            boolean isStruggling
    ) {}
}
