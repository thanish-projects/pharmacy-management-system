package com.pharmacy.auth_service.contoller;

import com.pharmacy.auth_service.dto.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.pharmacy.auth_service.dto.SignupRequest;
import com.pharmacy.auth_service.model.User;
import com.pharmacy.auth_service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "User signup and login APIs")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService service;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Operation(summary = "User Signup",
            description = "Register a new user with name, email, password and role (ADMIN or DOCTOR)")
    @PostMapping("/signup")
    public String addNewUser(@RequestBody SignupRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());
        return service.saveUser(user);
    }

    @Operation(summary = "User Login",
            description = "Login with email and password. Returns JWT token on success.")
    @PostMapping("/login")
    public String getToken(@RequestBody LoginRequest authRequest) {
        Authentication authenticate = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );
        if (authenticate.isAuthenticated()) {
            return service.generateToken(authRequest.getEmail());
        } else {
            throw new RuntimeException("Invalid access");
        }
    }
}