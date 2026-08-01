package ru.bogatov.antiyoyo.game.engine.v2.pipeline.event;

import ru.bogatov.antiyoyo.game.model.common.Hex;

public record EntityDestroyedEvent(Hex target) implements MoveEvent {
}
