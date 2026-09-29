package com.memora.modules.dictionary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.common.exception.BadRequestException;
import com.memora.modules.dictionary.client.DictionaryProvider;
import com.memora.modules.dictionary.dto.DictionaryResponse;
import com.memora.modules.dictionary.exception.DictionaryServiceUnavailableException;
import com.memora.modules.dictionary.exception.DictionaryWordNotFoundException;
import com.memora.modules.dictionary.service.DictionaryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DictionaryServiceTest {

    private DictionaryProvider dictionaryProvider;
    private DictionaryServiceImpl dictionaryService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        dictionaryProvider = mock(DictionaryProvider.class);
        dictionaryService = new DictionaryServiceImpl(dictionaryProvider);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should validate and trim input correctly")
    void testValidateAndNormalizeWord() {
        assertEquals("happy", dictionaryService.validateAndNormalizeWord("   happy   "));
        assertEquals("mother-in-law", dictionaryService.validateAndNormalizeWord("mother-in-law"));
        assertEquals("o'clock", dictionaryService.validateAndNormalizeWord("o'clock"));
        assertEquals("café", dictionaryService.validateAndNormalizeWord("café"));

        // Empty / null / whitespace
        assertThrows(BadRequestException.class, () -> dictionaryService.validateAndNormalizeWord(""));
        assertThrows(BadRequestException.class, () -> dictionaryService.validateAndNormalizeWord("    "));
        assertThrows(BadRequestException.class, () -> dictionaryService.validateAndNormalizeWord(null));

        // Excessively long
        String longWord = "a".repeat(65);
        assertThrows(BadRequestException.class, () -> dictionaryService.validateAndNormalizeWord(longWord));

        // Invalid characters / path injection
        assertThrows(BadRequestException.class, () -> dictionaryService.validateAndNormalizeWord("happy/world"));
        assertThrows(BadRequestException.class, () -> dictionaryService.validateAndNormalizeWord("../etc/passwd"));
        assertThrows(BadRequestException.class, () -> dictionaryService.validateAndNormalizeWord("happy?test=1"));
        assertThrows(BadRequestException.class, () -> dictionaryService.validateAndNormalizeWord("<script>"));
    }

    @Test
    @DisplayName("Should correctly resolve Merriam-Webster audio CDN URLs according to official subdirectory rules")
    void testResolveMerriamWebsterAudioUrl() {
        // Standard letter
        assertEquals("https://media.merriam-webster.com/audio/prons/en/us/mp3/h/happy001.mp3",
                dictionaryService.resolveMerriamWebsterAudioUrl("happy001"));
        assertEquals("https://media.merriam-webster.com/audio/prons/en/us/mp3/c/comput03.mp3",
                dictionaryService.resolveMerriamWebsterAudioUrl("comput03"));

        // "bix" prefix
        assertEquals("https://media.merriam-webster.com/audio/prons/en/us/mp3/bix/bix01.mp3",
                dictionaryService.resolveMerriamWebsterAudioUrl("bix01"));

        // "gg" prefix
        assertEquals("https://media.merriam-webster.com/audio/prons/en/us/mp3/gg/ggfoo01.mp3",
                dictionaryService.resolveMerriamWebsterAudioUrl("ggfoo01"));

        // Number prefix
        assertEquals("https://media.merriam-webster.com/audio/prons/en/us/mp3/number/90001.mp3",
                dictionaryService.resolveMerriamWebsterAudioUrl("90001"));

        // Null / blank
        assertNull(dictionaryService.resolveMerriamWebsterAudioUrl(null));
        assertNull(dictionaryService.resolveMerriamWebsterAudioUrl(""));
        assertNull(dictionaryService.resolveMerriamWebsterAudioUrl("   "));
    }

    @Test
    @DisplayName("Should cleanly strip and format Merriam-Webster tokens from text")
    void testCleanMwTokens() {
        assertEquals("fortunate", dictionaryService.cleanMwTokens("{bc}fortunate"));
        assertEquals("fortunate", dictionaryService.cleanMwTokens("{bc}{a_link|fortunate}"));
        assertEquals("a happy coincidence", dictionaryService.cleanMwTokens("a {it}happy{/it} coincidence"));
        assertEquals("Middle English, from hap luck", dictionaryService.cleanMwTokens("Middle English, from {it}hap{/it} luck"));
        assertEquals("lucky", dictionaryService.cleanMwTokens("{sx|lucky||}"));
        assertEquals("", dictionaryService.cleanMwTokens(null));
        assertEquals("", dictionaryService.cleanMwTokens(""));
    }

    @Test
    @DisplayName("Should parse full Merriam-Webster entry with headword, pronunciation, audio, definitions, examples, shortdefs, and etymology")
    void testParseFullEntry() throws Exception {
        String mwJson = """
                [
                  {
                    "meta": {
                      "id": "happy:1",
                      "uuid": "1234-uuid",
                      "stems": ["happy", "happier"]
                    },
                    "hwi": {
                      "hw": "hap*py",
                      "prs": [
                        {
                          "mw": "ˈha-pē",
                          "sound": {
                            "audio": "happy001"
                          }
                        }
                      ]
                    },
                    "fl": "adjective",
                    "def": [
                      {
                        "sseq": [
                          [
                            [
                              "sense",
                              {
                                "dt": [
                                  ["text", "{bc}favored by {a_link|fortune} {bc}fortunate"],
                                  ["vis", [ { "t": "a {it}happy{/it} coincidence" } ]]
                                ]
                              }
                            ]
                          ]
                        ]
                      }
                    ],
                    "shortdef": [
                      "favored by a good fortune : fortunate",
                      "enjoying or characterized by well-being and contentment"
                    ],
                    "et": [
                      ["text", "Middle English, from {it}hap{/it} luck"]
                    ]
                  }
                ]
                """;

        when(dictionaryProvider.fetchWordEntries("happy")).thenReturn(objectMapper.readTree(mwJson));

        DictionaryResponse response = dictionaryService.lookupWord("happy");

        assertNotNull(response);
        assertEquals("happy", response.word());
        assertEquals("hap·py", response.headword());
        assertEquals("ˈha-pē", response.pronunciation());
        assertEquals("https://media.merriam-webster.com/audio/prons/en/us/mp3/h/happy001.mp3", response.audioUrl());
        assertEquals("Middle English, from hap luck", response.etymology());

        // Short definitions
        assertEquals(2, response.shortDefinitions().size());
        assertEquals("favored by a good fortune : fortunate", response.shortDefinitions().get(0));

        // Parts of speech & detailed definitions
        assertEquals(1, response.partsOfSpeech().size());
        var pos = response.partsOfSpeech().get(0);
        assertEquals("adjective", pos.partOfSpeech());
        assertEquals(1, pos.definitions().size());

        var def = pos.definitions().get(0);
        assertTrue(def.definition().contains("favored by fortune"));
        assertEquals(1, def.examples().size());
        assertEquals("a happy coincidence", def.examples().get(0));

        // Suggestions should be empty on successful entry
        assertTrue(response.suggestions().isEmpty());
    }

    @Test
    @DisplayName("Should return spelling suggestions when provider returns array of strings")
    void testSpellingSuggestionsParsing() throws Exception {
        String suggestionsJson = "[\"happy\", \"happier\", \"happily\", \"haply\"]";

        when(dictionaryProvider.fetchWordEntries("hapy")).thenReturn(objectMapper.readTree(suggestionsJson));

        DictionaryResponse response = dictionaryService.lookupWord("hapy");

        assertNotNull(response);
        assertEquals("hapy", response.word());
        assertNull(response.headword());
        assertNull(response.pronunciation());
        assertNull(response.audioUrl());
        assertTrue(response.partsOfSpeech().isEmpty());
        assertTrue(response.shortDefinitions().isEmpty());
        assertNull(response.etymology());

        // Suggestions populated
        assertEquals(4, response.suggestions().size());
        assertEquals("happy", response.suggestions().get(0));
        assertEquals("happier", response.suggestions().get(1));
    }

    @Test
    @DisplayName("Should gracefully handle entry with missing audio, pronunciation, examples, or etymology")
    void testMissingOptionalFields() throws Exception {
        String minimalJson = """
                [
                  {
                    "meta": { "id": "simple" },
                    "hwi": { "hw": "simple" },
                    "fl": "noun",
                    "shortdef": ["something simple"]
                  }
                ]
                """;

        when(dictionaryProvider.fetchWordEntries("simple")).thenReturn(objectMapper.readTree(minimalJson));

        DictionaryResponse response = dictionaryService.lookupWord("simple");

        assertNotNull(response);
        assertEquals("simple", response.word());
        assertEquals("simple", response.headword());
        assertNull(response.pronunciation());
        assertNull(response.audioUrl());
        assertNull(response.etymology());
        assertEquals(1, response.shortDefinitions().size());
        assertEquals(1, response.partsOfSpeech().size());
        assertEquals("noun", response.partsOfSpeech().get(0).partOfSpeech());
        assertTrue(response.suggestions().isEmpty());
    }

    @Test
    @DisplayName("Should consolidate multiple entries across different parts of speech without losing definitions")
    void testConsolidateMultipleEntries() throws Exception {
        String multiJson = """
                [
                  {
                    "meta": { "id": "lead:1" },
                    "hwi": { "hw": "lead", "prs": [{ "mw": "ˈlēd", "sound": { "audio": "lead0001" } }] },
                    "fl": "verb",
                    "shortdef": ["to guide on a way"]
                  },
                  {
                    "meta": { "id": "lead:2" },
                    "hwi": { "hw": "lead", "prs": [{ "mw": "ˈled", "sound": { "audio": "lead0002" } }] },
                    "fl": "noun",
                    "shortdef": ["a heavy metallic element"]
                  }
                ]
                """;

        when(dictionaryProvider.fetchWordEntries("lead")).thenReturn(objectMapper.readTree(multiJson));

        DictionaryResponse response = dictionaryService.lookupWord("lead");

        assertNotNull(response);
        assertEquals("lead", response.word());
        assertEquals(2, response.partsOfSpeech().size());
        assertEquals("verb", response.partsOfSpeech().get(0).partOfSpeech());
        assertEquals("noun", response.partsOfSpeech().get(1).partOfSpeech());
        assertEquals(2, response.shortDefinitions().size());
    }

    @Test
    @DisplayName("Should serve subsequent requests for the same word from the in-memory cache")
    void testCacheHits() throws Exception {
        String json = """
                [
                  {
                    "meta": { "id": "cached" },
                    "hwi": { "hw": "cached" },
                    "fl": "adjective",
                    "shortdef": ["saved in cache"]
                  }
                ]
                """;

        when(dictionaryProvider.fetchWordEntries("cached")).thenReturn(objectMapper.readTree(json));

        // Call 1: triggers provider
        DictionaryResponse res1 = dictionaryService.lookupWord("cached");
        assertNotNull(res1);

        // Call 2: served from cache
        DictionaryResponse res2 = dictionaryService.lookupWord("cached");
        assertNotNull(res2);

        // Provider should have been called only once
        verify(dictionaryProvider, times(1)).fetchWordEntries("cached");
    }

    @Test
    @DisplayName("Should throw DictionaryWordNotFoundException when provider returns empty array")
    void testEmptyArrayThrowsNotFound() throws Exception {
        when(dictionaryProvider.fetchWordEntries("unknown")).thenReturn(objectMapper.readTree("[]"));

        assertThrows(DictionaryWordNotFoundException.class, () -> dictionaryService.lookupWord("unknown"));
    }

    @Test
    @DisplayName("Should propagate service unavailable exceptions from provider")
    void testProviderErrorPropagation() {
        when(dictionaryProvider.fetchWordEntries("error")).thenThrow(new DictionaryServiceUnavailableException());

        assertThrows(DictionaryServiceUnavailableException.class, () -> dictionaryService.lookupWord("error"));
    }

    @Test
    @DisplayName("Should extract explicit synonyms and antonyms while ignoring non-synonym hyperlinks and target word")
    void testDictionarySynonymAndAntonymExtraction() throws Exception {
        String mwJson = """
                [
                  {
                    "meta": {
                      "id": "joyous",
                      "syns": [
                        ["joyous", "cheerful", "glad"]
                      ]
                    },
                    "hwi": { "hw": "joyous" },
                    "fl": "adjective",
                    "def": [
                      {
                        "sseq": [
                          [
                            [
                              "sense",
                              {
                                "dt": [
                                  ["text", "{bc}feeling joy {bc}{sx|joyful||} {bc}see {a_link|happiness}"],
                                  ["uns", [
                                    [
                                      ["text", "{bc}{sx|gleeful||}"]
                                    ]
                                  ]]
                                ]
                              }
                            ]
                          ]
                        ]
                      }
                    ],
                    "ant_list": [
                      {
                        "wd": "sorrowful"
                      },
                      {
                        "wd": "miserable"
                      }
                    ],
                    "shortdef": ["feeling or expressing great happiness"]
                  }
                ]
                """;

        when(dictionaryProvider.fetchWordEntries("joyous")).thenReturn(objectMapper.readTree(mwJson));

        DictionaryResponse response = dictionaryService.lookupWord("joyous");

        assertNotNull(response);
        assertTrue(response.synonyms().contains("joyful"), "Should contain explicit cross-reference sx joyful");
        assertTrue(response.synonyms().contains("cheerful"), "Should contain syns item cheerful");
        assertTrue(response.synonyms().contains("glad"), "Should contain syns item glad");

        // Target word itself ("joyous") MUST be excluded
        assertFalse(response.synonyms().contains("joyous"), "Target word itself must not be in synonyms");

        // Non-synonym hyperlinks ({a_link|happiness}) MUST NOT be treated as synonyms
        assertFalse(response.synonyms().contains("happiness"), "Regular definition link must not be treated as synonym");

        // Antonyms must be extracted from ant_list
        assertTrue(response.antonyms().contains("sorrowful"));
        assertTrue(response.antonyms().contains("miserable"));
    }
}

