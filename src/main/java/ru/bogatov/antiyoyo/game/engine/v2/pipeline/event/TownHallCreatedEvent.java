package ru.bogatov.antiyoyo.game.engine.v2.pipeline.event;

import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

public record TownHallCreatedEvent(Hex hex, TownHall townHall) implements MoveEvent {
}
