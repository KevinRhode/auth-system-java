package com.authsystemjava.backend.controller;

import com.authsystemjava.backend.dto.SessionDto;
import com.authsystemjava.backend.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<SessionDto>> getSessions(Authentication auth) {
        return ResponseEntity.ok(sessionService.getUserSessions(auth.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> revokeSession(
            @PathVariable String id,
            Authentication auth) {
        sessionService.revokeSession(id, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> revokeAllSessions(Authentication auth) {
        sessionService.revokeAllSessions(auth.getName());
        return ResponseEntity.noContent().build();
    }
}