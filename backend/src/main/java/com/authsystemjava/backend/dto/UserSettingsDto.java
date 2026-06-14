package com.authsystemjava.backend.dto;

import com.authsystemjava.backend.model.Company;
import com.authsystemjava.backend.model.CompanyMember;
import com.authsystemjava.backend.model.User;
import com.authsystemjava.backend.model.UserSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDto {
    private String id;
    private String language;
    private String theme;
    private User user;
    private CompanyDto company;

    public static UserSettingsDto from(UserSettings settings) {
        return UserSettingsDto.builder()
                .id(settings.getId())
                .language(settings.getLanguage())
                .theme(settings.getTheme())
                .user(settings.getUser())
                .company(settings.getCompany() != null ? CompanyDto.from(settings.getCompany()) : null)
                .build();
    }
}

