package ru.bogatov.antiyoyo.game.model;

import lombok.Data;
import ru.bogatov.antiyoyo.game.model.entity.EntityType;

@Data
public class Move {

    private Boolean redactorMode;
    private Integer player;
    private Vector3 townHall;
    private Vector3 from;
    private Vector3 to;
    private EntityType entityType;

}
