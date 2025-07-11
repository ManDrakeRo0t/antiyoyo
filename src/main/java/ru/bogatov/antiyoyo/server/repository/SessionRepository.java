package ru.bogatov.antiyoyo.server.repository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import ru.bogatov.antiyoyo.game.model.GameSession;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class SessionRepository {

    Map<UUID, GameSession> storage = new ConcurrentHashMap<>();

    private final MongoTemplate mongoTemplate;

    public SessionRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void saveSession(GameSession session) {
        storage.put(session.getId(), session);
    }

    public GameSession getSession(UUID id) {
        return storage.get(id);
    }

    public void removeSession(UUID id) {
        storage.remove(id);
    }

    public List<GameSession> getAllSessionForRationUpdate() {
        return storage.values().stream()
                .filter(session -> session.getWinnerId() != null && !session.isRatingProcessed())
                .toList();
    }

    public Collection<GameSession> getAllSessions() {
        return storage.values();
    }

    public List<GameSession> getNotStartedSessions() {
        return storage.values().stream().filter(session -> !session.isStarted()).toList();
    }

    public void saveSessionToBase(GameSession session) {
        mongoTemplate.save(session);
    }

    public GameSession restoreSession(UUID sessionId) {
        return mongoTemplate.findOne(new Query().addCriteria(Criteria.where("id").is(sessionId)), GameSession.class);
    }


}
