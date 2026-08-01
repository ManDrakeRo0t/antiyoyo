package ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.StorageChangedEvent;
import ru.bogatov.antiyoyo.game.engine.v2.service.RegionService;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

public class StorageChangedApplier implements EventApplier<StorageChangedEvent> {

    @Override
    public Class<StorageChangedEvent> getEventType() {
        return StorageChangedEvent.class;
    }

    @Override
    public void apply(MoveContext context, StorageChangedEvent event) {
        Hex hex = RegionService.findTownHallById(context.getSession().getMap(), event.townHallId());
        if (hex != null && hex.getEntity() instanceof TownHall townHall) {
            townHall.getStorage().add(event.delta());
        }
    }
}
