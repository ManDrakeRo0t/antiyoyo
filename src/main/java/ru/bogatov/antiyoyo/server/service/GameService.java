package ru.bogatov.antiyoyo.server.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.game.engine.GameEngine;
import ru.bogatov.antiyoyo.game.engine.util.EntityUtils;
import ru.bogatov.antiyoyo.game.engine.util.MapUtils;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.model.*;
import ru.bogatov.antiyoyo.server.domain.GameEvent;
import ru.bogatov.antiyoyo.server.domain.GameMap;
import ru.bogatov.antiyoyo.server.dto.SessionCreateRequest;
import ru.bogatov.antiyoyo.server.dto.SessionJoinRequest;
import ru.bogatov.antiyoyo.server.repository.SessionRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


@Service
@Slf4j
@AllArgsConstructor
public class GameService {

    private final GameEngine gameEngine = new GameEngine();
    private final SessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameMapService gameMapService;


    public void validateGameMap(GameMap gameMap) {
        gameEngine.validateSessionAndGetPlayersCount(createSessionFromMap(gameMap));
    }

    @SneakyThrows
    public void handleEvent(String sessionId, GameEvent event) {


        GameSession session = sessionRepository.getSession(UUID.fromString(sessionId));

        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }

        switch (event.getType()) {
            case MOVE -> gameEngine.makeMove(session, event.getMove());
            case UNDO_MOVE -> gameEngine.undoMove(session);
            case BEFORE_MOVE -> gameEngine.handleBeforeMoveClick(session, event);
            case FINISH_TURN -> gameEngine.endMove(session);
            case GET_SESSION -> {
            }
        }

        messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", sessionId), session);

    }

    public GameSession createSession(SessionCreateRequest request) {

        GameMap gameMap = gameMapService.getById(request.getMapId());

        GameSession gameSession = createSessionFromMap(gameMap);
        sessionRepository.saveSession(gameSession);
        return gameSession;
    }

    public GameSession joinSession(SessionJoinRequest request) {

        GameSession session = sessionRepository.getSession(request.getSessionId());

        Player player = session.getPlayers()
                .values().stream()
                .filter(p -> p.getColor() == request.getColor()).findFirst().orElse(null);
        if (player.getUserId() == null) {
            player.setUserId(request.getUserId());
        }
        messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", session.getId().toString()), session);
        return session;
    }

    public GameSession createSessionFromMap(GameMap gameMap) {
        GameSession gameSession = new GameSession();
        gameSession.setId(UUID.randomUUID());
        gameSession.setName("Session :" + gameMap.getName());
        gameSession.setCurrentPlayerMove(0);
        gameSession.setPlayers(new HashMap<>());
        Map<Vector3, Hex> map = new HashMap<>();
        gameMap.getMap().forEach(h -> {
            map.put(h.getVector(), Hex.builder()
                    .color(h.getColor())
                    .isAvailable(true)
                    .vector(h.getVector())
                    .displayDefence(false)
                    .defenseLevel(0)
                    .entity(EntityUtils.fromType(h.getEntity()))
                    .build());
        });
        gameSession.setMap(map);
        Pair<Integer, Set<HexColor>> playerCount = gameEngine.validateSessionAndGetPlayersCount(gameSession);
        gameMap.setPlayersCount(playerCount.getFirst());
        final int[] counter = {0};
        playerCount.getSecond().forEach(color -> {
            gameSession.getPlayers().put(counter[0], new Player(null, color, null, false));
            counter[0]++;
            MapUtils.getAllRegionsByColor(gameSession.getMap(), color)
                    .forEach(MapUtils::updateTownHallEconomy);
        });

        // change player
        MapUtils.restoreMap(gameSession);
        MapUtils.restoreDefence(gameSession);
        return gameSession;
    }
}
