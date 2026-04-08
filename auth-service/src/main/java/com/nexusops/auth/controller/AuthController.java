package com.nexusops.auth.controller;

import com.nexusops.auth.entity.UserAccount;
import com.nexusops.auth.repository.UserRepository;
import com.nexusops.auth.security.JwtProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    public record AuthRequest(String email, String password, String role) {}
    public record AuthResponse(String token, String email) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        UserAccount user = new UserAccount();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role() != null ? request.role() : "EMPLOYEE");

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        Optional<UserAccount> userOpt = userRepository.findByEmail(request.email());
        
        if (userOpt.isPresent() && passwordEncoder.matches(request.password(), userOpt.get().getPasswordHash())) {
            UserAccount user = userOpt.get();
            String token = jwtProvider.generateToken(user.getEmail(), user.getRole());
            return ResponseEntity.ok(new AuthResponse(token, user.getEmail()));
        }
        
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
