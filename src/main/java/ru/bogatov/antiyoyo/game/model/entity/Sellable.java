package ru.bogatov.antiyoyo.game.model.entity;

import ru.bogatov.antiyoyo.game.model.common.Currency;

public interface Sellable {

    Currency getPrice(Integer unitsCount);

}
