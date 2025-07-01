package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.engine.util.HexCalculator;
import ru.bogatov.antiyoyo.game.engine.util.MapUtils;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Vector3;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
public class Drone extends Entity implements Sellable, Interactable {

    private HexColor ownerColor;

    public Drone(HexColor ownerColor) {
        this.ownerColor = ownerColor;
    }

    @Override
    public EntityType getType() {
        return EntityType.DRONE;
    }

    @Override
    public Integer getLevel() {
        return 0;
    }

    @Override
    public Integer getAttackRadius() {
        return 0;
    }

    @Override
    public Integer getMoveRadius() {
        return 4;
    }

    @Override
    public Currency getPrice(Integer unitsCount) {
        return Currency.of(5,0,0);
    }

    @Override
    public Set<Hex> customizeAvailableHexesForExisting(Map<Vector3, Hex> map, Hex initialPosition, Set<Hex> calculated) {
        Set<Class<? extends Entity>> canMoveOnEnemyTerritory = Set.of(
                        Field.class,
                        Factory.class,
                        MineFarm.class,
                        ForestFarm.class,
                        UnitStageOne.class,
                        UnitStageTwo.class
                );
        return HexCalculator.getNeighborsInRadius(map, this.getMoveRadius(), initialPosition, false)
                .stream()
                .filter(hex -> {
                    if (hex.getColor() == this.getOwnerColor() && hex.getEntity() instanceof Field) {
                        return true;
                    }
                    if (hex.getColor() != this.getOwnerColor() && canMoveOnEnemyTerritory.contains(hex.getEntity().getClass())) {
                        return true;
                    }
                    return false;
                }).collect(Collectors.toSet());
    }

    @Override
    public Set<Hex> customizeAvailableHexesForNew(Map<Vector3, Hex> map, Pair<TownHall, Set<Hex>> region, HexColor selfColor, Set<Hex> calculated) {
        return region.getSecond().stream().filter(hex -> hex.getEntity() instanceof Field).collect(Collectors.toSet());
    }
}
