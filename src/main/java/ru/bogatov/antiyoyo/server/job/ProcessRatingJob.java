package ru.bogatov.antiyoyo.server.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.server.config.RatingConfig;
import ru.bogatov.antiyoyo.server.domain.User;
import ru.bogatov.antiyoyo.server.repository.GameHistoryRepository;
import ru.bogatov.antiyoyo.server.repository.SessionRepository;
import ru.bogatov.antiyoyo.server.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;

@Service
@Slf4j
@AllArgsConstructor
public class ProcessRatingJob {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final GameHistoryRepository gameHistoryRepository;

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void processRating() {

        sessionRepository.getAllSessionForRationUpdate().forEach(this::processRating);

    }

    public void processRating(GameSession session) {
        List<Player> players = session.getPlayers().values().stream().sorted(Comparator.comparing(Player::getPlace)).toList();
        List<User> users = userRepository.findUsersByIds(players.stream().map(Player::getUserId).toList());
        Map<UUID, User> usersMap = users.stream().collect(Collectors.toMap(User::getId, identity()));
        Map<UUID, Integer> userIdToRating = users
                .stream()
                .collect(Collectors.toMap(User::getId, User::getRating));
        Map<Integer, RatingConfig.PlayerClass> ratingDeltaTable = RatingConfig.baseDeltaRating.get(players.size());

        Integer minRating = users.stream().map(User::getRating).min(Integer::compare).orElse(0);
        Integer maxRating = users.stream().map(User::getRating).max(Integer::compare).orElse(0);
        Integer sumRating = users.stream().map(User::getRating).reduce(Integer::sum).orElse(0);
        Integer avgRating = sumRating / players.size();

        players.forEach(player -> {

            int playerBaseDelta;

            double proportionFromMax = (double) userIdToRating.get(player.getUserId()) / maxRating;
            double proportionFromSum = (double) userIdToRating.get(player.getUserId()) / sumRating;

            if (proportionFromMax < RatingConfig.MIDDLE_DELTA) {
                playerBaseDelta = ratingDeltaTable.get(player.getPlace()).getLow();
            } else if (proportionFromMax > RatingConfig.FAVORITE_DELTA) {
                playerBaseDelta = ratingDeltaTable.get(player.getPlace()).getFavorite();
            } else {
                playerBaseDelta = ratingDeltaTable.get(player.getPlace()).getMiddle();
            }

            double deltaK = RatingConfig.GLOBAL_DELTA * Math.abs(proportionFromMax - 1) + 1;
            double deltaS = deltaK * (avgRating - userIdToRating.get(player.getUserId()) + 1) / (maxRating - minRating + 1);

            double deltaForUp = Math.abs(1 - proportionFromSum);
            double deltaForDown = 1 + proportionFromSum;

            int deltaRating = (int) (playerBaseDelta + deltaS);
            int finalDeltaRating;
            if (deltaRating > 0) {
                finalDeltaRating = (int) (deltaRating * deltaForUp);
            } else {
                finalDeltaRating = (int) (deltaRating * deltaForDown);
            }
            User user = usersMap.get(player.getUserId());
            if (player.getPlace() == 0) {
                user.setWinGames(user.getWinGames() + 1);
            }
            userRepository.updateUserStats(player.getUserId(), user.getRating() + finalDeltaRating, user.getWinGames(), user.getTotalGames() + 1);

        });

    }

}
