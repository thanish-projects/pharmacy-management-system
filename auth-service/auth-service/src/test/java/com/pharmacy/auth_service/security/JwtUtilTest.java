package com.pharmacy.auth_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// No @ExtendWith needed — JwtUtil has no dependencies to mock
// It's a pure utility class — just create it directly with new
class JwtUtilTest {

    // We create a REAL JwtUtil — not a mock
    // Because we want to test its actual token generation and parsing logic
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void generateToken_ShouldReturnNonNullToken() {
        String token = jwtUtil.generateToken("thanish@pharmacy.com", "ADMIN");

        assertNotNull(token);
        // JWT tokens have 3 parts separated by dots: header.payload.signature
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void extractUsername_ShouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken("thanish@pharmacy.com", "ADMIN");

        String extractedEmail = jwtUtil.extractUsername(token);

        assertEquals("thanish@pharmacy.com", extractedEmail);
    }

    @Test
    void extractRole_ShouldReturnCorrectRole() {
        String token = jwtUtil.generateToken("thanish@pharmacy.com", "ADMIN");

        String extractedRole = jwtUtil.extractRole(token);

        assertEquals("ADMIN", extractedRole);
    }

    @Test
    void extractRole_ShouldWorkForDoctorRole() {
        String token = jwtUtil.generateToken("doctor@pharmacy.com", "DOCTOR");

        assertEquals("DOCTOR", jwtUtil.extractRole(token));
        assertEquals("doctor@pharmacy.com", jwtUtil.extractUsername(token));
    }

    @Test
    void validateToken_WhenValidToken_ShouldNotThrow() {
        String token = jwtUtil.generateToken("thanish@pharmacy.com", "ADMIN");

        // validateToken() throws an exception if token is invalid
        // assertDoesNotThrow verifies it completes without throwing anything
        assertDoesNotThrow(() -> jwtUtil.validateToken(token));
    }

    @Test
    void validateToken_WhenTamperedToken_ShouldThrowException() {
        String token = jwtUtil.generateToken("thanish@pharmacy.com", "ADMIN");

        // Tamper with the token by changing the last character
        // This breaks the signature — validateToken should reject it
        String tamperedToken = token.substring(0, token.length() - 1) + "X";

        // Any JWT exception counts here — signature mismatch, malformed, etc.
        assertThrows(Exception.class, () -> jwtUtil.validateToken(tamperedToken));
    }
}