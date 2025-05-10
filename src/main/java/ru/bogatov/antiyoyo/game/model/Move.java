package ru.bogatov.antiyoyo.game.model;

import lombok.Data;
import ru.bogatov.antiyoyo.game.model.entity.Entity;

@Data
public class Move {

    private Integer player;
    private Vector3 from;
    private Vector3 to;
    private Entity entity;

}
