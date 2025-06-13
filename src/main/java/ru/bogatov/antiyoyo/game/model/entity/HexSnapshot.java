package ru.bogatov.antiyoyo.game.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bogatov.antiyoyo.game.model.HexColor;
import ru.bogatov.antiyoyo.game.model.Vector3;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HexSnapshot {
    Vector3 vector;
    HexColor color;
    EntityType entityType;
    Integer balance;
    Integer defenseLevel;
    Boolean isMoved;
}
