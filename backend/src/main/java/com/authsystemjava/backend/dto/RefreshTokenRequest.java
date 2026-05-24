package com.authsystemjava.backend.dto;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}