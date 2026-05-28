package com.authsystemjava.backend.service;

import com.authsystemjava.backend.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionCleanupService {

    private static final Logger log = LoggerFactory.getLogger(SessionCleanupService.class);
    private final SessionRepository sessionRepository;

    @Transactional
    @Scheduled(fixedRate = 3600000) // every hour
    public void purgeExpiredSessions() {
        int deleted = sessionRepository.deleteAllByExpiresAtBefore(LocalDateTime.now());
        if (deleted > 0) {
            log.info("Purged {} expired sessions", deleted);
        }
    }
}