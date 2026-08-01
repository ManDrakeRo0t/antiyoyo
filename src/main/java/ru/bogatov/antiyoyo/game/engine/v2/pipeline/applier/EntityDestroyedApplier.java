package ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.EntityDestroyedEvent;
import ru.bogatov.antiyoyo.game.engine.v2.service.DefenseService;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.Entity;
import ru.bogatov.antiyoyo.game.model.entity.Field;
import ru.bogatov.antiyoyo.game.model.entity.Interactable;

public class EntityDestroyedApplier implements EventApplier<EntityDestroyedEvent> {

    @Override
    public Class<EntityDestroyedEvent> getEventType() {
        return EntityDestroyedEvent.class;
    }

    @Override
    public void apply(MoveContext context, EntityDestroyedEvent event) {
        Hex hex = event.target();
        Entity oldEntity = hex.getEntity();
        HexColor oldColor = hex.getColor();
        hex.setEntity(new Field());
        if (oldEntity instanceof Interactable) {
            DefenseService.updateDefenseLevelForColor(context.getSession().getMap(), hex, oldColor);
        }
    }
}
