package ru.bogatov.antiyoyo.game.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.entity.Entity;

@Data
@Builder
@EqualsAndHashCode
public class Hex {

    private Vector3 vector;
    private Boolean isAvailable;
    private HexColor color;
    private Entity entity;
    private Integer defenseLevel;

}
