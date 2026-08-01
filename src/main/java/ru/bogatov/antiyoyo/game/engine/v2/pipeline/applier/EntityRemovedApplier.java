package ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.EntityRemovedEvent;
import ru.bogatov.antiyoyo.game.engine.v2.service.DefenseService;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.Entity;
import ru.bogatov.antiyoyo.game.model.entity.Field;
import ru.bogatov.antiyoyo.game.model.entity.Interactable;

public class EntityRemovedApplier implements EventApplier<EntityRemovedEvent> {

    @Override
    public Class<EntityRemovedEvent> getEventType() {
        return EntityRemovedEvent.class;
    }

    @Override
    public void apply(MoveContext context, EntityRemovedEvent event) {
        Hex hex = event.source();
        Entity oldEntity = hex.getEntity();
        HexColor oldColor = hex.getColor();
        hex.setEntity(new Field());
        if (!event.preserveColor()) {
            hex.setColor(HexColor.EMPTY);
        }
        if (oldEntity instanceof Interactable) {
            DefenseService.updateDefenseLevelForColor(context.getSession().getMap(), hex, oldColor);
        }
    }
}
