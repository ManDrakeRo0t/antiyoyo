package ru.bogatov.antiyoyo.server.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.bogatov.antiyoyo.game.engine.GameEngine;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.server.repository.SessionRepository;
import ru.bogatov.antiyoyo.server.service.GameService;

import java.util.UUID;

@Data
@Slf4j
@AllArgsConstructor
public class EndMoveTask implements Runnable {

    private UUID sessionId;
    private SessionRepository sessionRepository;
    private GameEngine gameEngine;
    private SimpMessagingTemplate messagingTemplate;
    private GameService gameService;

    @Override
    public void run() {
        log.info("End move evaluated for session : {}", sessionId);

        gameEngine.endMove(sessionRepository.getSession(sessionId));

        gameService.scheduleEndMoveTask(sessionId, null);

        messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", sessionId.toString()), sessionRepository.getSession(sessionId));

    }
}
