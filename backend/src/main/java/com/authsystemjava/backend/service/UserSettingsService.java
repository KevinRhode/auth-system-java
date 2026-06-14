package com.authsystemjava.backend.service;

import com.authsystemjava.backend.dto.CompanyDto;
import com.authsystemjava.backend.dto.UserSettingsDto;
import com.authsystemjava.backend.exception.ApiException;
import com.authsystemjava.backend.exception.ErrorCode;
import com.authsystemjava.backend.model.Company;
import com.authsystemjava.backend.model.User;
import com.authsystemjava.backend.model.UserSettings;
import com.authsystemjava.backend.repository.UserRepository;
import com.authsystemjava.backend.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;

    @Transactional
    public UserSettingsDto getOrCreateSettings(String userId) {
        return userSettingsRepository.findByUserIdDto(userId)
                .orElseGet(() -> createDefaultSettings(userId));
    }

    private UserSettingsDto createDefaultSettings(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        UserSettings settings = UserSettings.builder()
                .user(user)
                .theme("light")
                .build();

        userSettingsRepository.save(settings);
        log.info("Default settings created for user: {}", user.getEmail());

        return UserSettingsDto.from(settings);
    }


}
