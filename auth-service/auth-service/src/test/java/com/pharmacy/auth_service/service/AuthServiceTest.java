package com.pharmacy.auth_service.service;

import com.pharmacy.auth_service.exception.ResourceNotFoundException;
import com.pharmacy.auth_service.model.User;
import com.pharmacy.auth_service.repository.UserRepository;
import com.pharmacy.auth_service.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository repository;

    // PasswordEncoder is a dependency of AuthService — we mock it
    // so we never actually run BCrypt during tests (BCrypt is slow by design)
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1);
        sampleUser.setName("Thanish");
        sampleUser.setEmail("thanish@pharmacy.com");
        sampleUser.setPassword("rawPassword123");
        sampleUser.setRole("ADMIN");
    }

    // ===== saveUser() tests =====

    @Test
    void saveUser_WhenNewUser_ShouldRegisterSuccessfully() {
        // Email doesn't exist yet — Optional.empty() simulates "not found in DB"
        when(repository.findByEmail(sampleUser.getEmail())).thenReturn(Optional.empty());

        // BCrypt encode — we fake it, return a dummy hash string
        // Real BCrypt takes ~100ms per call — mocking keeps tests fast
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hashedPassword");

        when(repository.save(any(User.class))).thenReturn(sampleUser);

        String result = authService.saveUser(sampleUser);

        assertEquals("User registered successfully", result);

        // Verify the password was encoded before saving
        // This is critical — if someone removes passwordEncoder.encode() from the service,
        // raw passwords get stored in DB. This test catches that.
        verify(passwordEncoder, times(1)).encode("rawPassword123");

        // Verify user was actually saved
        verify(repository, times(1)).save(sampleUser);
    }

    @Test
    void saveUser_WhenEmailAlreadyExists_ShouldThrowException() {
        // Email already exists — Optional.of() simulates "found in DB"
        when(repository.findByEmail(sampleUser.getEmail())).thenReturn(Optional.of(sampleUser));

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> authService.saveUser(sampleUser)
        );

        assertTrue(ex.getMessage().contains(sampleUser.getEmail()));

        // If email exists, we should never reach save() — verify it was never called
        verify(repository, never()).save(any());

        // Password encoding should also never happen for duplicate registrations
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void saveUser_ShouldEncodePasswordBeforeSaving() {
        // This test specifically verifies the PASSWORD IS HASHED before storing
        // It's a security test — not just a logic test
        when(repository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("rawPassword123")).thenReturn("$2a$10$hashedPassword");
        when(repository.save(any(User.class))).thenReturn(sampleUser);

        authService.saveUser(sampleUser);

        // After saveUser runs, the user's password should be the hashed version
        // because authService does: user.setPassword(passwordEncoder.encode(user.getPassword()))
        assertEquals("$2a$10$hashedPassword", sampleUser.getPassword());
    }

    // ===== generateToken() tests =====

    @Test
    void generateToken_WhenUserExists_ShouldReturnToken() {
        when(repository.findByEmail(sampleUser.getEmail())).thenReturn(Optional.of(sampleUser));

        // jwtUtil.generateToken() is mocked — we're not testing JWT generation here
        // We're testing that AuthService correctly calls it with the right arguments
        when(jwtUtil.generateToken(sampleUser.getEmail(), sampleUser.getRole()))
            .thenReturn("mocked.jwt.token");

        String token = authService.generateToken(sampleUser.getEmail());

        assertEquals("mocked.jwt.token", token);

        // Verify generateToken was called with BOTH email AND role
        // If someone changes the service to not pass the role, this test fails
        verify(jwtUtil, times(1)).generateToken(sampleUser.getEmail(), "ADMIN");
    }

    @Test
    void generateToken_WhenUserNotFound_ShouldThrowException() {
        when(repository.findByEmail("unknown@pharmacy.com")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> authService.generateToken("unknown@pharmacy.com")
        );

        assertTrue(ex.getMessage().contains("unknown@pharmacy.com"));

        // If user not found, token should never be generated
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void generateToken_ShouldPassCorrectRoleToJwt() {
        // Doctor role test — verifies role is correctly passed regardless of which role
        User doctorUser = new User();
        doctorUser.setEmail("doctor@pharmacy.com");
        doctorUser.setRole("DOCTOR");

        when(repository.findByEmail("doctor@pharmacy.com")).thenReturn(Optional.of(doctorUser));
        when(jwtUtil.generateToken("doctor@pharmacy.com", "DOCTOR"))
            .thenReturn("doctor.jwt.token");

        String token = authService.generateToken("doctor@pharmacy.com");

        assertEquals("doctor.jwt.token", token);
        verify(jwtUtil, times(1)).generateToken("doctor@pharmacy.com", "DOCTOR");
    }
}