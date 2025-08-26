package ru.bogatov.antiyoyo.game.model.entity;

import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.model.MoveContext;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.Color;
import ru.bogatov.antiyoyo.game.model.common.Vector3;

import java.util.Map;
import java.util.Set;

public interface Interactable {

    default void onClick(Hex hex, MoveContext context) {

    }

    default Set<Hex> customizeAvailableHexesForNew(Map<Vector3, Hex> map,
                                                   Pair<TownHall, Set<Hex>> region,
                                                       Color selfColor,
                                                       Set<Hex> calculated) {
        return calculated;
    }

    default Set<Hex> customizeAvailableHexesForExisting(Map<Vector3, Hex> map,
                                                            Hex initialPosition,
                                                            Set<Hex> calculated) {
        return calculated;
    }
}
