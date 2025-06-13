package ru.bogatov.antiyoyo.server.domain;

import lombok.Data;
import ru.bogatov.antiyoyo.game.model.HexColor;
import ru.bogatov.antiyoyo.game.model.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.EntityType;

@Data
public class HexDto {
    private Vector3 vector;
    private HexColor color;
    private EntityType entity;
}
