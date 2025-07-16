package ru.bogatov.antiyoyo.server.service;

import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.server.dto.SessionResponse;
import ru.bogatov.antiyoyo.server.job.TaskSchedulingService;
import ru.bogatov.antiyoyo.server.repository.SessionRepository;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SimpUserRegistry simpUserRegistry;
    private final TaskSchedulingService taskSchedulingService;

    public List<SessionResponse> getActiveSessions() {
        return sessionRepository.getNotStartedSessions().stream().map(this::enrichToResponse)
                .sorted(Comparator.comparingInt(session -> session.getTotalUsers() - session.getConnectedUsers())).toList();
    }

    public void saveSessionToBase(UUID sessionId) {
        GameSession gameSession = sessionRepository.getSession(sessionId);
        gameSession.setLeftSecondsToMove((int) (gameSession.getEndMoveTime().getEpochSecond() - Instant.now().getEpochSecond()));
        sessionRepository.saveSessionToBase(sessionRepository.getSession(sessionId));
        taskSchedulingService.cancelTask(gameSession.getSkipMoveTaskId().toString());
        sessionRepository.removeSession(sessionId);
    }

    public GameSession restoreSession(UUID sessionId) {
        GameSession gameSession = sessionRepository.restoreSession(sessionId);
        sessionRepository.saveSession(gameSession);
        return gameSession;
    }

    public Set<SimpSubscription> findSubscriptions(String sessionId) {
        return simpUserRegistry.findSubscriptions(subscription -> StringUtils.isEmpty(sessionId) ? true : subscription.getDestination().contains(sessionId));
    }

    private SessionResponse enrichToResponse(GameSession gameSession) {
        SessionResponse response = new SessionResponse();
        response.setId(gameSession.getId());
        response.setConnectedUsers((int) gameSession.getPlayers().values().stream().filter(p -> p.getUserId() != null).count());
        response.setName(gameSession.getName());
        response.setTotalUsers(gameSession.getPlayers().size());
        response.setSetting(gameSession.getSetting());
        return response;
    }

}
