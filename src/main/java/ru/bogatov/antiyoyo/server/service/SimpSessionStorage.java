package ru.bogatov.antiyoyo.server.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.bogatov.antiyoyo.server.events.UserConnectedEvent;
import ru.bogatov.antiyoyo.server.events.UserDisconnectedEvent;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@Slf4j
@AllArgsConstructor
public class SimpSessionStorage {

    private final ApplicationEventPublisher eventPublisher;
    private final ConcurrentMap<String, UserAndSession> simpToUser = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> userIdToSimp = new ConcurrentHashMap<>();

    public void userConnected(String simpSession, String userId, String gameSession) {
        simpToUser.put(simpSession, new UserAndSession(userId, gameSession));
        userIdToSimp.put(userId, simpSession);
        eventPublisher.publishEvent(new UserConnectedEvent(this, userId, gameSession));
    }

    public void userDisconnected(String simpSession) {
        if (!simpToUser.containsKey(simpSession)) {
            log.error("Strange situation, simp session not found : {}", simpSession);
        }
        UserAndSession user = simpToUser.get(simpSession);
        simpToUser.remove(simpSession);
        userIdToSimp.remove(user.getUserId());
        eventPublisher.publishEvent(new UserDisconnectedEvent(this, user.getUserId(), user.getGameSession()));
    }

    public boolean isUserConnectedToSession(UUID userId, UUID sessionId) {
        if (userIdToSimp.containsKey(userId.toString())) {
            String simp = userIdToSimp.get(userId.toString());
            if (simpToUser.containsKey(simp)) {
                return sessionId.toString().equals(simpToUser.get(simp).getGameSession());
            }
        }
        return false;
    }


    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    static class UserAndSession {
        private String userId;
        private String gameSession;
    }

}
