package com.authsystemjava.backend;

import com.authsystemjava.backend.model.Role;
import com.authsystemjava.backend.model.User;
import com.authsystemjava.backend.repository.SessionRepository;
import com.authsystemjava.backend.repository.UserRepository;
import com.authsystemjava.backend.service.RateLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Locks in the cookie security posture and login enumeration resistance:
 *  - access/refresh tokens live only in HttpOnly; Secure; SameSite=None cookies,
 *    never in the JSON body (they are @JsonIgnore on AuthResponse);
 *  - an unknown email and a wrong password are indistinguishable to the caller;
 *  - an unverified account cannot log in.
 *
 * RateLimitService is mocked so the per-IP login limiter can't make repeated
 * test logins flaky.
 */

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CookieSecurityTest {

    private static final String PASSWORD = "ValidPassw0rd!";

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired SessionRepository sessionRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean RateLimitService rateLimitService;

    @BeforeEach
    void allowRateLimiting() {
        lenient().when(rateLimitService.tryConsumeLogin(anyString())).thenReturn(true);
        lenient().when(rateLimitService.tryConsumeRegister(anyString())).thenReturn(true);
    }

    private void persistVerifiedUser(String email) {
        userRepository.save(User.builder()
                .name("Cookie Tester").email(email)
                .password(passwordEncoder.encode(PASSWORD))
                .role(Role.USER).emailVerified(true).build());
    }

    private String loginBody(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    @Test
    void loginIssuesHttpOnlySecureSameSiteNoneCookies() throws Exception {
        persistVerifiedUser("cookie-flags@test.com");

        MvcResult result = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("cookie-flags@test.com", PASSWORD)))
                .andExpect(status().isOk())
                // tokens are @JsonIgnore — they must never appear in the response body
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(jsonPath("$.sessionId").exists())
                .andReturn();

        List<String> setCookies = result.getResponse().getHeaders("Set-Cookie");
        String access = setCookies.stream().filter(c -> c.startsWith("access_token="))
                .findFirst().orElseThrow(() -> new AssertionError("no access_token cookie was set"));
        String refresh = setCookies.stream().filter(c -> c.startsWith("refresh_token="))
                .findFirst().orElseThrow(() -> new AssertionError("no refresh_token cookie was set"));

        for (String cookie : List.of(access, refresh)) {
            assertTrue(cookie.contains("HttpOnly"), "cookie must be HttpOnly: " + cookie);
            assertTrue(cookie.contains("Secure"), "cookie must be Secure: " + cookie);
            assertTrue(cookie.contains("SameSite=None"), "cookie must be SameSite=None: " + cookie);
            assertTrue(cookie.contains("Path=/"), "cookie must be scoped to Path=/: " + cookie);
        }
    }

    @Test
    void unknownEmailAndWrongPasswordAreIndistinguishable() throws Exception {
        // Unknown account
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("ghost@test.com", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));

        // Known account, wrong password — must look exactly the same to the caller
        persistVerifiedUser("real@test.com");
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("real@test.com", "WrongPassw0rd!")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void unverifiedUserCannotLogIn() throws Exception {
        userRepository.save(User.builder()
                .name("Unverified").email("unverified@test.com")
                .password(passwordEncoder.encode(PASSWORD))
                .role(Role.USER).emailVerified(false).build());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("unverified@test.com", PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("EMAIL_NOT_VERIFIED"));
    }
}