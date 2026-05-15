package com.pharmacy.auth_service.service;

import com.pharmacy.auth_service.exception.ResourceNotFoundException;
import com.pharmacy.auth_service.model.User;
import com.pharmacy.auth_service.repository.UserRepository;
import com.pharmacy.auth_service.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String saveUser(User user) {
        log.info("Registering new user with email: {}", user.getEmail());
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            log.warn("Registration failed — user already exists: {}", user.getEmail());
            throw new IllegalArgumentException(
                "User already exists with email: " + user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
        log.info("User registered successfully: {}", user.getEmail());
        return "User registered successfully";
    }

    public String generateToken(String username) {
        log.info("Generating JWT token for user: {}", username);
        User user = repository.findByEmail(username)
            .orElseThrow(() -> {
                log.warn("Token generation failed — user not found: {}", username);
                return new ResourceNotFoundException(
                    "User not found with email: " + username);
            });
        log.info("Token generated successfully for user: {} with role: {}",
            username, user.getRole());
        return jwtUtil.generateToken(username, user.getRole());
    }
}