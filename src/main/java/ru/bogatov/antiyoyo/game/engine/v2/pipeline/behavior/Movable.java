package ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.MoveEvent;
import ru.bogatov.antiyoyo.game.model.common.Hex;

import java.util.List;
import java.util.Set;

public interface Movable extends EntityBehavior {

    Set<Hex> availableDestinations(MoveContext context, Hex source);

    List<MoveEvent> onMove(MoveContext context, Hex source, Hex target);
}
