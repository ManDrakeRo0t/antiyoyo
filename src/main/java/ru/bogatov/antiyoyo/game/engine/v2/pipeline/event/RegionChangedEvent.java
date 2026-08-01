package ru.bogatov.antiyoyo.game.engine.v2.pipeline.event;

import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.Entity;

public record RegionChangedEvent(HexColor oldColor, Entity oldEntity) implements MoveEvent {
}
