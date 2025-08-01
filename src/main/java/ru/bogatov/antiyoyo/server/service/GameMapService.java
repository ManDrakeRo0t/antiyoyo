package ru.bogatov.antiyoyo.server.service;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import ru.bogatov.antiyoyo.server.domain.GameMap;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@AllArgsConstructor
@Slf4j
public class GameMapService {

    private final MongoTemplate mongoTemplate;

    public GameMap saveMap(GameMap gameMap) {
        gameMap.setId(UUID.randomUUID());
        return mongoTemplate.save(gameMap);
    }

    public List<GameMap> getMaps() {
        Query query = new Query();
        query.fields().include("id", "name", "playersCount", "mapSize");
        return  mongoTemplate.find(query, GameMap.class);
    }

    @PostConstruct
    public void migrateAllMaps() {
        Query query = new Query();
        query.addCriteria(Criteria.where("mapSize").exists(false));
        List<GameMap> gameMaps = mongoTemplate.find(query, GameMap.class);
        log.info("Необходимо смигрировать {} карт", gameMaps.size());
        AtomicInteger countMigrate = new AtomicInteger();
        if (!gameMaps.isEmpty()) {
            gameMaps.forEach(map -> {
                map.setMapSize(map.getMap().size());
                mongoTemplate.replace(new Query().addCriteria(Criteria.where("id").is(map.getId())), map);
                countMigrate.getAndIncrement();
            });
        }
        log.info("Смигрировано {} карт", countMigrate.get());
    }

    public GameMap getById(UUID id) {
        return mongoTemplate.findOne(new Query().addCriteria(Criteria.where("id").is(id)), GameMap.class);
    }

}
