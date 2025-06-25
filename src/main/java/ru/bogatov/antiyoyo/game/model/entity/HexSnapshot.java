package ru.bogatov.antiyoyo.game.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HexSnapshot {
    Vector3 vector;
    HexColor color;
    EntityType entityType;
    HexColor entityOwnerColor;
    Currency storage;
    Boolean isDronesAvailable;
    Integer defenseLevel;
    Boolean isMoved;
}
