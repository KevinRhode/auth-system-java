// SessionRepository.java
package com.authsystemjava.backend.repository;

import com.authsystemjava.backend.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

public interface SessionRepository extends JpaRepository<Session, String> {
    Optional<Session> findByToken(String token);
    List<Session> findAllByUserId(String userId);

    @Transactional
    void deleteByToken(String token);
    
    @Transactional
    void deleteAllByUserId(String userId);
}