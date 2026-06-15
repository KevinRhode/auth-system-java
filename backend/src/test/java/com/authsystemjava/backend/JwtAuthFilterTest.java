package com.authsystemjava.backend;

import com.authsystemjava.backend.model.Role;
import com.authsystemjava.backend.model.Session;
import com.authsystemjava.backend.model.User;
import com.authsystemjava.backend.repository.SessionRepository;
import com.authsystemjava.backend.repository.UserRepository;
import com.authsystemjava.backend.service.JwtService;
import com.authsystemjava.backend.util.TokenHasher;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises JwtAuthFilter end-to-end through a protected endpoint. The filter
 * reads the access token from the {@code access_token} cookie (not a Bearer
 * header), rejects expired tokens, ignores tampered ones, and — crucially —
 * rejects valid tokens whose server-side session has been revoked.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JwtAuthFilterTest {

    /** Any authenticated, non-admin endpoint works as a probe. */
    private static final String PROTECTED_ENDPOINT = "/api/sessions";

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired SessionRepository sessionRepository;
    @Autowired JwtService jwtService;
    @Autowired TokenHasher tokenHasher;

    @Value("${jwt.secret}") String accessSecret;

    private User persistUser(String email) {
        return userRepository.save(User.builder()
                .name("Filter Tester").email(email)
                .password("hash").role(Role.USER).emailVerified(true).build());
    }

    private Session persistSession(User user, String rawRefresh) {
        return sessionRepository.save(Session.builder()
                .user(user).token(tokenHasher.sha256(rawRefresh))
                .userAgent("JUnit").expiresAt(LocalDateTime.now().plusDays(7)).build());
    }

    @Test
    void validAccessTokenCookieAuthenticatesRequest() throws Exception {
        User user = persistUser("valid-filter@test.com");
        Session session = persistSession(user, "valid-filter-refresh");
        String access = jwtService.generateAccessToken(
                user.getId(), user.getRole().name(), session.getId());

        mvc.perform(get(PROTECTED_ENDPOINT).cookie(new Cookie("access_token", access)))
                .andExpect(status().isOk());
    }

    @Test
    void requestWithoutAnyTokenIsDenied() throws Exception {
        int status = mvc.perform(get(PROTECTED_ENDPOINT))
                .andReturn().getResponse().getStatus();
        assertTrue(status == 401 || status == 403,
                "an unauthenticated request must be denied, was " + status);
    }

    @Test
    void expiredAccessTokenReturns401TokenExpired() throws Exception {
        User user = persistUser("expired-filter@test.com");
        Session session = persistSession(user, "expired-filter-refresh");

        // Hand-craft a correctly signed but already-expired token using the same key.
        long now = System.currentTimeMillis();
        SecretKey key = Keys.hmacShaKeyFor(accessSecret.getBytes());
        String expired = Jwts.builder()
                .subject(user.getId())
                .claim("role", user.getRole().name())
                .claim("sessionId", session.getId())
                .issuedAt(new Date(now - 60_000))
                .expiration(new Date(now - 30_000))   // expired 30s ago
                .signWith(key)
                .compact();

        mvc.perform(get(PROTECTED_ENDPOINT).cookie(new Cookie("access_token", expired)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("TOKEN_EXPIRED"));
    }

    @Test
    void tamperedAccessTokenIsRejected() throws Exception {
        User user = persistUser("tampered-filter@test.com");
        Session session = persistSession(user, "tampered-filter-refresh");
        String valid = jwtService.generateAccessToken(
                user.getId(), user.getRole().name(), session.getId());

        // Keep header + payload, replace the signature with garbage so verification fails.
        String[] parts = valid.split("\\.");
        String tampered = parts[0] + "." + parts[1] + ".this-signature-is-not-valid";

        int status = mvc.perform(get(PROTECTED_ENDPOINT)
                        .cookie(new Cookie("access_token", tampered)))
                .andReturn().getResponse().getStatus();
        assertTrue(status == 401 || status == 403,
                "a token with an invalid signature must be denied, was " + status);
    }

    /**
     * Server-side revocation: a perfectly valid, correctly signed token whose
     * session no longer exists (revoked from the dashboard, logged out elsewhere)
     * is rejected because the filter verifies the session still exists.
     */
    @Test
    void validTokenForRevokedSessionIsRejected() throws Exception {
        User user = persistUser("revoked-filter@test.com");
        // Deliberately persist NO session for this id.
        String access = jwtService.generateAccessToken(
                user.getId(), user.getRole().name(), "session-id-that-was-revoked");

        mvc.perform(get(PROTECTED_ENDPOINT).cookie(new Cookie("access_token", access)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("SESSION_REVOKED"));
    }
}