package com.memora.common.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/health should return 200 OK with UP status without Authorization header")
    void healthCheckShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Memora backend is running normally")))
                .andExpect(jsonPath("$.data.status", is("UP")))
                .andExpect(jsonPath("$.data.service", is("Memora Backend Platform")))
                .andExpect(jsonPath("$.data.version", is("1.0.0")));
    }

    @Test
    @DisplayName("HEAD /api/v1/health (UptimeRobot default probe) should return 200 OK without Authorization header")
    void healthCheckHeadShouldReturnOk() throws Exception {
        mockMvc.perform(head("/api/v1/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/health/ with trailing slash should return 200 OK with UP status")
    void healthCheckWithTrailingSlashShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/health/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("UP")));
    }

    @Test
    @DisplayName("HEAD /api/v1/health/ with trailing slash should return 200 OK")
    void healthCheckHeadWithTrailingSlashShouldReturnOk() throws Exception {
        mockMvc.perform(head("/api/v1/health/"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unauthenticated request to protected endpoint should return 401 Unauthorized")
    void protectedEndpointShouldRejectUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }
}
