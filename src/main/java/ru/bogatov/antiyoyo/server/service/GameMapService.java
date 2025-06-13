package ru.bogatov.antiyoyo.server.service;

import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.server.domain.GameMap;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class GameMapService {

    private final MongoTemplate mongoTemplate;

    public GameMap saveMap(GameMap gameMap) {
        gameMap.setId(UUID.randomUUID());
        return mongoTemplate.save(gameMap);
    }

    public List<GameMap> getMaps() {
        Query query = new Query();
        query.fields().include("id", "name", "playersCount");
        return mongoTemplate.find(query, GameMap.class);
    }

    public GameMap getById(UUID id) {
        return mongoTemplate.findOne(new Query().addCriteria(Criteria.where("id").is(id)), GameMap.class);
    }

}
