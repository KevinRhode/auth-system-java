// UserSettingsRepository.java
package com.authsystemjava.backend.repository;

import com.authsystemjava.backend.dto.UserSettingsDto;
import com.authsystemjava.backend.model.User;
import com.authsystemjava.backend.model.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {
    @Query("SELECT us FROM UserSettings us JOIN FETCH us.user u LEFT JOIN FETCH u.companyMember cm LEFT JOIN FETCH cm.company LEFT JOIN FETCH u.sessions WHERE u.id = :id")
    Optional<UserSettingsDto> findByUserIdDto(@Param("id") String id);
}