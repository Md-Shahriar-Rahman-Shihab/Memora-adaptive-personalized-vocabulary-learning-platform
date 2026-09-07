package com.memora.modules.ai;

import com.memora.modules.ai.service.CollocationValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollocationValidatorTest {

    @Test
    @DisplayName("Should normalize register against controlled list or provide sensible CEFR defaults")
    void testRegisterNormalization() {
        assertEquals("Formal", CollocationValidator.normalizeRegister("Formal", "B1"));
        assertEquals("Academic", CollocationValidator.normalizeRegister("Academic style", "C1"));
        assertEquals("Professional", CollocationValidator.normalizeRegister("professional context", "B2"));
        assertEquals("Professional", CollocationValidator.normalizeRegister(null, "B2"));
        assertEquals("Professional", CollocationValidator.normalizeRegister(null, "C1"));
        assertEquals("General Conversational", CollocationValidator.normalizeRegister(null, "B1"));
        assertEquals("General Conversational", CollocationValidator.normalizeRegister("unknown register", "A2"));
    }

    @Test
    @DisplayName("Should detect invalid transitive verb + bare adjective collocations")
    void testInvalidVerbAdjectiveDetection() {
        // e.g. "demonstrate resilient", "maintain resilient", "build resilient"
        assertTrue(CollocationValidator.isInvalidVerbAdjectiveCollocation(
                new String[]{"demonstrate", "resilient"}, "resilient"
        ));
        assertTrue(CollocationValidator.isInvalidVerbAdjectiveCollocation(
                new String[]{"maintain", "resilient"}, "resilient"
        ));
        assertTrue(CollocationValidator.isInvalidVerbAdjectiveCollocation(
                new String[]{"build", "resilient"}, "resilient"
        ));

        // Valid combinations
        assertFalse(CollocationValidator.isInvalidVerbAdjectiveCollocation(
                new String[]{"resilient", "community"}, "resilient"
        ));
        assertFalse(CollocationValidator.isInvalidVerbAdjectiveCollocation(
                new String[]{"highly", "resilient"}, "resilient"
        ));
        assertFalse(CollocationValidator.isInvalidVerbAdjectiveCollocation(
                new String[]{"demonstrate", "resilience"}, "resilience"
        ));
    }

    @Test
    @DisplayName("Should filter ungrammatical collocations like 'demonstrate resilient' and supply natural fallbacks")
    void testValidateCollocationsRejectsUngrammatical() {
        List<String> candidates = List.of(
                "demonstrate resilient", // invalid verb + adjective
                "maintain resilient",    // invalid verb + adjective
                "resilient people",      // valid
                "highly resilient"       // valid
        );

        List<String> validated = CollocationValidator.validateCollocations(candidates, "resilient", "B2");

        assertNotNull(validated);
        assertTrue(validated.size() >= 3 && validated.size() <= 5);
        assertFalse(validated.contains("demonstrate resilient"));
        assertFalse(validated.contains("maintain resilient"));
        assertTrue(validated.contains("resilient people"));
        assertTrue(validated.contains("highly resilient"));
    }

    @Test
    @DisplayName("Should generate natural POS-aware collocations when candidates are empty")
    void testValidateCollocationsWithEmptyCandidates() {
        List<String> validated = CollocationValidator.validateCollocations(List.of(), "resilient", "B2");

        assertNotNull(validated);
        assertTrue(validated.size() >= 3 && validated.size() <= 5);
        for (String c : validated) {
            assertTrue(c.toLowerCase().contains("resilient"));
        }
    }

    @Test
    @DisplayName("Should clean JSON strings enclosed in markdown blocks")
    void testCleanJsonText() {
        String wrapped = "```json\n{\"usageNotes\": \"Test notes\", \"register\": \"Formal\"}\n```";
        String cleaned = CollocationValidator.cleanJsonText(wrapped);

        assertEquals("{\"usageNotes\": \"Test notes\", \"register\": \"Formal\"}", cleaned);

        String bare = "{\"usageNotes\": \"Direct\"}";
        assertEquals(bare, CollocationValidator.cleanJsonText(bare));
    }
}
