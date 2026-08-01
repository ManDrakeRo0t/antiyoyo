package ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.EntityMergedEvent;
import ru.bogatov.antiyoyo.game.engine.v2.service.DefenseService;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.entity.Entity;
import ru.bogatov.antiyoyo.game.model.entity.Interactable;

public class EntityMergedApplier implements EventApplier<EntityMergedEvent> {

    @Override
    public Class<EntityMergedEvent> getEventType() {
        return EntityMergedEvent.class;
    }

    @Override
    public void apply(MoveContext context, EntityMergedEvent event) {
        Hex hex = event.target();
        Entity result = event.resultEntity();
        Boolean previousMoved = hex.getEntity().getMovedOnThisTurn();
        hex.setEntity(result);
        if (previousMoved != null) {
            result.setMovedOnThisTurn(previousMoved);
        }
        if (result instanceof Interactable interactable) {
            DefenseService.updateDefenseLevel(context.getSession().getMap(), hex, interactable.getLevel(), hex.getColor());
        }
    }
}
