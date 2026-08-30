package com.memora.modules.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.memora.modules.user.domain.Role;
import com.memora.modules.user.domain.VocabularyLevel;
import com.memora.modules.user.dto.LoginRequest;
import com.memora.modules.user.dto.RegistrationRequest;
import com.memora.modules.user.entity.User;
import com.memora.modules.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("1. Successful user registration should persist user and return JWT token")
    void testSuccessfulRegistration() throws Exception {
        RegistrationRequest request = new RegistrationRequest("Alice Walker", "alice@example.com", "Secret123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User registered successfully")))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.tokenType", is("Bearer")))
                .andExpect(jsonPath("$.data.user.name", is("Alice Walker")))
                .andExpect(jsonPath("$.data.user.email", is("alice@example.com")))
                .andExpect(jsonPath("$.data.user.currentLevel", is("A1")))
                .andExpect(jsonPath("$.data.user.xp", is(0)))
                .andExpect(jsonPath("$.data.user.streak", is(0)))
                .andExpect(jsonPath("$.data.user.role", is("LEARNER")));

        assertTrue(userRepository.existsByEmail("alice@example.com"));
    }

    @Test
    @DisplayName("2. Duplicate email registration should return 409 Conflict")
    void testDuplicateEmailRegistration() throws Exception {
        RegistrationRequest request = new RegistrationRequest("Alice", "alice@example.com", "Secret123");

        // First registration
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Duplicate registration with same email
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", containsString("User with email 'alice@example.com' already exists")));
    }

    @Test
    @DisplayName("3. Password hashing verification - password must be stored as BCrypt hash, never plain text")
    void testPasswordIsHashedInDatabase() throws Exception {
        String rawPassword = "PlainTextPassword123";
        RegistrationRequest request = new RegistrationRequest("Bob", "bob@example.com", rawPassword);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        User user = userRepository.findByEmail("bob@example.com").orElseThrow();
        assertNotEquals(rawPassword, user.getPasswordHash());
        assertTrue(passwordEncoder.matches(rawPassword, user.getPasswordHash()));
        assertTrue(user.getPasswordHash().startsWith("$2a$") || user.getPasswordHash().startsWith("$2b$"));
    }

    @Test
    @DisplayName("4. Successful login should verify credentials and return valid JWT")
    void testSuccessfulLogin() throws Exception {
        // Register user
        RegistrationRequest regRequest = new RegistrationRequest("Charlie", "charlie@example.com", "MyPassword123");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        // Login
        LoginRequest loginRequest = new LoginRequest("charlie@example.com", "MyPassword123");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Authentication successful")))
                .andExpect(jsonPath("$.data.accessToken", notNullValue()))
                .andExpect(jsonPath("$.data.user.email", is("charlie@example.com")));
    }

    @Test
    @DisplayName("5. Invalid password login should return 401 Unauthorized")
    void testLoginWithInvalidPassword() throws Exception {
        RegistrationRequest regRequest = new RegistrationRequest("Dave", "dave@example.com", "CorrectPassword");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("dave@example.com", "WrongPassword");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(401)))
                .andExpect(jsonPath("$.message", is("Invalid email or password")));
    }

    @Test
    @DisplayName("6. Non-existent email login should return 401 Unauthorized")
    void testLoginWithNonExistentEmail() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", "AnyPassword");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("7. Protected endpoint without JWT should return 401 Unauthorized")
    void testProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.status", is(401)));
    }

    @Test
    @DisplayName("8. Protected endpoint with valid JWT should return 200 OK")
    void testProtectedEndpointWithValidToken() throws Exception {
        RegistrationRequest regRequest = new RegistrationRequest("Eve", "eve@example.com", "SecurePassword");
        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String token = responseNode.get("data").get("accessToken").asText();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.email", is("eve@example.com")));
    }

    @Test
    @DisplayName("9. GET /api/v1/users/me should return authenticated user's safe profile")
    void testGetMeEndpoint() throws Exception {
        RegistrationRequest regRequest = new RegistrationRequest("Frank Miller", "frank@example.com", "FrankPassword123");
        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(regResult.getResponse().getContentAsString());
        String token = responseNode.get("data").get("accessToken").asText();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.name", is("Frank Miller")))
                .andExpect(jsonPath("$.data.email", is("frank@example.com")))
                .andExpect(jsonPath("$.data.currentLevel", is("A1")))
                .andExpect(jsonPath("$.data.xp", is(0)))
                .andExpect(jsonPath("$.data.streak", is(0)))
                .andExpect(jsonPath("$.data.role", is("LEARNER")))
                .andExpect(jsonPath("$.data.password").doesNotExist())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }
}
