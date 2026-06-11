// UserSettingsRepository.java
package com.authsystemjava.backend.repository;

import com.authsystemjava.backend.model.User;
import com.authsystemjava.backend.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, String> {
    Optional<UserSettings> findByUserEmail(String email);
    Optional<UserSettings> findByUserId(Long userId);
}