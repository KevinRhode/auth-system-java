package com.authsystemjava.backend.service;

import com.authsystemjava.backend.dto.SessionDto;
import com.authsystemjava.backend.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;

    public List<SessionDto> getUserSessions(String userId) {
        return sessionRepository.findAllByUserId(userId)
                .stream()
                .map(SessionDto::from)
                .toList();
    }
    
    @Transactional
    public void revokeSession(String sessionId, String userId) {
        sessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getUser().getId().equals(userId)) {
                sessionRepository.delete(session);
            }
        });
    }

    public void revokeAllSessions(String userId) {
        sessionRepository.deleteAllByUserId(userId);
    }
}