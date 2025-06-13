package ru.bogatov.antiyoyo.server.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Document("maps")
public class GameMap {

    @MongoId
    private UUID id;
    private String name;
    private List<HexDto> map;
    private Integer playersCount;

}
