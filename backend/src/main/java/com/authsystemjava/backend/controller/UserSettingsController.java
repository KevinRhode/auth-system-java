package com.authsystemjava.backend.controller;

import com.authsystemjava.backend.dto.UserSettingsDto;
import com.authsystemjava.backend.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user/settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserSettingsDto> getSettings(Authentication auth) {
        return ResponseEntity.ok(userSettingsService.getOrCreateSettings(auth.getName()));
    }
}