package com.authsystemjava.backend.controller;

import com.authsystemjava.backend.dto.UserDto;
import com.authsystemjava.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(Authentication auth) {
        return ResponseEntity.ok(userService.getMe(auth.getName()));
    }
}