package ru.bogatov.antiyoyo.game.engine.v2.pipeline.stage;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.PipelineStage;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier.EventApplierRegistry;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.MoveEvent;

import java.util.List;

public class EventApplicationStage implements PipelineStage {

    private final EventApplierRegistry applierRegistry;

    public EventApplicationStage(EventApplierRegistry applierRegistry) {
        this.applierRegistry = applierRegistry;
    }

    @Override
    public void execute(MoveContext context) {
        List<MoveEvent> events = context.getEvents();
        for (MoveEvent event : events) {
            applierRegistry.apply(context, event);
        }
    }
}
