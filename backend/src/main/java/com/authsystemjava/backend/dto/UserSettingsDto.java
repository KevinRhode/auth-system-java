package com.authsystemjava.backend.dto;

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
    private Long id;
    private String theme;
    private Long companyId;

    public static UserSettingsDto from(UserSettings settings) {
        return UserSettingsDto.builder()
                .id(settings.getId())
                .theme(settings.getTheme())
                .companyId(Long.valueOf(settings.getCompany().getId()))
                .build();
    }
}

