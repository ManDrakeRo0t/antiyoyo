package ru.bogatov.antiyoyo.game.engine.v2.pipeline.event;

import ru.bogatov.antiyoyo.game.model.common.Currency;

import java.util.UUID;

public record StorageChangedEvent(UUID townHallId, Currency delta) implements MoveEvent {
}
