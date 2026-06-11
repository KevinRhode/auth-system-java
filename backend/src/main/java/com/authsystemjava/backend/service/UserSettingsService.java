package com.authsystemjava.backend.service;

import com.authsystemjava.backend.dto.UserSettingsDto;
import com.authsystemjava.backend.model.User;
import com.authsystemjava.backend.model.UserSettings;
import com.authsystemjava.backend.repository.UserRepository;
import com.authsystemjava.backend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    @Transactional(readOnly = true)
    public UserSettingsDto getSettings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserSettings settings = userSettingsRepository.findByUserId(Long.valueOf(user.getId()))
                .orElseThrow(() -> new RuntimeException("User settings not found"));

        return UserSettingsDto.builder()
                .id(Long.valueOf(user.getId()))
                .theme(settings.getTheme())
                .companyId(Long.valueOf(settings.getCompany().getId()))
                .build();
    }
}
