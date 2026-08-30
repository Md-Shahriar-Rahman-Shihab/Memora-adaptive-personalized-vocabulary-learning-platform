package com.memora.modules.vocabulary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.vocabulary.domain.DifficultyLevel;
import com.memora.modules.vocabulary.domain.WordCategory;
import com.memora.modules.vocabulary.dto.VocabularyWordRequest;
import com.memora.modules.vocabulary.dto.VocabularyWordResponse;
import com.memora.modules.vocabulary.repository.UserWordProgressRepository;
import com.memora.modules.vocabulary.repository.VocabularyWordRepository;
import com.memora.modules.vocabulary.service.VocabularyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VocabularyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VocabularyWordRepository vocabularyWordRepository;

    @Autowired
    private UserWordProgressRepository userWordProgressRepository;

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userWordProgressRepository.deleteAll();
        vocabularyWordRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/v1/vocabulary should return all vocabulary items publicly")
    void testGetAllVocabularyPublic() throws Exception {
        vocabularyService.createWord(new VocabularyWordRequest("brilliant", "exceptionally clever or talented", null, null, null, DifficultyLevel.B2, WordCategory.ACADEMIC));
        vocabularyService.createWord(new VocabularyWordRequest("calm", "not showing or feeling nervousness", null, null, null, DifficultyLevel.A1, WordCategory.GENERAL));

        mockMvc.perform(get("/api/v1/vocabulary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/vocabulary/{id} should return specific word details")
    void testGetWordById() throws Exception {
        VocabularyWordResponse created = vocabularyService.createWord(new VocabularyWordRequest(
                "innovate", "make changes in something established", "To introduce new methods or ideas.",
                "/ˈɪn.ə.veɪt/", "Engineers continuously innovate new sustainable solutions.",
                DifficultyLevel.B2, WordCategory.TECHNOLOGY
        ));

        mockMvc.perform(get("/api/v1/vocabulary/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.word", is("innovate")))
                .andExpect(jsonPath("$.data.difficultyLevel", is("B2")))
                .andExpect(jsonPath("$.data.category", is("TECHNOLOGY")));
    }

    @Test
    @DisplayName("GET /api/v1/vocabulary/search?query=... should return matching words")
    void testSearchVocabulary() throws Exception {
        vocabularyService.createWord(new VocabularyWordRequest("pragmatic", "dealing with things sensibly and realistically", null, null, null, DifficultyLevel.B2, WordCategory.GENERAL));

        mockMvc.perform(get("/api/v1/vocabulary/search?query=pragm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].word", is("pragmatic")));
    }

    @Test
    @DisplayName("GET /api/v1/vocabulary/level/{level} should filter by CEFR level")
    void testFilterByLevel() throws Exception {
        vocabularyService.createWord(new VocabularyWordRequest("tree", "tall perennial woody plant", null, null, null, DifficultyLevel.A1, WordCategory.GENERAL));

        mockMvc.perform(get("/api/v1/vocabulary/level/A1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].word", is("tree")));
    }

    @Test
    @DisplayName("GET /api/v1/vocabulary/category/{category} should filter by category")
    void testFilterByCategory() throws Exception {
        vocabularyService.createWord(new VocabularyWordRequest("airport", "place where aircraft land and take off", null, null, null, DifficultyLevel.A1, WordCategory.TRAVEL));

        mockMvc.perform(get("/api/v1/vocabulary/category/TRAVEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].word", is("airport")));
    }

    @Test
    @WithMockUser(username = "admin@memora.com", roles = {"ADMIN"})
    @DisplayName("POST /api/v1/vocabulary as authenticated user should create word")
    void testCreateWordAuthenticated() throws Exception {
        VocabularyWordRequest request = new VocabularyWordRequest(
                "zenith", "the time at which something is most powerful or successful",
                "The highest point reached by a celestial or other object.", "/ˈzen.ɪθ/",
                "His career reached its zenith with the international award.",
                DifficultyLevel.C1, WordCategory.ACADEMIC
        );

        mockMvc.perform(post("/api/v1/vocabulary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.word", is("zenith")))
                .andExpect(jsonPath("$.data.difficultyLevel", is("C1")));
    }
}
