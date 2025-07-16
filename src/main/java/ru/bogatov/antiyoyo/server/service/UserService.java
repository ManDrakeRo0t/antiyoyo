package ru.bogatov.antiyoyo.server.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.server.domain.GameHistory;
import ru.bogatov.antiyoyo.server.domain.User;
import ru.bogatov.antiyoyo.server.dto.UserDto;
import ru.bogatov.antiyoyo.server.repository.GameHistoryRepository;
import ru.bogatov.antiyoyo.server.repository.SessionRepository;
import ru.bogatov.antiyoyo.server.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GameHistoryRepository gameHistoryRepository;
    private final SessionRepository sessionRepository;

    public List<UserDto> getTopRatingUsers() {
        return userRepository.getTopRatingUsers()
                .stream()
                .map(user -> UserDto.builder().login(user.getLogin()).rating(user.getRating()).build())
                .toList();
    }

    public UUID findActiveGameSession(UUID userId) {
        GameSession gameSession = sessionRepository.getActiveSessionForUser(userId.toString());
        return gameSession == null ? null : gameSession.getId();
    }

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
