package ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.ResourceHarvestedEvent;
import ru.bogatov.antiyoyo.game.engine.v2.service.RegionService;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

import java.util.UUID;

public class ResourceHarvestedApplier implements EventApplier<ResourceHarvestedEvent> {

    @Override
    public Class<ResourceHarvestedEvent> getEventType() {
        return ResourceHarvestedEvent.class;
    }

    @Override
    public void apply(MoveContext context, ResourceHarvestedEvent event) {
        TownHall townHall = resolveTownHall(context);
        if (townHall != null) {
            townHall.getStorage().add(event.reward());
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
