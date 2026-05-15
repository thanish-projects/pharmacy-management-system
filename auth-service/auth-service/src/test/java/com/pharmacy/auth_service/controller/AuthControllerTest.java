package com.pharmacy.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pharmacy.auth_service.contoller.AuthController;
import com.pharmacy.auth_service.dto.LoginRequest;
import com.pharmacy.auth_service.dto.SignupRequest;
import com.pharmacy.auth_service.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
    }
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // AuthenticationManager is directly @Autowired in AuthController
    // so it must be mocked as @MockBean too
    @MockBean
    private AuthenticationManager authenticationManager;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setName("Thanish");
        signupRequest.setEmail("thanish@pharmacy.com");
        signupRequest.setPassword("password123");
        signupRequest.setRole("ADMIN");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("thanish@pharmacy.com");
        loginRequest.setPassword("password123");
    }

    // ===== /auth/signup tests =====

    @Test
    void signup_WhenValidRequest_ShouldReturn200WithMessage() throws Exception {
        when(authService.saveUser(any())).thenReturn("User registered successfully");

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string("User registered successfully"));
    }

    @Test
    void signup_WhenDuplicateEmail_ShouldReturn400() throws Exception {
        // Your GlobalExceptionHandler maps IllegalArgumentException → 400 Bad Request
        // confirmed from the actual response body in the test output
        when(authService.saveUser(any()))
            .thenThrow(new IllegalArgumentException(
                "User already exists with email: thanish@pharmacy.com"));

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
            .andExpect(status().isBadRequest()); // 400
    }

    // ===== /auth/login tests =====

    @Test
    void login_WhenValidCredentials_ShouldReturnToken() throws Exception {
        // 3-argument constructor — sets isAuthenticated() = true
        // This matches what Spring's real AuthenticationManager returns after
        // successful BCrypt verification. The 2-arg constructor sets it to false
        // by default, which caused the controller to throw "Invalid access".
        Authentication mockAuth = new UsernamePasswordAuthenticationToken(
            loginRequest.getEmail(),
            loginRequest.getPassword(),
            Collections.emptyList() // empty authorities list — triggers authenticated=true
        );

        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);
        when(authService.generateToken(loginRequest.getEmail()))
            .thenReturn("mocked.jwt.token");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(content().string("mocked.jwt.token"));
    }

    @Test
    void login_WhenInvalidCredentials_ShouldReturn500() throws Exception {
        // BadCredentialsException is what Spring Security throws on wrong password
        // Your GlobalExceptionHandler doesn't handle this specifically
        // so Spring returns 500
        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().is5xxServerError());
    }
}