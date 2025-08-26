package ru.bogatov.antiyoyo.game.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bogatov.antiyoyo.game.model.common.Color;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HexSnapshot {
    Vector3 vector;
    Color color;
    EntityType entityType;
    Color entityOwnerColor;
    Currency storage;
    Boolean isDronesAvailable;
    Integer dronesLimit;
    Integer defenseLevel;
    Boolean isMoved;
    Integer fireStage;
    Boolean glue;
}
