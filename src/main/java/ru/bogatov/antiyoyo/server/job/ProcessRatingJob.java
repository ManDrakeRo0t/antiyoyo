package ru.bogatov.antiyoyo.server.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.server.config.RatingConfig;
import ru.bogatov.antiyoyo.server.domain.GameHistory;
import ru.bogatov.antiyoyo.server.domain.HistoryPlayerEntry;
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

    @Scheduled(fixedRate = 2 * 60 * 1000)
    public void processRating() {
        log.info("Started rating process job");

        sessionRepository.getAllSessionForRationUpdate().forEach(this::processRating);

    }

    public int getUserRating(User user) {
        return user.getRating() == null || user.getRating() == 0 ? 1 : user.getRating();
    }

    public void processRating(GameSession session) {

        GameHistory gameHistory = new GameHistory();
        gameHistory.setSessionId(session.getId());
        gameHistory.setStartTime(session.getStartTime());
        gameHistory.setEndTime(session.getEndTime());
        gameHistory.setPlayersCount(session.getPlayers().size());
        gameHistory.setSessionName(session.getName());
        gameHistory.setPlayers(new ArrayList<>());

        List<Player> players = session.getPlayers().values().stream().sorted(Comparator.comparing(Player::getPlace)).toList();
        List<User> users = userRepository.findUsersByIds(players.stream().map(Player::getUserId).toList());
        Map<UUID, User> usersMap = users.stream().collect(Collectors.toMap(User::getId, identity()));
        Map<UUID, Integer> userIdToRating = users
                .stream()
                .collect(Collectors.toMap(User::getId, this::getUserRating));
        Map<Integer, RatingConfig.PlayerClass> ratingDeltaTable = RatingConfig.baseDeltaRating.get(players.size());

        Integer minRating = users.stream().map(this::getUserRating).min(Integer::compare).orElse(0);
        Integer maxRating = users.stream().map(this::getUserRating).max(Integer::compare).orElse(0);
        Integer sumRating = users.stream().map(this::getUserRating).reduce(Integer::sum).orElse(0);
        Integer avgRating = sumRating / players.size();

        players.forEach(player -> {

            HistoryPlayerEntry playerEntry = new HistoryPlayerEntry();

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
            User user = usersMap.get(player.getUserId());
            if (deltaRating > 0) {
                finalDeltaRating = (int) (deltaRating * deltaForUp);
            } else {
                finalDeltaRating = (int) (deltaRating * deltaForDown * RatingConfig.getDeltaMultiplier(getUserRating(user)));
            }
            if (player.getPlace() == 1) {
                int oldWin = user.getWinGames() == null ? 0 : user.getWinGames();
                user.setWinGames(oldWin + 1);
            }
            int newRating = Math.max(user.getRating() + finalDeltaRating, 0);
            userRepository.updateUserStats(player.getUserId(), newRating, user.getWinGames(), user.getTotalGames() + 1);
            playerEntry.setRankDelta(finalDeltaRating);
            playerEntry.setPlace(player.getPlace());
            playerEntry.setUserId(player.getUserId());
            gameHistory.getPlayers().add(playerEntry);
        });

        gameHistoryRepository.save(gameHistory);
        session.setRatingProcessed(true);

    }

}
