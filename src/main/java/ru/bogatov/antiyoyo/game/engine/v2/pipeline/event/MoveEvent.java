package ru.bogatov.antiyoyo.game.engine.v2.pipeline.event;

public sealed interface MoveEvent permits
        EntityPlacedEvent,
        EntityRemovedEvent,
        EntityMovedEvent,
        EntityMergedEvent,
        EntityDestroyedEvent,
        ResourceHarvestedEvent,
        StorageChangedEvent,
        FireIgnitedEvent,
        RegionChangedEvent,
        TownHallCreatedEvent,
        DronePlacedEvent,
        TurnEndedEvent {
}
