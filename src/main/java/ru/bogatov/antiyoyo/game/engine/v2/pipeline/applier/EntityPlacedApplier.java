package ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.EntityPlacedEvent;
import ru.bogatov.antiyoyo.game.engine.v2.service.DefenseService;
import ru.bogatov.antiyoyo.game.engine.v2.service.EconomyService;
import ru.bogatov.antiyoyo.game.engine.v2.util.EntityClassifier;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.Drone;
import ru.bogatov.antiyoyo.game.model.entity.Entity;
import ru.bogatov.antiyoyo.game.model.entity.Interactable;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

public class EntityPlacedApplier implements EventApplier<EntityPlacedEvent> {

    @Override
    public Class<EntityPlacedEvent> getEventType() {
        return EntityPlacedEvent.class;
    }

    @Override
    public void apply(MoveContext context, EntityPlacedEvent event) {
        Hex hex = event.target();
        Entity entity = event.entity();
        HexColor newColor = event.newColor();

        hex.setEntity(entity);
        if (newColor != null) {
            hex.setColor(newColor);
        }
        if (entity instanceof Drone drone) {
            drone.setOwnerColor(hex.getColor());
        }

        if (event.movedOnThisTurn() != null) {
            entity.setMovedOnThisTurn(event.movedOnThisTurn());
        } else if (EntityClassifier.isMoveableUnit(entity)) {
            entity.setMovedOnThisTurn(true);
        } else {
            entity.setMovedOnThisTurn(null);
        }

        if (entity instanceof Interactable interactable) {
            DefenseService.updateDefenseLevel(context.getSession().getMap(), hex, interactable.getLevel(), hex.getColor());
        } else {
            DefenseService.updateDefenseLevel(context.getSession().getMap(), hex, 0, hex.getColor());
        }

        if (entity instanceof TownHall) {
            EconomyService.updateTownHallEconomy(context.getSession().getMap(), hex);
        }
    }
}
