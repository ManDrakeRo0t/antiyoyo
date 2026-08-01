package ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior;

import ru.bogatov.antiyoyo.game.model.entity.Entity;

public interface Upgradable extends EntityBehavior {

    boolean canUpgrade(Entity existing, Entity placed);

    Entity merge(Entity existing, Entity placed);
}
