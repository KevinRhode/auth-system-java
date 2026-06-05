package com.authsystemjava.backend.service;

import com.authsystemjava.backend.dto.UpdateRoleRequest;
import com.authsystemjava.backend.dto.UserDto;
import com.authsystemjava.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserDto::from)
                .toList();
    }

    public UserDto getUserById(String id) {
        return userRepository.findById(id)
                .map(UserDto::from)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public UserDto updateRole(String id, UpdateRoleRequest request) {
        return userRepository.findById(id).map(user -> {
            user.setRole(request.getRole());
            userRepository.save(user);
            log.info("Role updated for user: {} to {}", user.getEmail(), request.getRole());
            return UserDto.from(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(String id) {
        userRepository.findById(id).ifPresentOrElse(user -> {
            userRepository.delete(user);
            log.info("User deleted: {}", user.getEmail());
        }, () -> { throw new RuntimeException("User not found"); });
    }

    public UserDto getMe(String userId) {
        return userRepository.findById(userId)
                .map(UserDto::from)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}