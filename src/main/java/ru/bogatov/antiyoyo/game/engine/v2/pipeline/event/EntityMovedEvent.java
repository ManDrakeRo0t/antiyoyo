package ru.bogatov.antiyoyo.game.engine.v2.pipeline.event;

import ru.bogatov.antiyoyo.game.model.common.Hex;

public record EntityMovedEvent(Hex from, Hex to) implements MoveEvent {
}
