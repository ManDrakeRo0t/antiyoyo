package ru.bogatov.antiyoyo.server.repository;

import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class GameHistoryRepository {

    private final MongoTemplate mongoTemplate;

    public void create() {

    }

}
