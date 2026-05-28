package com.authsystemjava.backend.controller;

import com.authsystemjava.backend.dto.*;
import com.authsystemjava.backend.service.AuthService;
import com.authsystemjava.backend.util.CookieUtil;
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
    private final CookieUtil cookieUtil;

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
}