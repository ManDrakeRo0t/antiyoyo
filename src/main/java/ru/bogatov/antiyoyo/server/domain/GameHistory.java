package ru.bogatov.antiyoyo.server.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.UUID;

@Data
@Document("history")
@Builder
public class GameHistory {

    @MongoId
    private UUID id;
    private UUID userId;
    private String sessionName;
    private Integer playersCount;
    private Boolean win;
    private Integer place;
    private Integer ratingDelta;

}
