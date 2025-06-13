package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;

@Data
public abstract class Entity {

    private Boolean movedOnThisTurn;

    public abstract EntityType getType();

    public Integer getBalanceChange() {
        return 0;
    }

}
