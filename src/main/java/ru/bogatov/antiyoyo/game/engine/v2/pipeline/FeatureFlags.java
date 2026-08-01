package ru.bogatov.antiyoyo.game.engine.v2.pipeline;

import ru.bogatov.antiyoyo.game.model.GameSetting;

public record FeatureFlags(
        boolean undoMove,
        boolean grave,
        boolean demolition,
        boolean cut,
        int farmsDensity,
        int secondsToMove
) {

    public static FeatureFlags from(GameSetting setting) {
        if (setting == null) {
            return defaults();
        }
        return new FeatureFlags(
                Boolean.TRUE.equals(setting.getUndoMove()),
                Boolean.TRUE.equals(setting.getGrave()),
                Boolean.TRUE.equals(setting.getDemolition()),
                Boolean.TRUE.equals(setting.getCut()),
                setting.getFarmsDensity() != null ? setting.getFarmsDensity() : 20,
                setting.getSecondsToMove() != null ? setting.getSecondsToMove() : 60
        );
    }

    public static FeatureFlags defaults() {
        return new FeatureFlags(false, false, false, false, 20, 60);
    }
}
