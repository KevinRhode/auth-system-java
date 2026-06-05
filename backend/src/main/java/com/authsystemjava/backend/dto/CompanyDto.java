package com.authsystemjava.backend.dto;

import com.authsystemjava.backend.model.Company;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private String id;
    private String name;
    private String slug;
    private LocalDateTime createdAt;
    private List<CompanyMemberDto> members;

    public static CompanyDto from(Company company) {
        return CompanyDto.builder()
                .id(company.getId())
                .name(company.getName())
                .slug(company.getSlug())
                .createdAt(company.getCreatedAt())
                .build();
    }

    public static CompanyDto withMembers(Company company, List<CompanyMemberDto> members) {
        CompanyDto dto = from(company);
        dto.setMembers(members);
        return dto;
    }
}