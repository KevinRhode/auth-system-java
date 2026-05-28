package com.authsystemjava.backend.repository;

import com.authsystemjava.backend.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {
    Optional<Session> findByToken(String token);
    List<Session> findAllByUserId(String userId);

    @Transactional
    void deleteByToken(String token);

    @Transactional
    void deleteAllByUserId(String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Session s WHERE s.expiresAt < :now")
    int deleteAllByExpiresAtBefore(LocalDateTime now);
}