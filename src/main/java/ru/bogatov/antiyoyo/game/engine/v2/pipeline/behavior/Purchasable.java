package ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior;

import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.MoveEvent;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

import java.util.List;
import java.util.Set;

public interface Purchasable extends EntityBehavior {

    Set<Hex> availablePlacement(MoveContext context, TownHall townHall);

    Currency price(MoveContext context, Hex target);

    List<MoveEvent> onPurchase(MoveContext context, Hex target, TownHall townHall);
}
