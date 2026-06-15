package com.authsystemjava.backend;

import com.authsystemjava.backend.dto.AuthResponse;
import com.authsystemjava.backend.exception.ApiException;
import com.authsystemjava.backend.exception.ErrorCode;
import com.authsystemjava.backend.model.Role;
import com.authsystemjava.backend.model.Session;
import com.authsystemjava.backend.model.User;
import com.authsystemjava.backend.repository.SessionRepository;
import com.authsystemjava.backend.repository.TokenRepository;
import com.authsystemjava.backend.repository.UserRepository;
import com.authsystemjava.backend.service.AuthService;
import com.authsystemjava.backend.util.TokenHasher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Proves the README's strongest claim: "refresh token rotation that survives
 * concurrent requests." Because the service only rotates when the refresh token
 * has less than one day of life remaining, several refreshes firing at once on a
 * fresh token are idempotent — none of them rotates, so none invalidates the
 * others, and the user is never spuriously logged out.
 *
 * This test class is intentionally NOT @Transactional: the worker threads run in
 * their own transactions/connections and must see committed rows, so each test
 * persists real data and cleans up in @AfterEach.
 */
@SpringBootTest
@ActiveProfiles("test")
class RefreshTokenRotationTest {

    @Autowired AuthService authService;
    @Autowired UserRepository userRepository;
    @Autowired SessionRepository sessionRepository;
    @Autowired TokenRepository tokenRepository;
    @Autowired TokenHasher tokenHasher;

    @AfterEach
    void cleanUp() {
        sessionRepository.deleteAll();
        tokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User persistUser(String email) {
        return userRepository.save(User.builder()
                .name("Concurrency Tester")
                .email(email)
                .password("not-used-by-refresh")
                .role(Role.USER)
                .emailVerified(true)
                .build());
    }

    private void persistSession(User user, String rawRefreshToken, LocalDateTime expiresAt) {
        sessionRepository.save(Session.builder()
                .user(user)
                .token(tokenHasher.sha256(rawRefreshToken))
                .userAgent("JUnit")
                .expiresAt(expiresAt)
                .build());
    }

    @Test
    void concurrentRefreshesOnAFreshTokenAllSucceedAndDoNotRotate() throws Exception {
        User user = persistUser("fresh@test.com");
        String raw = "fresh-refresh-token";
        persistSession(user, raw, LocalDateTime.now().plusDays(7)); // > 1 day → no rotation

        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch fireAtOnce = new CountDownLatch(1);
        List<Future<AuthResponse>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                fireAtOnce.await();              // line everyone up...
                return authService.refresh(raw); // ...then fire simultaneously
            }));
        }

        ready.await(5, TimeUnit.SECONDS);
        fireAtOnce.countDown();

        int succeeded = 0;
        List<String> failures = new ArrayList<>();
        for (Future<AuthResponse> f : futures) {
            try {
                AuthResponse res = f.get(20, TimeUnit.SECONDS);
                assertNotNull(res.getAccessToken(), "each refresh should mint a fresh access token");
                assertEquals(raw, res.getRefreshToken(),
                        "a fresh token must not rotate, so the same refresh token is returned");
                succeeded++;
            } catch (ExecutionException e) {
                failures.add(String.valueOf(e.getCause()));
            }
        }
        pool.shutdownNow();

        assertEquals(threads, succeeded,
                "every concurrent refresh should succeed; failures were: " + failures);
        assertTrue(sessionRepository.findByToken(tokenHasher.sha256(raw)).isPresent(),
                "the original session must still exist after the concurrent burst");

        // The user is demonstrably still logged in: a follow-up refresh works.
        assertNotNull(authService.refresh(raw).getAccessToken());
    }

    @Test
    void refreshNearExpiryRotatesTheToken() {
        User user = persistUser("near-expiry@test.com");
        String raw = "near-expiry-refresh-token";
        persistSession(user, raw, LocalDateTime.now().plusHours(12)); // < 1 day → rotate

        AuthResponse res = authService.refresh(raw);

        assertNotEquals(raw, res.getRefreshToken(),
                "a near-expiry token must be rotated to a new value");
        assertTrue(sessionRepository.findByToken(tokenHasher.sha256(raw)).isEmpty(),
                "the old session must be deleted after rotation");
        assertTrue(sessionRepository.findByToken(tokenHasher.sha256(res.getRefreshToken())).isPresent(),
                "a new session must exist for the rotated token");
    }

    @Test
    void refreshWithUnknownTokenIsRejected() {
        ApiException ex = assertThrows(ApiException.class,
                () -> authService.refresh("a-token-that-was-never-issued"));
        assertEquals(ErrorCode.INVALID_REFRESH_TOKEN, ex.getErrorCode());
    }

    @Test
    void refreshWithExpiredSessionIsRejectedAndPurged() {
        User user = persistUser("expired-session@test.com");
        String raw = "expired-session-refresh-token";
        persistSession(user, raw, LocalDateTime.now().minusMinutes(1)); // already expired

        ApiException ex = assertThrows(ApiException.class, () -> authService.refresh(raw));
        assertEquals(ErrorCode.TOKEN_EXPIRED, ex.getErrorCode());
        assertTrue(sessionRepository.findByToken(tokenHasher.sha256(raw)).isEmpty(),
                "an expired session should be deleted when a refresh is attempted");
    }
}