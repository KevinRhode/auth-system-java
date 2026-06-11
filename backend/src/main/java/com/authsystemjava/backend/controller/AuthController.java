package com.authsystemjava.backend.controller;

import com.authsystemjava.backend.dto.*;
import com.authsystemjava.backend.service.AuthService;
import com.authsystemjava.backend.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(201).body(Map.of("message", "Check your email to verify your account"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthResponse auth = authService.login(request, userAgent);
        cookieUtil.setCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(auth);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest httpRequest,
            HttpServletResponse response) {
        String refreshToken = cookieUtil.getCookieValue(httpRequest, "refresh_token");
        if (refreshToken == null) {
            return ResponseEntity.status(401).build();
        }
        AuthResponse auth = authService.refresh(refreshToken);
        cookieUtil.setCookies(response, auth.getAccessToken(), auth.getRefreshToken());
        return ResponseEntity.ok(auth);
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
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
        @RequestBody Map<String, String> body) {
        authService.resendVerification(body.get("email"));
        return ResponseEntity.ok(Map.of("message", "Verification email sent"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        // identical response whether or not the account exists
        return ResponseEntity.ok(Map.of(
                "message", "If an account exists for that email, a reset link has been sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of(
                "message", "Password updated. Please log in with your new password"));
    }
}