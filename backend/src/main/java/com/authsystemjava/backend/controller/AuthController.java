package com.authsystemjava.backend.controller;

import com.authsystemjava.backend.dto.*;
import com.authsystemjava.backend.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse auth = authService.register(request, userAgent);
        setCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(auth.getUser());
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse auth = authService.login(request, userAgent);
        setCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(auth.getUser());
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserDto> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String refreshToken = getCookieValue(httpRequest, "refresh_token");
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }
        AuthResponse auth = authService.refresh(refreshToken);
        setCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(auth.getUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String refreshToken = getCookieValue(httpRequest, "refresh_token");
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        clearCookies(response);
        return ResponseEntity.noContent().build();
    }

    // ── Cookie helpers ────────────────────────────────────

    private void setCookies(HttpServletResponse response,
                            String accessToken,
                            String refreshToken) {
        response.addHeader("Set-Cookie", buildCookie("access_token", accessToken, 900));
        response.addHeader("Set-Cookie", buildCookie("refresh_token", refreshToken, 604800));
    }

    private void clearCookies(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildCookie("access_token", "", 0));
        response.addHeader("Set-Cookie", buildCookie("refresh_token", "", 0));
    }

    private String buildCookie(String name, String value, long maxAge) {
        return String.format(
            "%s=%s; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=%d",
            name, value, maxAge
        );
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(name)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}