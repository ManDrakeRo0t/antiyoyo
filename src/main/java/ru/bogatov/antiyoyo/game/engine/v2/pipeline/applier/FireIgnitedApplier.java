package ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.FireIgnitedEvent;
import ru.bogatov.antiyoyo.game.engine.v2.service.DefenseService;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.Entity;
import ru.bogatov.antiyoyo.game.model.entity.Field;
import ru.bogatov.antiyoyo.game.model.entity.Fire;
import ru.bogatov.antiyoyo.game.model.entity.Interactable;

public class FireIgnitedApplier implements EventApplier<FireIgnitedEvent> {

    @Override
    public Class<FireIgnitedEvent> getEventType() {
        return FireIgnitedEvent.class;
    }

    @Override
    public void apply(MoveContext context, FireIgnitedEvent event) {
        Hex hex = event.target();
        Entity oldEntity = hex.getEntity();
        HexColor oldColor = hex.getColor();
        Fire fire = event.stage() == 3 ? new Fire() : new Fire(event.stage());
        hex.setEntity(fire);
        if (!(oldEntity instanceof Field)) {
            hex.setColor(HexColor.EMPTY);
        }
        DefenseService.updateDefenseLevel(context.getSession().getMap(), hex, 0, oldColor);
    }
}
