package ru.bogatov.antiyoyo.server.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import ru.bogatov.antiyoyo.game.engine.IGameEngine;
import ru.bogatov.antiyoyo.server.repository.SessionRepository;
import ru.bogatov.antiyoyo.server.service.GameService;

import java.util.UUID;

@Data
@Slf4j
@AllArgsConstructor
public class EndMoveTask implements Runnable {

    private UUID sessionId;
    private SessionRepository sessionRepository;
    private IGameEngine gameEngine;
    private SimpMessagingTemplate messagingTemplate;
    private GameService gameService;

    @Override
    public void run() {
        log.info("End move evaluated for session : {}", sessionId);

        gameEngine.endMove(sessionRepository.getSession(sessionId));
        var session = sessionRepository.getSession(sessionId);
        if (session != null) {
            gameService.scheduleEndMoveTask(sessionId, null);
            log.info("Sending session {}", sessionRepository.getSession(sessionId).getId());
            try {
                messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", sessionId.toString()), session);
            } catch (RuntimeException e) {
                log.error("Error send : {}", e.getMessage());
            }
        }


    }
}
