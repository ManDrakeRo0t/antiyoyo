package ru.bogatov.antiyoyo.game.engine.v2.pipeline.event;

import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.entity.Entity;

public record EntityMergedEvent(Hex target, Entity resultEntity) implements MoveEvent {
}
