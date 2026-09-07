package com.memora.modules.ai.service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Lightweight linguistic validator ensuring contextual collocations and register
 * are grammatically valid, non-redundant, and strictly adhere to controlled standards.
 */
public final class CollocationValidator {

    private static final Set<String> CONTROLLED_REGISTERS = Set.of(
            "Casual",
            "General Conversational",
            "Neutral",
            "Formal",
            "Academic",
            "Professional"
    );

    // Common transitive verbs that require a noun object and cannot be directly followed
    // by a bare adjective (e.g. "demonstrate resilient", "maintain resilient", "build resilient")
    private static final Set<String> TRANSITIVE_NOUN_OBJECT_VERBS = Set.of(
            "demonstrate", "maintain", "build", "develop", "show", "exhibit", "possess", "acquire"
    );

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("(?s)```(?:json)?\\s*(.*?)\\s*```");

    private CollocationValidator() {}

    /**
     * Cleans raw JSON text by removing markdown code block markers.
     */
    public static String cleanJsonText(String raw) {
        if (raw == null) return "{}";
        var matcher = CODE_BLOCK_PATTERN.matcher(raw.trim());
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return raw.trim();
    }

    /**
     * Validates and normalizes the register string against the controlled vocabulary.
     */
    public static String normalizeRegister(String candidate, String cefrLevel) {
        if (candidate != null && !candidate.isBlank()) {
            String trimmed = candidate.trim();
            for (String reg : CONTROLLED_REGISTERS) {
                if (reg.equalsIgnoreCase(trimmed) || trimmed.toLowerCase().contains(reg.toLowerCase())) {
                    return reg;
                }
            }
        }
        // Sensible CEFR-based default
        if ("B2".equalsIgnoreCase(cefrLevel) || "C1".equalsIgnoreCase(cefrLevel)) {
            return "Professional";
        }
        return "General Conversational";
    }

    /**
     * Validates candidate collocations, filtering out malformed, duplicate, or ungrammatical phrases,
     * and guarantees 3 to 5 natural, valid collocations.
     */
    public static List<String> validateCollocations(List<String> candidates, String targetWord, String cefrLevel) {
        if (targetWord == null || targetWord.isBlank()) {
            return Collections.emptyList();
        }

        String lowerWord = targetWord.trim().toLowerCase();
        boolean isLikelyAdjective = isLikelyAdjective(lowerWord);

        List<String> valid = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        if (candidates != null) {
            for (String item : candidates) {
                if (item == null || item.isBlank()) continue;
                String cleaned = item.trim().replaceAll("[\\\"'\\.]", "").trim();
                String cleanedLower = cleaned.toLowerCase();

                // Must contain the target word or its primary inflected form
                if (!cleanedLower.contains(lowerWord)) continue;

                // Max length check (collocations should be 2-5 words, never run-on sentences)
                String[] words = cleaned.split("\\s+");
                if (words.length > 5) continue;

                // Reject invalid transitive-verb + bare-adjective combinations (e.g. "demonstrate resilient")
                if (isLikelyAdjective && isInvalidVerbAdjectiveCollocation(words, lowerWord)) {
                    continue;
                }

                if (seen.add(cleanedLower)) {
                    valid.add(cleaned);
                }
            }
        }

        // If fewer than 3 valid collocations remain, supplement with natural POS-aware collocations
        if (valid.size() < 3) {
            List<String> fallbacks = generatePosAwareFallbacks(lowerWord, isLikelyAdjective, cefrLevel);
            for (String fb : fallbacks) {
                if (valid.size() >= 4) break;
                if (seen.add(fb.toLowerCase())) {
                    valid.add(fb);
                }
            }
        }

        // Ensure at most 5 collocations
        return valid.size() > 5 ? valid.subList(0, 5) : valid;
    }

    /**
     * Checks whether a collocation uses a transitive verb directly preceding a bare adjective
     * without a following noun, e.g. "demonstrate resilient", "maintain resilient".
     */
    public static boolean isInvalidVerbAdjectiveCollocation(String[] tokens, String targetWord) {
        if (tokens == null || tokens.length == 0 || targetWord == null) return false;
        if (!isLikelyAdjective(targetWord.trim().toLowerCase())) return false;

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].toLowerCase();
            if (token.equals(targetWord)) {
                // If preceded by a transitive verb that expects a noun object
                if (i > 0) {
                    String prev = tokens[i - 1].toLowerCase();
                    if (TRANSITIVE_NOUN_OBJECT_VERBS.contains(prev)) {
                        // If there is no noun following the adjective, this is ungrammatical (e.g. "demonstrate resilient")
                        if (i == tokens.length - 1) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Heuristic identifying words that function primarily as adjectives based on common suffixes.
     */
    public static boolean isLikelyAdjective(String word) {
        if (word == null) return false;
        String w = word.toLowerCase().trim();
        return w.endsWith("ent") || w.endsWith("ant") ||
               w.endsWith("ous") || w.endsWith("ful") ||
               w.endsWith("able") || w.endsWith("ible") ||
               w.endsWith("ic")  || w.endsWith("ive") ||
               w.endsWith("al")  || w.endsWith("less") ||
               (w.endsWith("y") && !w.endsWith("ly") && w.length() > 3);
    }

    /**
     * Generates grammatically sound, natural default collocations tailored to the grammatical role.
     */
    public static List<String> generatePosAwareFallbacks(String word, boolean isAdjective, String cefrLevel) {
        List<String> result = new ArrayList<>();
        if (isAdjective) {
            result.add("remain " + word);
            result.add("highly " + word);
            result.add("a " + word + " approach");
            result.add(word + " community");
            result.add(word + " individuals");
        } else {
            result.add("effective " + word);
            result.add("understand " + word);
            result.add("apply " + word);
            result.add("key " + word);
        }
        return result;
    }
}
