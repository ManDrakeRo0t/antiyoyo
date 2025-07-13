package ru.bogatov.antiyoyo.server.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.server.domain.GameHistory;
import ru.bogatov.antiyoyo.server.domain.User;
import ru.bogatov.antiyoyo.server.dto.UserDto;
import ru.bogatov.antiyoyo.server.repository.GameHistoryRepository;
import ru.bogatov.antiyoyo.server.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GameHistoryRepository gameHistoryRepository;

    public UserDto getUserWithHistory(UUID userId) {

        User user = userRepository.getUsersById(userId);
        List<GameHistory> userHistory = gameHistoryRepository.findUsersHistory(userId);

        return UserDto.builder()
                .login(user.getLogin())
                .rating(user.getRating())
                .totalGames(user.getTotalGames())
                .winGames(user.getWinGames())
                .userHistory(userHistory).build();

    }

}
