package ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.MoveEvent;

import java.util.HashMap;
import java.util.Map;

public class EventApplierRegistry {

    private final Map<Class<? extends MoveEvent>, EventApplier<?>> appliers = new HashMap<>();

    public <T extends MoveEvent> void register(EventApplier<T> applier) {
        appliers.put(applier.getEventType(), applier);
    }

    @SuppressWarnings("unchecked")
    public <T extends MoveEvent> void apply(MoveContext context, T event) {
        EventApplier<T> applier = (EventApplier<T>) appliers.get(event.getClass());
        if (applier == null) {
            throw new IllegalStateException("No applier registered for event " + event.getClass().getSimpleName());
        }
        applier.apply(context, event);
    }

    public static EventApplierRegistry defaults() {
        EventApplierRegistry registry = new EventApplierRegistry();
        registry.register(new EntityPlacedApplier());
        registry.register(new EntityRemovedApplier());
        registry.register(new EntityMergedApplier());
        registry.register(new EntityDestroyedApplier());
        registry.register(new ResourceHarvestedApplier());
        registry.register(new ResourceCaptureApplier());
        registry.register(new StorageChangedApplier());
        registry.register(new FireIgnitedApplier());
        registry.register(new DronePlacedApplier());
        return registry;
    }
}
