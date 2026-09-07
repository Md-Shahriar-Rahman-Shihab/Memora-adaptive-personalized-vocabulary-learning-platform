package com.memora.modules.ai;

import com.memora.modules.ai.service.AiPromptBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AiPromptBuilderTest {

    @Test
    @DisplayName("Explanation prompt enforces CEFR calibration, authentic definitions, and anti-circularity")
    void testExplanationPrompt() {
        String prompt = AiPromptBuilder.buildExplanationPrompt(
                "resilient", "B2", "able to recover quickly", "GENERAL", false
        );

        assertNotNull(prompt);
        assertTrue(prompt.contains("resilient"));
        assertTrue(prompt.contains("B2"));
        assertTrue(prompt.contains("Do NOT use circular definitions"));
        assertTrue(prompt.contains("Focus on the most common, authentic meaning"));
        assertFalse(prompt.contains("The learner struggles with this word"));
    }

    @Test
    @DisplayName("Explanation prompt includes additional breakdown when learner is struggling")
    void testExplanationPromptStruggling() {
        String prompt = AiPromptBuilder.buildExplanationPrompt(
                "resilient", "B2", "able to recover quickly", "GENERAL", true
        );

        assertTrue(prompt.contains("The learner struggles with this word"));
    }

    @Test
    @DisplayName("Example prompt demands single sentence, correct POS, and zero preamble")
    void testExamplePrompt() {
        String prompt = AiPromptBuilder.buildExamplePrompt(
                "meticulous", "C1", "very careful and precise", "ACADEMIC"
        );

        assertNotNull(prompt);
        assertTrue(prompt.contains("EXACTLY ONE grammatically correct, natural native-English sentence"));
        assertTrue(prompt.contains("correct part of speech"));
        assertTrue(prompt.contains("meticulous"));
        assertTrue(prompt.contains("C1"));
        assertTrue(prompt.contains("Return ONLY the sentence itself without quotation marks"));
    }

    @Test
    @DisplayName("Memory tip prompt strictly prohibits fake etymology and mandates real semantic hook")
    void testMemoryTipPrompt() {
        String prompt = AiPromptBuilder.buildMemoryTipPrompt(
                "ephemeral", "C1", "lasting a very short time", "LITERARY", true
        );

        assertNotNull(prompt);
        assertTrue(prompt.contains("STRICT PROHIBITION: Do NOT invent fake etymology"));
        assertTrue(prompt.contains("lasting a very short time"));
        assertTrue(prompt.contains("The learner has forgotten this word multiple times"));
    }

    @Test
    @DisplayName("Contextual usage prompt instructs POS-specific collocation rules and JSON output schema")
    void testContextualUsagePrompt() {
        String prompt = AiPromptBuilder.buildContextualUsagePrompt(
                "resilient", "B2", "able to recover quickly", "GENERAL"
        );

        assertNotNull(prompt);
        assertTrue(prompt.contains("CRITICAL COLLOCATION GRAMMAR RULES:"));
        assertTrue(prompt.contains("NEVER pair with transitive verbs that require noun objects (e.g. NEVER 'demonstrate resilient'"));
        assertTrue(prompt.contains("'Casual', 'General Conversational', 'Neutral', 'Formal', 'Academic', 'Professional'"));
        assertTrue(prompt.contains("\"usageNotes\""));
        assertTrue(prompt.contains("\"register\""));
        assertTrue(prompt.contains("\"collocations\""));
    }
}
