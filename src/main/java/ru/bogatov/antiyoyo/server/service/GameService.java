package ru.bogatov.antiyoyo.server.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.game.engine.GameEngine;
import ru.bogatov.antiyoyo.game.engine.IGameEngine;
import ru.bogatov.antiyoyo.game.engine.util.EntityUtils;
import ru.bogatov.antiyoyo.game.engine.util.MapUtils;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.engine.v2.GameEngineV2;
import ru.bogatov.antiyoyo.game.model.*;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.server.domain.GameEvent;
import ru.bogatov.antiyoyo.server.domain.GameMap;
import ru.bogatov.antiyoyo.server.dto.SessionCreateRequest;
import ru.bogatov.antiyoyo.server.dto.SessionJoinRequest;
import ru.bogatov.antiyoyo.server.events.GameStartedEvent;
import ru.bogatov.antiyoyo.server.exception.ErrorUtils;
import ru.bogatov.antiyoyo.server.job.EndMoveTask;
import ru.bogatov.antiyoyo.server.job.TaskSchedulingService;
import ru.bogatov.antiyoyo.server.repository.SessionRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;


@Service
@Slf4j
@AllArgsConstructor
public class GameService {

    private final IGameEngine gameEngine = new GameEngineV2();
    private final SessionRepository sessionRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameMapService gameMapService;
    private final TaskSchedulingService taskSchedulingService;
    private final SessionService sessionService;
    private final ApplicationEventPublisher eventPublisher;


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
            case FINISH_TURN -> {
                gameEngine.endMove(session);
                scheduleEndMoveTask(UUID.fromString(sessionId), null);
            }
            case GET_SESSION -> {
            }
        }
        session.setLastInteraction(OffsetDateTime.now());
        messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", sessionId), session);

    }

    public GameSession createSession(SessionCreateRequest request) {

        GameMap gameMap = gameMapService.getById(request.getMapId());
        GameSession gameSession = createSessionFromMap(gameMap);
        gameSession.setSetting(settingFromRequest(request));
        sessionRepository.saveSession(gameSession);
        return gameSession;

    }

    private GameSetting settingFromRequest(SessionCreateRequest request) {
        GameSetting setting = new GameSetting();
        setting.setGrave(request.getGrave());
        setting.setFarmsDensity(request.getFarmsDensity());
        setting.setSecondsToMove(request.getSecondToMove());
        setting.setUndoMove(request.getUndoMove());
        setting.setCut(request.getCut());
        setting.setDemolition(request.getDemolition());
        return setting;
    }

    public void scheduleEndMoveTask(UUID sessionId, Instant when) {
        GameSession gameSession = sessionRepository.getSession(sessionId);
        if (gameSession.getSkipMoveTaskId() != null) {
            taskSchedulingService.cancelTask(gameSession.getSkipMoveTaskId().toString());
        }
        Instant nextTimeToSkip = when != null ? when : Instant.now().plusSeconds(gameSession.getSetting().getSecondsToMove());
        UUID nextTaskId = UUID.randomUUID();
        gameSession.setSkipMoveTaskId(nextTaskId);
        gameSession.setEndMoveTime(nextTimeToSkip);
        taskSchedulingService.scheduleTask(nextTaskId.toString(), new EndMoveTask(sessionId, sessionRepository, gameEngine, messagingTemplate, this), nextTimeToSkip);
    }

    public void restoreSession(UUID id) {
        GameSession gameSession = sessionService.restoreSession(id);
        scheduleEndMoveTask(gameSession.getId(), Instant.now().plusSeconds(gameSession.getLeftSecondsToMove()));
    }

    public GameSession joinSession(SessionJoinRequest request) {
        if (sessionRepository.getActiveSessionForUser(request.getUserId().toString()) != null) {
            ErrorUtils.failWithBadRequest("You have ongoing session");
        }
        GameSession session = sessionRepository.getSession(request.getSessionId());
        Player player = session.getPlayers()
                .values().stream()
                .filter(p -> p.getColor() == request.getColor()).findFirst().orElse(null);
        session.getAliveUsersId().add(request.getUserId().toString());
        if (player.getUserId() == null) {
            player.setUserId(request.getUserId());
        }
        if (session.getPlayers().values().stream().allMatch(p -> p.getUserId() != null)) {
            scheduleEndMoveTask(request.getSessionId(), null);
            session.setStartTime(OffsetDateTime.now());
            session.setStarted(true);
            eventPublisher.publishEvent(
                    new GameStartedEvent(
                            this,
                            session.getId(),
                            session.getPlayers().values()
                                    .stream()
                                    .map(p -> Pair.of(p.getUserId(), p.getColor())).toList()
                    )
            );
        }
        messagingTemplate.convertAndSend("/topic/sessions.{session_id}.event.fetch".replace("{session_id}", session.getId().toString()), session);
        return session;
    }

    public GameSession createSessionFromMap(GameMap gameMap) {
        GameSession gameSession = new GameSession();
        gameSession.setId(UUID.randomUUID());
        gameSession.setName("Session : " + gameMap.getName());
        gameSession.setCurrentPlayerMove(0);
        gameSession.setPlayers(new HashMap<>());
        gameSession.setAliveUsersId(new HashSet<>());
        gameSession.setLastInteraction(OffsetDateTime.now());
        Map<Vector3, Hex> map = new HashMap<>();
        gameMap.getMap().forEach(h -> {
            map.put(h.getVector(), Hex.builder()
                    .color(h.getColor())
                    .isAvailable(true)
                    .glue(false)
                    .vector(h.getVector())
                    .displayDefence(false)
                    .defenseLevel(0)
                    .entity(EntityUtils.fromType(h.getEntity()))
                    .build());
        });
        gameSession.setMap(map);
        Pair<Integer, Set<HexColor>> playerCount = gameEngine.validateSessionAndGetPlayersCount(gameSession);
        gameMap.setPlayersCount(playerCount.getFirst());
        gameMap.setMapSize(map.size());
        final int[] counter = {0};
        playerCount.getSecond().forEach(color -> {
            gameSession.getPlayers().put(counter[0], new Player(null, color, null, false, null));
            counter[0]++;
            MapUtils.getAllRegionsByColor(gameSession.getMap(), color)
                    .forEach(MapUtils::updateTownHallEconomy);
        });

        // change player
        MapUtils.restoreMap(gameSession);
        MapUtils.restoreDefence(gameSession);
        MapUtils.updatePowerAndDronesAvailability(gameSession);
        return gameSession;
    }
}
