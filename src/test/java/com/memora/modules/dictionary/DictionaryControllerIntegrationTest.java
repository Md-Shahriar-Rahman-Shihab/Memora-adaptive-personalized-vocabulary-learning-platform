package com.memora.modules.dictionary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.dictionary.client.DictionaryProvider;
import com.memora.modules.dictionary.exception.DictionaryRateLimitException;
import com.memora.modules.dictionary.exception.DictionaryServiceUnavailableException;
import com.memora.modules.dictionary.exception.DictionaryWordNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DictionaryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DictionaryProvider dictionaryProvider;

    private static final String HAPPY_MW_JSON = """
            [
              {
                "meta": {
                  "id": "happy:1",
                  "uuid": "mock-uuid-1",
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

    private static final String WORLD_MW_JSON = """
            [
              {
                "meta": { "id": "world:1" },
                "hwi": { "hw": "world", "prs": [{ "mw": "ˈwərld", "sound": { "audio": "world001" } }] },
                "fl": "noun",
                "shortdef": ["the earth or globe"]
              }
            ]
            """;

    private static final String COMPUTER_MW_JSON = """
            [
              {
                "meta": { "id": "computer:1" },
                "hwi": { "hw": "com*put*er", "prs": [{ "mw": "kəm-ˈpyü-tər", "sound": { "audio": "comput03" } }] },
                "fl": "noun",
                "shortdef": ["one that computes", "a programmable electronic device"]
              }
            ]
            """;

    private static final String HELLO_MW_JSON = """
            [
              {
                "meta": { "id": "hello:1" },
                "hwi": { "hw": "hel*lo", "prs": [{ "mw": "hə-ˈlō", "sound": { "audio": "hello001" } }] },
                "fl": "noun",
                "shortdef": ["an expression or gesture of greeting"]
              }
            ]
            """;

    @Test
    @DisplayName("Should require authentication for GET /api/v1/dictionary/{word}")
    void testDictionaryRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/dictionary/happy"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "learner@memora.com")
    @DisplayName("Should successfully return full dictionary entry for valid word 'happy' without exposing secret keys")
    void testValidWordSearch() throws Exception {
        when(dictionaryProvider.fetchWordEntries("happy"))
                .thenReturn(objectMapper.readTree(HAPPY_MW_JSON));

        mockMvc.perform(get("/api/v1/dictionary/happy")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.word", is("happy")))
                .andExpect(jsonPath("$.data.headword", is("hap·py")))
                .andExpect(jsonPath("$.data.pronunciation", is("ˈha-pē")))
                .andExpect(jsonPath("$.data.audioUrl", is("https://media.merriam-webster.com/audio/prons/en/us/mp3/h/happy001.mp3")))
                .andExpect(jsonPath("$.data.etymology", containsString("Middle English")))
                // Short definitions
                .andExpect(jsonPath("$.data.shortDefinitions", hasSize(2)))
                .andExpect(jsonPath("$.data.shortDefinitions[0]", containsString("fortunate")))
                // Parts of speech
                .andExpect(jsonPath("$.data.partsOfSpeech", hasSize(1)))
                .andExpect(jsonPath("$.data.partsOfSpeech[0].partOfSpeech", is("adjective")))
                .andExpect(jsonPath("$.data.partsOfSpeech[0].definitions", hasSize(1)))
                .andExpect(jsonPath("$.data.partsOfSpeech[0].definitions[0].definition", containsString("favored by fortune")))
                .andExpect(jsonPath("$.data.partsOfSpeech[0].definitions[0].examples", hasItem("a happy coincidence")))
                // Suggestions empty
                .andExpect(jsonPath("$.data.suggestions", hasSize(0)))
                // Verify API key is NOT exposed anywhere in JSON response
                .andExpect(jsonPath("$.data.apiKey").doesNotExist())
                .andExpect(jsonPath("$.data.key").doesNotExist());
    }

    @Test
    @WithMockUser(username = "learner@memora.com")
    @DisplayName("Should return spelling suggestions when exact word is not found")
    void testSpellingSuggestions() throws Exception {
        when(dictionaryProvider.fetchWordEntries("hapy"))
                .thenReturn(objectMapper.readTree("[\"happy\", \"happier\", \"happily\"]"));

        mockMvc.perform(get("/api/v1/dictionary/hapy")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.word", is("hapy")))
                .andExpect(jsonPath("$.data.suggestions", hasSize(3)))
                .andExpect(jsonPath("$.data.suggestions[0]", is("happy")));
    }

    @Test
    @WithMockUser(username = "learner@memora.com")
    @DisplayName("Should return 404 with friendly message when word is not found and no suggestions exist")
    void testWordNotFoundReturns404() throws Exception {
        when(dictionaryProvider.fetchWordEntries("unknownwordxyz"))
                .thenReturn(objectMapper.readTree("[]"));

        mockMvc.perform(get("/api/v1/dictionary/unknownwordxyz")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Word not found. Try checking the spelling.")));
    }

    @Test
    @WithMockUser(username = "learner@memora.com")
    @DisplayName("Should return 400 Bad Request on invalid word parameter")
    void testInvalidWordReturns400() throws Exception {
        mockMvc.perform(get("/api/v1/dictionary/invalid<script>word")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Invalid search word")));
    }

    @Test
    @WithMockUser(username = "learner@memora.com")
    @DisplayName("Should return 429 Too Many Requests when rate limited")
    void testRateLimitReturns429() throws Exception {
        when(dictionaryProvider.fetchWordEntries(anyString()))
                .thenThrow(new DictionaryRateLimitException());

        mockMvc.perform(get("/api/v1/dictionary/ratelimit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status", is(429)));
    }

    @Test
    @WithMockUser(username = "learner@memora.com")
    @DisplayName("Should return 503 Service Unavailable on external API failure")
    void testExternalApiFailureReturns503() throws Exception {
        when(dictionaryProvider.fetchWordEntries(anyString()))
                .thenThrow(new DictionaryServiceUnavailableException());

        mockMvc.perform(get("/api/v1/dictionary/networkfail")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status", is(503)))
                .andExpect(jsonPath("$.message", is("Dictionary service is temporarily unavailable. Please try again.")));
    }

    @Test
    @WithMockUser(username = "learner@memora.com")
    @DisplayName("Sequential searches: hello -> world -> computer -> happy must all execute successfully")
    void testSequentialSearches() throws Exception {
        when(dictionaryProvider.fetchWordEntries("hello")).thenReturn(objectMapper.readTree(HELLO_MW_JSON));
        when(dictionaryProvider.fetchWordEntries("world")).thenReturn(objectMapper.readTree(WORLD_MW_JSON));
        when(dictionaryProvider.fetchWordEntries("computer")).thenReturn(objectMapper.readTree(COMPUTER_MW_JSON));
        when(dictionaryProvider.fetchWordEntries("happy")).thenReturn(objectMapper.readTree(HAPPY_MW_JSON));

        // 1. hello
        mockMvc.perform(get("/api/v1/dictionary/hello"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.word", is("hello")));

        // 2. world
        mockMvc.perform(get("/api/v1/dictionary/world"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.word", is("world")));

        // 3. computer
        mockMvc.perform(get("/api/v1/dictionary/computer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.word", is("computer")));

        // 4. happy
        mockMvc.perform(get("/api/v1/dictionary/happy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.word", is("happy")));
    }

    @Test
    @WithMockUser(username = "learner@memora.com")
    @DisplayName("Sequential failure isolation: failed request does not poison subsequent requests")
    void testSequentialFailureIsolation() throws Exception {
        when(dictionaryProvider.fetchWordEntries("firstword")).thenReturn(objectMapper.readTree(HELLO_MW_JSON));
        when(dictionaryProvider.fetchWordEntries("failword")).thenThrow(new DictionaryWordNotFoundException("failword"));
        when(dictionaryProvider.fetchWordEntries("secondword")).thenReturn(objectMapper.readTree(WORLD_MW_JSON));

        // 1. Success
        mockMvc.perform(get("/api/v1/dictionary/firstword"))
                .andExpect(status().isOk());

        // 2. Failure
        mockMvc.perform(get("/api/v1/dictionary/failword"))
                .andExpect(status().isNotFound());

        // 3. Success again (must not be poisoned)
        mockMvc.perform(get("/api/v1/dictionary/secondword"))
                .andExpect(status().isOk());
    }
}
