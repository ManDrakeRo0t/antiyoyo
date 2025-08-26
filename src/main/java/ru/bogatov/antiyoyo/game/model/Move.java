package ru.bogatov.antiyoyo.game.model;

import lombok.Data;
import ru.bogatov.antiyoyo.game.model.common.Color;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.EntityType;

@Data
public class Move {

    private Color color;
    private Vector3 clickedHex;
    private EntityType entityType;

}
