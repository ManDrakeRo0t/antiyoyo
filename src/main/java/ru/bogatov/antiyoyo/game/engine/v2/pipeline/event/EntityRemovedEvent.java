package ru.bogatov.antiyoyo.game.engine.v2.pipeline.event;

import ru.bogatov.antiyoyo.game.model.common.Hex;

public record EntityRemovedEvent(Hex source, boolean preserveColor) implements MoveEvent {
}
