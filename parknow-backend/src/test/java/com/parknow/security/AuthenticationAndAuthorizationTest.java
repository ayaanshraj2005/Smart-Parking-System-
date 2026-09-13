package com.parknow.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parknow.dto.request.LoginRequest;
import com.parknow.dto.request.RegisterRequest;
import com.parknow.entity.Role;
import com.parknow.entity.User;
import com.parknow.entity.enums.RoleName;
import com.parknow.repository.RoleRepository;
import com.parknow.repository.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationAndAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_USER).build()));
        adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.ROLE_ADMIN).build()));
    }

    @Test
    @DisplayName("1. Should register a new user and hash the password with BCrypt")
    void testRegisterUserAndBCryptPasswordHashing() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .password("secret123")
                .phoneNumber("+1-555-0100")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.email").value("jane.doe@example.com"));

        // Verify password hashing in DB (Requirement 7)
        User dbUser = userRepository.findByEmail("jane.doe@example.com").orElseThrow();
        assertNotEquals("secret123", dbUser.getPasswordHash(), "Password must not be stored in plain text");
        assertTrue(passwordEncoder.matches("secret123", dbUser.getPasswordHash()), "Password hash must match BCrypt verification");
        assertTrue(dbUser.getPasswordHash().startsWith("$2a$") || dbUser.getPasswordHash().startsWith("$2b$"), "Must use BCrypt format");
    }

    @Test
    @DisplayName("2 & 3. Should authenticate valid user login and issue signed JWT")
    void testUserLoginAndReceiveJWT() throws Exception {
        userRepository.save(User.builder()
                .fullName("Alex Morgan")
                .email("alex@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());

        LoginRequest request = LoginRequest.builder()
                .email("alex@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.email").value("alex@example.com"));
    }

    @Test
    @DisplayName("4. Should access protected endpoint with valid JWT token")
    void testAccessProtectedEndpointWithValidJWT() throws Exception {
        userRepository.save(User.builder()
                .fullName("Sarah Connor")
                .email("sarah@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());

        LoginRequest loginReq = LoginRequest.builder().email("sarah@example.com").password("password123").build();
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseJson).get("data").get("accessToken").asText();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("sarah@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Sarah Connor"));
    }

    @Test
    @DisplayName("5. Should reject request with invalid JWT token")
    void testRejectInvalidJWTToken() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid.jwt.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("6. Should reject unauthorized role when regular USER attempts ADMIN endpoint")
    void testRejectUnauthorizedRole() throws Exception {
        userRepository.save(User.builder()
                .fullName("Regular User")
                .email("regular@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .roles(Set.of(userRole))
                .build());

        LoginRequest loginReq = LoginRequest.builder().email("regular@example.com").password("password123").build();
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("6b. Should allow ADMIN role to access ADMIN endpoint")
    void testAllowAdminRole() throws Exception {
        userRepository.save(User.builder()
                .fullName("System Admin")
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode("adminpass123"))
                .roles(Set.of(userRole, adminRole))
                .build());

        LoginRequest loginReq = LoginRequest.builder().email("admin@example.com").password("adminpass123").build();
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString()).get("data").get("accessToken").asText();

        mockMvc.perform(get("/api/admin/dashboard")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").exists());
    }
}
