package ru.bogatov.antiyoyo.game.model.entity;

public abstract class Entity {

    private boolean shouldInteract;

    public abstract EntityType getType();

    public Integer getBalanceChange() {
        return 0;
    }

}
