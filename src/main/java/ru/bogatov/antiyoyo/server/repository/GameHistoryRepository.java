package ru.bogatov.antiyoyo.server.repository;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import ru.bogatov.antiyoyo.server.domain.GameHistory;

import java.util.List;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class GameHistoryRepository {

    private final MongoTemplate mongoTemplate;

    public void save(GameHistory gameHistory) {
        gameHistory.setId(UUID.randomUUID());
        mongoTemplate.save(gameHistory);
    }

    public List<GameHistory> findUsersHistory(UUID userId) {
        Query query = new Query().addCriteria(Criteria.where("players.userId").is(userId)).with(Sort.by(Sort.Direction.DESC, "endTime"));
        return mongoTemplate.find(query, GameHistory.class);
    }

}
