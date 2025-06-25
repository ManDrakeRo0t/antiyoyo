package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.engine.util.MapUtils;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class Factory extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.FACTORY;
    }

    @Override
    public Currency getStorageChanges() {
        return Currency.of(4,0,0);
    }

    @Override
    public Currency getPrice(Integer unitsCount) {
        return Currency.of(0,4 + unitsCount  ,2 + unitsCount);
    }

    @Override
    public Integer getLevel() {
        return 0;
    }

    @Override
    public Integer getAttackRadius() {
        return -1;
    }

    @Override
    public Integer getMoveRadius() {
        return -1;
    }

    @Override
    public Set<Hex> customizeAvailableHexesForNew(Map<Vector3, Hex> map, Pair<TownHall, Set<Hex>> region, HexColor selfColor, Set<Hex> calculated) {
        return calculated.stream()
                .filter(hex -> MapUtils.hasInNeighbors(map, map.get(hex.getVector()), Set.of(Factory.class, TownHall.class)))
                    .collect(Collectors.toSet());

    }
}
