package ru.bogatov.antiyoyo.server.repository;

import org.springframework.stereotype.Repository;
import ru.bogatov.antiyoyo.game.model.GameSession;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SessionRepository {

    Map<UUID, GameSession> storage = new ConcurrentHashMap<>();

    public void saveSession(GameSession session) {
        storage.put(session.getId(), session);
    }

    public GameSession getSession(UUID id) {
        return storage.get(id);
    }


}
