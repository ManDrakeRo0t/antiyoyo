package ru.bogatov.antiyoyo.game.engine.v2.pipeline.event;

import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;

public record ResourceHarvestedEvent(Hex target, Currency reward) implements MoveEvent {
}
