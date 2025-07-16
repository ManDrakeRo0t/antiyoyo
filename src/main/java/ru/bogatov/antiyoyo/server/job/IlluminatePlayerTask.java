package ru.bogatov.antiyoyo.server.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.server.dto.UiMessage;
import ru.bogatov.antiyoyo.server.repository.SessionRepository;

import java.util.Set;
import java.util.UUID;

import static ru.bogatov.antiyoyo.game.engine.util.MapUtils.getPlayersCount;

@Data
@Slf4j
@AllArgsConstructor
public class IlluminatePlayerTask implements Runnable {

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionRepository sessionRepository;
    private final UUID gameSessionId;
    private final UUID userId;

    @Override
    public void run() {

        GameSession gameSession = sessionRepository.getSession(gameSessionId);
        if (gameSession != null) {
            Player player = gameSession.getPlayers()
                    .values()
                    .stream()
                    .filter(p -> p.getUserId().equals(userId)).findFirst().orElse(null);
            if (player == null) {
                log.warn("Player is null, strange situation");
                return;
            }

            log.info("Player illuminated : {} for session {}", player.getColor(), gameSession.getId());
            player.setIlluminated(true);
            Pair<Integer, Set<HexColor>> playerCount = getPlayersCount(gameSession.getMap());
            player.setPlace(playerCount.getFirst());
            gameSession.getAliveUsersId().remove(userId.toString());
            messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", gameSession.getId().toString()), UiMessage.illuminated(player.getColor()));
        }

    }
}
