package com.authsystemjava.backend.controller;

import com.authsystemjava.backend.dto.*;
import com.authsystemjava.backend.service.AuthService;
import com.authsystemjava.backend.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @Value("${app.base-url}")
    private String frontendUrl;

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(
            @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse auth = authService.register(request, userAgent);
        cookieUtil.setCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(auth.getUser());
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse auth = authService.login(request, userAgent);
        cookieUtil.setCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(auth.getUser());
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserDto> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String refreshToken = cookieUtil.getCookieValue(httpRequest, "refresh_token");
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }
        AuthResponse auth = authService.refresh(refreshToken);
        cookieUtil.setCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(auth.getUser());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String refreshToken = cookieUtil.getCookieValue(httpRequest, "refresh_token");
        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        cookieUtil.clearCookies(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(
        @RequestParam String token,
        HttpServletResponse response) throws IOException {
        try {
            authService.verifyEmail(token);
            response.sendRedirect(frontendUrl + "/login?verified=true");
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "/verify-email-error");
        }
        return ResponseEntity.ok().build();
    }   

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
        @RequestBody Map<String, String> body) {
        authService.resendVerification(body.get("email"));
        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }
}