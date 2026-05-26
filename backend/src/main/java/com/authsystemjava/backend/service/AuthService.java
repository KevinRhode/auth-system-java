package com.authsystemjava.backend.service;

import com.authsystemjava.backend.dto.*;
import com.authsystemjava.backend.model.*;
import com.authsystemjava.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthResponse register(RegisterRequest request, String userAgent) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);
        return generateAuthResponse(user, userAgent);
    }

    public AuthResponse login(LoginRequest request, String userAgent) {
        log.debug("Login attempt for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - email not found: {}", request.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        log.info("Login successful for: {}", request.getEmail());
        return generateAuthResponse(user, userAgent);
    }

    public AuthResponse refresh(String refreshToken) {
        Session session = sessionRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessionRepository.delete(session);
            throw new RuntimeException("Refresh token expired");
        }

        String existingUserAgent = session.getUserAgent();
        User user = session.getUser();
        sessionRepository.delete(session); // rotate — delete old session
        return generateAuthResponse(user, existingUserAgent);
    }

    public void logout(String refreshToken) {
        sessionRepository.findByToken(refreshToken)
                .ifPresent(sessionRepository::delete);
    }

    private AuthResponse generateAuthResponse(User user, String userAgent) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        Session session = Session.builder()
                .user(user)
                .token(refreshToken)
                .userAgent(userAgent)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        sessionRepository.save(session);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserDto.from(user))
                .build();
    }
}