package com.authsystemjava.backend;

import com.authsystemjava.backend.model.Role;
import com.authsystemjava.backend.model.Session;
import com.authsystemjava.backend.model.User;
import com.authsystemjava.backend.repository.SessionRepository;
import com.authsystemjava.backend.repository.UserRepository;
import com.authsystemjava.backend.service.JwtService;
import com.authsystemjava.backend.util.TokenHasher;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Defense in depth: the Angular guard is UX, but the actual control is the
 * backend. These tests confirm role enforcement at the API — an admin endpoint
 * is reachable only with a token carrying ROLE_ADMIN.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoleBasedAccessControlTest {

    private static final String ADMIN_ENDPOINT = "/api/admin/users";

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepository;
    @Autowired SessionRepository sessionRepository;
    @Autowired JwtService jwtService;
    @Autowired TokenHasher tokenHasher;

    private Cookie accessCookieFor(String email, Role role) {
        User user = userRepository.save(User.builder()
                .name("RBAC Tester").email(email)
                .password("hash").role(role).emailVerified(true).build());
        Session session = sessionRepository.save(Session.builder()
                .user(user).token(tokenHasher.sha256("refresh-" + email))
                .userAgent("JUnit").expiresAt(LocalDateTime.now().plusDays(7)).build());
        String access = jwtService.generateAccessToken(user.getId(), role.name(), session.getId());
        return new Cookie("access_token", access);
    }

    @Test
    void userRoleCannotReachAdminEndpoint() throws Exception {
        mvc.perform(get(ADMIN_ENDPOINT).cookie(accessCookieFor("rbac-user@test.com", Role.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void moderatorRoleCannotReachAdminEndpoint() throws Exception {
        mvc.perform(get(ADMIN_ENDPOINT).cookie(accessCookieFor("rbac-mod@test.com", Role.MODERATOR)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminRoleCanReachAdminEndpoint() throws Exception {
        mvc.perform(get(ADMIN_ENDPOINT).cookie(accessCookieFor("rbac-admin@test.com", Role.ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousCannotReachAdminEndpoint() throws Exception {
        int status = mvc.perform(get(ADMIN_ENDPOINT)).andReturn().getResponse().getStatus();
        assertTrue(status == 401 || status == 403,
                "anonymous access to an admin endpoint must be denied, was " + status);
    }
}