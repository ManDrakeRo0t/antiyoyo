package ru.bogatov.antiyoyo.game.engine.v2.pipeline.event;

import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.Entity;

public record EntityPlacedEvent(Hex target, Entity entity, HexColor newColor, Boolean movedOnThisTurn) implements MoveEvent {

    public EntityPlacedEvent(Hex target, Entity entity, HexColor newColor) {
        this(target, entity, newColor, null);
    }
}
