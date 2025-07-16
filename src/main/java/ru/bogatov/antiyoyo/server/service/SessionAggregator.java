package ru.bogatov.antiyoyo.server.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.server.dto.UiMessage;
import ru.bogatov.antiyoyo.server.events.GameStartedEvent;
import ru.bogatov.antiyoyo.server.events.UserConnectedEvent;
import ru.bogatov.antiyoyo.server.events.UserDisconnectedEvent;
import ru.bogatov.antiyoyo.server.job.IlluminatePlayerTask;
import ru.bogatov.antiyoyo.server.job.TaskSchedulingService;
import ru.bogatov.antiyoyo.server.repository.SessionRepository;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class SessionAggregator {

    private final SimpSessionStorage simpSessionStorage;
    private final SessionRepository sessionRepository;
    private final Map<SimpSessionStorage.UserAndSession, UUID> userSessionToIlluminateTask = new HashMap<>();
    private final TaskSchedulingService taskSchedulingService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    @Async("gameTaskExecutor")
    public void handleUserConnected(UserConnectedEvent event) {
        log.info("Aggregator handle joined: u {}, s {}", event.getUserId(), event.getGameSessionId());
        UUID userId = UUID.fromString(event.getUserId());
        sessionRepository.getSession(UUID.fromString(event.getGameSessionId()))
                .getPlayers()
                .values().stream()
                .filter(p -> userId.equals(p.getUserId())).findFirst()
                .ifPresent(player -> cancelIlluminateTask(UUID.fromString(event.getUserId()), UUID.fromString(event.getGameSessionId()), player.getColor()));

    }

    @EventListener
    @Async("gameTaskExecutor")
    public void handleGameStarted(GameStartedEvent event) {
        log.info("Aggregator game started : s {}", event.getGameSessionId());
        event.getUsersId().forEach(user -> {
            if (!simpSessionStorage.isUserConnectedToSession(user.getFirst(), event.getGameSessionId())) {
                scheduleIlluminateTask(user.getFirst(), event.getGameSessionId(), user.getSecond());
            }
        }
        );
    }

    @EventListener
    @Async("gameTaskExecutor")
    public void handleUserDisconnected(UserDisconnectedEvent event) {
        log.info("Aggregator handle leave: u {}, s {}", event.getUserId(), event.getGameSessionId());
        GameSession gameSession = sessionRepository.getSession(UUID.fromString(event.getGameSessionId()));
        if (gameSession == null) {
            return;
        }
        UUID userId = UUID.fromString(event.getUserId());
        Player player = gameSession.getPlayers().values().stream().filter(p -> userId.equals(p.getUserId())).findFirst().orElse(null);
        if (gameSession.isStarted() && gameSession.getWinnerId() == null && player != null && !player.isIlluminated()) {
            scheduleIlluminateTask(player.getUserId(), UUID.fromString(event.getGameSessionId()), player.getColor());
        }
    }

    private void scheduleIlluminateTask(UUID userId, UUID gameSessionId, HexColor color) {
        messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", gameSessionId.toString()), UiMessage.left(color));
        UUID taskId = UUID.randomUUID();
        taskSchedulingService.scheduleTask(taskId.toString(),
                new IlluminatePlayerTask(messagingTemplate, sessionRepository, gameSessionId, userId),
                OffsetDateTime.now().toInstant().plusSeconds(10));
        userSessionToIlluminateTask.put(new SimpSessionStorage.UserAndSession(userId.toString(), gameSessionId.toString()), taskId);
        log.info("Created illuminate task for {} and session {}", userId, gameSessionId);
    }

    private void cancelIlluminateTask(UUID userId, UUID gameSessionId, HexColor color) {
        var key = new SimpSessionStorage.UserAndSession(userId.toString(), gameSessionId.toString());
        UUID taskId = userSessionToIlluminateTask.get(key);
        if (taskId != null) {
            taskSchedulingService.cancelTask(taskId.toString());
        }

        messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", gameSessionId.toString()), UiMessage.joined(color));
        log.info("Canceled illuminate task for {} and session {}", userId, gameSessionId);
    }

}
