package ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior;

import ru.bogatov.antiyoyo.game.model.common.Currency;

public interface Harvestable extends EntityBehavior {

    Currency reward();
}
