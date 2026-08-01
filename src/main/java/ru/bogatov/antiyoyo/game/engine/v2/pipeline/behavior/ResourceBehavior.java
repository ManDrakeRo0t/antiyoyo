package ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior;

import ru.bogatov.antiyoyo.game.model.common.Currency;

public class ResourceBehavior implements Harvestable {

    private final Currency reward;

    public ResourceBehavior(Currency reward) {
        this.reward = reward;
    }

    @Override
    public Currency reward() {
        return reward;
    }
}
