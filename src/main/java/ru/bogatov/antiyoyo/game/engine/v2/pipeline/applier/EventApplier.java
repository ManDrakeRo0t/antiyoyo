package ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.MoveEvent;

public interface EventApplier<T extends MoveEvent> {

    Class<T> getEventType();

    void apply(MoveContext context, T event);
}
