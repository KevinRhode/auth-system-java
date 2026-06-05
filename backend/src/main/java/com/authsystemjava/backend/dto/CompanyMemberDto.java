package com.authsystemjava.backend.dto;

import com.authsystemjava.backend.model.CompanyMember;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyMemberDto {
    private String id;
    private String userId;
    private String name;
    private String email;
    private String role;
    private LocalDateTime joinedAt;

    public static CompanyMemberDto from(CompanyMember member) {
        return CompanyMemberDto.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .name(member.getUser().getName())
                .email(member.getUser().getEmail())
                .role(member.getRole().name())
                .joinedAt(member.getJoinedAt())
                .build();
    }
}