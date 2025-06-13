package ru.bogatov.antiyoyo.server.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.server.domain.User;
import ru.bogatov.antiyoyo.server.dto.AuthRequest;
import ru.bogatov.antiyoyo.server.repository.UserRepository;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public User register(AuthRequest authRequest) {
        return userRepository.saveUser(User.builder()
                .id(UUID.randomUUID())
                .login(authRequest.getLogin())
                .password(authRequest.getPassword())
                .build());
    }

    public User login(AuthRequest authRequest) {
        return userRepository.findByLoginAndPassword(authRequest.getLogin(), authRequest.getPassword());
    }

}
