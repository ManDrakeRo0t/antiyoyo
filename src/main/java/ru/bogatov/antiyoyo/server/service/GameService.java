package ru.bogatov.antiyoyo.server.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.game.engine.GameEngine;
import ru.bogatov.antiyoyo.game.engine.util.HexMapGenerator;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.HexColor;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.game.model.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;
import ru.bogatov.antiyoyo.server.domain.EventType;
import ru.bogatov.antiyoyo.server.domain.GameEvent;
import ru.bogatov.antiyoyo.server.repository.SessionRepository;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class GameService {

    private final GameEngine gameEngine = new GameEngine();
    private final SessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @SneakyThrows
    public void handleEvent(String sessionId, GameEvent event) {


        GameSession session = sessionRepository.getSession(UUID.fromString(sessionId));

        if (session == null) {
            session = createSession(UUID.fromString(sessionId));
            sessionRepository.saveSession(session);
        }

        if (Objects.requireNonNull(event.getType()) == EventType.FINISH_TURN) {
            gameEngine.endMove(session);
            messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", sessionId), session);
            return;
        }

//        log.info("Session : {}", session);
//        log.info("Event : {}", event);

        if (Objects.requireNonNull(event.getType()) == EventType.UNDO_MOVE) {
            gameEngine.undoMove(session);
            messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", sessionId), session);
            return;
        }

        if (Objects.requireNonNull(event.getType()) == EventType.MOVE) {
            gameEngine.makeMove(session, event.getMove());
        }

        if (Objects.requireNonNull(event.getType()) == EventType.BEFORE_MOVE) {
          gameEngine.handleBeforeMoveClick(session, event);
          messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", sessionId), session);
          return;
        }

        messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", sessionId), session);
    }

    public GameSession createSession(UUID id) {
        GameSession session = new GameSession();
        session.setId(id);
        session.setPlayers(Map.of(0, new Player(UUID.randomUUID(), HexColor.RED, null)));
        session.setName("Test");
        session.setCurrentPlayerMove(0);
        session.setStartTime(OffsetDateTime.now());
        session.setMap(HexMapGenerator.generateEmptyMap(5));
        session.getMap().get(Vector3.from(0,0,0)).setColor(HexColor.RED);
        session.getMap().get(Vector3.from(2,-2,0)).setColor(HexColor.RED);
        session.getMap().get(Vector3.from(0,0,0)).setEntity(new TownHall(20, 0));
        session.getMap().get(Vector3.from(2,-2,0)).setEntity(new TownHall(2, 0));
        return session;
    }
}
