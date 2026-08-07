package ru.bogatov.antiyoyo.game.engine.v2.pipeline.event;

public sealed interface MoveEvent permits DronePlacedEvent, EntityDestroyedEvent, EntityMergedEvent, EntityMovedEvent, EntityPlacedEvent, EntityRemovedEvent, FireIgnitedEvent, RegionChangedEvent, ResourceCaptureEvent, ResourceHarvestedEvent, StorageChangedEvent, TownHallCreatedEvent, TurnEndedEvent {
}
