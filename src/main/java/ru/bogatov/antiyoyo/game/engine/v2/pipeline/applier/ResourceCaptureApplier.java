package ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier;


import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.ResourceCaptureEvent;
import ru.bogatov.antiyoyo.game.engine.v2.service.RegionService;
import ru.bogatov.antiyoyo.game.engine.v2.util.EntityClassifier;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.Entity;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

import java.util.UUID;

public class ResourceCaptureApplier implements EventApplier<ResourceCaptureEvent> {

    @Override
    public Class<ResourceCaptureEvent> getEventType() {
        return ResourceCaptureEvent.class;
    }

    @Override
    public void apply(MoveContext context, ResourceCaptureEvent event) {
        Hex hex = event.target();
        Entity entity = event.entity();
        HexColor newColor = event.newColor();

        if (newColor != null) {
            hex.setColor(newColor);
        }

        //toDo захватывающий ресурс юнит - может двигаться еще
        if (EntityClassifier.isMoveableUnit(entity)) {
            entity.setMovedOnThisTurn(true);
        }
        if (event.movedOnThisTurn()) {
            entity.setMovedOnThisTurn(true);
        }

        TownHall townHall = resolveTownHall(context);
        if (townHall != null) {
            townHall.getStorageUpdate().add(hex.getEntity().getStorageChanges());
        }
    }

    private TownHall resolveTownHall(MoveContext context) {
        if (context.getSelectedTownHall() != null) {
            UUID id = context.getSelectedTownHall().getUuid();
            Hex hex = RegionService.findTownHallById(context.getSession().getMap(), id);
            return hex != null ? (TownHall) hex.getEntity() : null;
        }
        return null;
    }
}
