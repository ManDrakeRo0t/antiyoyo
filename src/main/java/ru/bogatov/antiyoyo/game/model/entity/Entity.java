package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@Data
public abstract class Entity {

    private Boolean movedOnThisTurn;

    public abstract EntityType getType();

    public Currency getStorageChanges() {
        return Currency.EMPTY.clone();
    }

}
