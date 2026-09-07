package com.memora.modules.ai.service;

/**
 * Centralized builder for all Memora AI prompt operations:
 * 1. Word Explanation (factually accurate, CEFR-calibrated, anti-circular)
 * 2. Personalized Example (single natural native sentence, part-of-speech verified)
 * 3. Memory Tip (mnemonics, mental imagery, anti-fake-etymology)
 * 4. Contextual Usage (part-of-speech aware collocations, controlled register)
 */
public final class AiPromptBuilder {

    private AiPromptBuilder() {}

    /**
     * Builds a prompt for CEFR-tailored vocabulary explanation.
     */
    public static String buildExplanationPrompt(String word, String cefrLevel, String meaning, String category, boolean isStruggling) {
        String level = cefrLevel != null && !cefrLevel.isBlank() ? cefrLevel.trim().toUpperCase() : "B1";
        return String.format(
                "You are Memora's AI vocabulary learning tutor. Explain the English word '%s' for an ESL learner at CEFR %s level.\n" +
                "Core meaning: '%s'. Category: '%s'.\n\n" +
                "REQUIREMENTS:\n" +
                "- Clearly explain the actual meaning of the word in simple, learner-friendly English appropriate for CEFR %s.\n" +
                "- Do NOT use circular definitions or use '%s' as the main explanation of itself.\n" +
                "- Focus on the most common, authentic meaning of the word.\n" +
                "- Avoid dictionary-style verbosity or overly academic jargon.\n" +
                "%s" +
                "- CEFR Guidelines:\n" +
                "  * A1/A2: Very simple everyday words, short clear sentences.\n" +
                "  * B1/B2: Clear, accessible language with practical context.\n" +
                "  * C1: Nuanced, precise explanation without artificial complexity.\n" +
                "- Provide 1 to 2 direct sentences. Do NOT include markdown titles, labels, or formatting.",
                word, level, meaning, category,
                level,
                word,
                isStruggling ? "- The learner struggles with this word; provide an extra simple, intuitive breakdown.\n" : ""
        );
    }

    /**
     * Builds a prompt for generating a single natural example sentence.
     */
    public static String buildExamplePrompt(String word, String cefrLevel, String meaning, String category) {
        String level = cefrLevel != null && !cefrLevel.isBlank() ? cefrLevel.trim().toUpperCase() : "B1";
        return String.format(
                "You are Memora's AI vocabulary learning tutor. Generate ONE natural example sentence using the word '%s' for a learner at CEFR %s level.\n" +
                "Meaning: '%s'. Category: '%s'.\n\n" +
                "REQUIREMENTS:\n" +
                "- Generate EXACTLY ONE grammatically correct, natural native-English sentence.\n" +
                "- The target word '%s' must be used strictly in its correct part of speech.\n" +
                "- The sentence must clearly demonstrate the meaning in a realistic context.\n" +
                "- Calibrate complexity to CEFR %s (A1/A2: everyday simple context; B1/B2: realistic workplace/social context; C1: sophisticated natural usage).\n" +
                "- Do NOT produce awkward sentences just to force the target word.\n" +
                "- Do NOT mention 'CEFR', 'AI', 'learner', 'example', or prompt instructions.\n" +
                "- Return ONLY the sentence itself without quotation marks, labels, or preamble.",
                word, level, meaning, category,
                word,
                level
        );
    }

    /**
     * Builds a prompt for generating a memorable retention mnemonic.
     */
    public static String buildMemoryTipPrompt(String word, String cefrLevel, String meaning, String category, boolean isStruggling) {
        String level = cefrLevel != null && !cefrLevel.isBlank() ? cefrLevel.trim().toUpperCase() : "B1";
        return String.format(
                "You are Memora's memory engine retention coach. Create a vivid mnemonic or memory association to help a learner remember the word '%s'.\n" +
                "Meaning: '%s'. CEFR level: %s. Category: '%s'.\n\n" +
                "REQUIREMENTS:\n" +
                "- Use a vivid mental image, sound association, contrast, analogy, or mini-story.\n" +
                "- The memory hook must directly connect to the real meaning: '%s'.\n" +
                "- STRICT PROHIBITION: Do NOT invent fake etymology or present false linguistic origins as historical facts.\n" +
                "- If using a sound association, frame it explicitly as a memory trick or rhyme, not actual word history.\n" +
                "- Keep it concise, punchy, and memorable (1 to 2 sentences).\n" +
                "%s" +
                "- Return ONLY the mnemonic tip without markdown titles, labels, or preamble.",
                word, meaning, level, category,
                meaning,
                isStruggling ? "- The learner has forgotten this word multiple times. Make the visual memory hook especially striking.\n" : ""
        );
    }

    /**
     * Builds a prompt for contextual usage analysis, requesting structured JSON output.
     */
    public static String buildContextualUsagePrompt(String word, String cefrLevel, String meaning, String category) {
        String level = cefrLevel != null && !cefrLevel.isBlank() ? cefrLevel.trim().toUpperCase() : "B1";
        return String.format(
                "You are Memora's AI vocabulary learning tutor. Analyze the practical contextual usage of the English word '%s'.\n" +
                "Meaning: '%s'. CEFR Level: %s. Category: '%s'.\n\n" +
                "TASK:\n" +
                "1. Determine the grammatical part of speech of '%s' (adjective, noun, verb, adverb).\n" +
                "2. Write usageNotes: 1-2 sentences explaining when and how native speakers naturally use this word (typical contexts, tone), without repeating the dictionary definition.\n" +
                "3. Select register: Choose exactly ONE from this controlled list: 'Casual', 'General Conversational', 'Neutral', 'Formal', 'Academic', 'Professional'.\n" +
                "4. Generate collocations: 3 to 5 natural, high-frequency collocations strictly respecting the word's grammatical part of speech.\n\n" +
                "CRITICAL COLLOCATION GRAMMAR RULES:\n" +
                "- If '%s' is an ADJECTIVE (e.g. 'resilient', 'meticulous'):\n" +
                "  * Pair with natural nouns (e.g. 'resilient people', 'resilient communities', 'resilient approach') or adverbs/copular verbs (e.g. 'remain resilient', 'highly resilient').\n" +
                "  * NEVER pair with transitive verbs that require noun objects (e.g. NEVER 'demonstrate resilient', 'maintain resilient', 'build resilient').\n" +
                "- If '%s' is a NOUN (e.g. 'resilience'):\n" +
                "  * Pair with natural verbs (e.g. 'build resilience', 'demonstrate resilience', 'develop resilience') or modifying adjectives.\n" +
                "- If '%s' is a VERB (e.g. 'scrutinize'):\n" +
                "  * Pair with natural objects (e.g. 'scrutinize the evidence', 'scrutinize a report') or adverbs (e.g. 'closely scrutinize').\n" +
                "- If '%s' is an ADVERB (e.g. 'meticulously'):\n" +
                "  * Pair with verbs or participles (e.g. 'meticulously planned', 'meticulously clean').\n\n" +
                "OUTPUT FORMAT:\n" +
                "Respond ONLY with a valid JSON object in this exact schema with no surrounding text or markdown formatting:\n" +
                "{\n" +
                "  \"usageNotes\": \"...\",\n" +
                "  \"register\": \"Formal\",\n" +
                "  \"collocations\": [\"phrase 1\", \"phrase 2\", \"phrase 3\"]\n" +
                "}",
                word, meaning, level, category,
                word, word, word, word, word
        );
    }
}
