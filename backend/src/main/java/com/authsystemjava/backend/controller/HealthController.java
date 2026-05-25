package com.authsystemjava.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController  {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
    
    @GetMapping("/debug")
    public ResponseEntity<Map<String, String>> debug() {
        return ResponseEntity.ok(Map.of(
            "clientUrl", allowedOrigins,
            "status", "ok"
        ));
    }
}
