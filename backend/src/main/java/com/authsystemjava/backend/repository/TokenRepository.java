package com.authsystemjava.backend.repository;

import com.authsystemjava.backend.model.Token;
import com.authsystemjava.backend.model.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {
    Optional<Token> findByTokenAndType(String token, TokenType type);

    @Transactional
    void deleteByUserIdAndType(String userId, TokenType type);
}