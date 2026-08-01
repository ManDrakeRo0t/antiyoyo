package ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.DronePlacedEvent;
import ru.bogatov.antiyoyo.game.engine.v2.service.DefenseService;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.entity.Drone;
import ru.bogatov.antiyoyo.game.model.entity.Interactable;

public class DronePlacedApplier implements EventApplier<DronePlacedEvent> {

    @Override
    public Class<DronePlacedEvent> getEventType() {
        return DronePlacedEvent.class;
    }

    @Override
    public void apply(MoveContext context, DronePlacedEvent event) {
        Hex hex = event.target();
        Drone drone = (Drone) event.entity();
        drone.setOwnerColor(event.ownerColor());
        hex.setEntity(drone);
        // hex color intentionally not changed to match legacy logic
        if (drone instanceof Interactable interactable) {
            DefenseService.updateDefenseLevel(context.getSession().getMap(), hex, interactable.getLevel(), hex.getColor());
        }
    }
}
