package ru.bogatov.antiyoyo.game.model.entity;

import ru.bogatov.antiyoyo.game.model.common.Currency;

import java.util.Set;

public interface Farmable {

    Currency getFarm(Set<Entity> neighbors);

    EntityType farmableType();

}
