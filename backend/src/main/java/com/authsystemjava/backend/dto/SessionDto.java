package com.authsystemjava.backend.dto;

import com.authsystemjava.backend.model.Session;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionDto {
    private String id;
    private String userAgent;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public static SessionDto from(Session session) {
        return SessionDto.builder()
                .id(session.getId())
                .userAgent(session.getUserAgent())
                .createdAt(session.getCreatedAt())
                .expiresAt(session.getExpiresAt())
                .build();
    }
}