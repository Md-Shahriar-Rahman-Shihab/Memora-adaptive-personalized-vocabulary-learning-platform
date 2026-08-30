package com.memora.common.exception;

import com.memora.common.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(GlobalExceptionHandlerTest.TestExceptionController.class)
@WithMockUser(username = "tester@memora.com", roles = {"LEARNER"})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/api/v1/test-exceptions")
    public static class TestExceptionController {

        @GetMapping("/not-found")
        public ApiResponse<String> throwNotFound() {
            throw new ResourceNotFoundException("Word", "id", 42L);
        }

        @GetMapping("/bad-request")
        public ApiResponse<String> throwBadRequest() {
            throw new BadRequestException("Invalid query parameter supplied");
        }

        @GetMapping("/illegal-argument")
        public ApiResponse<String> throwIllegalArgument() {
            throw new IllegalArgumentException("Negative repetition interval is not allowed");
        }

        @PostMapping("/validate")
        public ApiResponse<String> validatePayload(@Valid @RequestBody TestValidationDto dto) {
            return ApiResponse.success(dto.getName());
        }
    }

    public static class TestValidationDto {
        @NotBlank(message = "Name must not be blank")
        private String name;

        public TestValidationDto() {
        }

        public TestValidationDto(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    @DisplayName("ResourceNotFoundException should return 404 with ErrorDetails")
    void testResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/api/v1/test-exceptions/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Word not found with id: '42'")))
                .andExpect(jsonPath("$.path", is("/api/v1/test-exceptions/not-found")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("BadRequestException should return 400 with ErrorDetails")
    void testBadRequestException() throws Exception {
        mockMvc.perform(get("/api/v1/test-exceptions/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Invalid query parameter supplied")));
    }

    @Test
    @DisplayName("IllegalArgumentException should return 400 with ErrorDetails")
    void testIllegalArgumentException() throws Exception {
        mockMvc.perform(get("/api/v1/test-exceptions/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.message", is("Negative repetition interval is not allowed")));
    }

    @Test
    @DisplayName("Validation failure should return 400 with field validation errors")
    void testValidationException() throws Exception {
        mockMvc.perform(post("/api/v1/test-exceptions/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", is("Validation failed for one or more fields")))
                .andExpect(jsonPath("$.validationErrors[0].field", is("name")))
                .andExpect(jsonPath("$.validationErrors[0].message", is("Name must not be blank")));
    }
}
