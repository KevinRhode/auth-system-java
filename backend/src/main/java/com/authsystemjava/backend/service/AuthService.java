package com.authsystemjava.backend.service;

import com.authsystemjava.backend.dto.*;
import com.authsystemjava.backend.model.*;
import com.authsystemjava.backend.repository.*;
import com.authsystemjava.backend.util.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TokenHasher tokenHasher;
    private final RateLimitService rateLimitService;
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

        // generate and save verification token
        String rawToken = UUID.randomUUID().toString();        
        Token verificationToken = Token.builder()
                .user(user)
                .token(tokenHasher.sha256(rawToken))
                .type(TokenType.EMAIL_VERIFICATION)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        tokenRepository.save(verificationToken);

        // send email
        emailService.sendVerificationEmail(user.getEmail(), user.getName(), rawToken);

        log.info("User registered: {}", user.getEmail());

        return generateAuthResponse(user, userAgent);
    }
    
    @Transactional
    public void verifyEmail(String rawToken) {
        Token token = tokenRepository
                .findByTokenAndType(tokenHasher.sha256(rawToken), TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            throw new RuntimeException("Verification token expired");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(token);
        log.info("Email verified for: {}", user.getEmail());
    }

    // public void resendVerification(String email) {
    //     if (!rateLimitService.tryConsumeResend(email)) {
    //         throw new RuntimeException("RATE_LIMITED");
    //     }

    //     User user = userRepository.findByEmail(email)
    //             .orElseThrow(() -> new RuntimeException("User not found"));

    //     if (user.getEmailVerified()) {
    //         throw new RuntimeException("Email already verified");
    //     }

    //     // delete any existing verification tokens
    //     tokenRepository.deleteByUserIdAndType(user.getId(), TokenType.EMAIL_VERIFICATION);

    //     String rawToken = UUID.randomUUID().toString();
    //     Token verificationToken = Token.builder()
    //             .user(user)
    //             .token(rawToken)
    //             .type(TokenType.EMAIL_VERIFICATION)
    //             .expiresAt(LocalDateTime.now().plusHours(24))
    //             .build();

    //     tokenRepository.save(verificationToken);
    //     emailService.sendVerificationEmail(user.getEmail(), user.getName(), rawToken);
    //     log.info("Verification email resent to: {}", user.getEmail());
    // }

    public void resendVerification(String email) {
        if (!rateLimitService.tryConsumeResend(email)) {
            throw new RuntimeException("RATE_LIMITED");
        }

        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getEmailVerified()) return;   // silently no-op

            tokenRepository.deleteByUserIdAndType(user.getId(), TokenType.EMAIL_VERIFICATION);

            String rawToken = UUID.randomUUID().toString();
            Token verificationToken = Token.builder()
                    .user(user)
                    .token(tokenHasher.sha256(rawToken))
                    .type(TokenType.EMAIL_VERIFICATION)
                    .expiresAt(LocalDateTime.now().plusHours(24))
                    .build();

            tokenRepository.save(verificationToken);
            emailService.sendVerificationEmail(user.getEmail(), user.getName(), rawToken);
        });
    // always returns success to the caller — no user enumeration
    }

    public AuthResponse login(LoginRequest request, String userAgent) {        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - email not found: {}", request.getEmail());
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.getEmailVerified()) {
            throw new RuntimeException("EMAIL_NOT_VERIFIED");
        }

        log.info("Login successful for: {}", request.getEmail());
        return generateAuthResponse(user, userAgent);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
    Session session = sessionRepository.findByToken(refreshToken)
            .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessionRepository.delete(session);
            throw new RuntimeException("Refresh token expired");
        }

        User user = session.getUser();

        // only rotate refresh token if less than 1 day remaining
        boolean shouldRotate = session.getExpiresAt()
            .isBefore(LocalDateTime.now().plusDays(1));

        String newAccessToken;
        String returnedRefreshToken = refreshToken;
        String returnedSessionId = session.getId();
                        
        if (shouldRotate) {
            log.info("Rotating refresh token for user: {}", user.getEmail());
            String newRefreshToken = jwtService.generateRefreshToken(user.getId());

            sessionRepository.delete(session);

            Session newSession = Session.builder()
                .user(user)
                .token(tokenHasher.sha256(newRefreshToken))
                .userAgent(session.getUserAgent())
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

            sessionRepository.save(newSession);
            
            newAccessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name(), session.getId());
            returnedRefreshToken = newRefreshToken;
            returnedSessionId = newSession.getId();
        } else {
            newAccessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name(), session.getId());
        }

        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(returnedRefreshToken)
            .sessionId(returnedSessionId)
            .user(UserDto.from(user))
            .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        sessionRepository.findByToken(tokenHasher.sha256(refreshToken))
                .ifPresent(sessionRepository::delete);
    }

    private AuthResponse generateAuthResponse(User user, String userAgent) {        
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        Session session = Session.builder()
                .user(user)
                .token(tokenHasher.sha256(refreshToken))
                .userAgent(userAgent)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        sessionRepository.save(session);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name(), session.getId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .sessionId(session.getId())
                .user(UserDto.from(user))
                .build();
    }
}