package ru.bogatov.antiyoyo.server.repository;

import org.springframework.stereotype.Repository;
import ru.bogatov.antiyoyo.game.engine.GameEngine;
import ru.bogatov.antiyoyo.game.model.GameSession;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class SessionRepository {

    Map<UUID, GameSession> storage = new HashMap<>();

    public GameSession saveSession(GameSession session) {
        storage.put(session.getId(), session);
        return session;
    }

    public GameSession getSession(UUID id) {
        return storage.get(id);
    }


}
