package ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.service.RegionService;
import ru.bogatov.antiyoyo.game.engine.v2.util.HexGeometry;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.Map;
import java.util.Set;

@UtilityClass
public class BehaviorHelper {

    public static Pair<TownHall, Set<Hex>> getTownHallRegion(MoveContext context, TownHall townHall) {
        Hex hex = RegionService.findTownHallById(context.getSession().getMap(), townHall.getUuid());
        if (hex == null) {
            return Pair.of(null, Set.of());
        }
        return RegionService.findTownHallWithRegion(context.getSession().getMap(), hex.getColor(), hex);
    }

    public static Pair<TownHall, Set<Hex>> getRegionOf(MoveContext context, Hex hex) {
        return RegionService.findTownHallWithRegion(context.getSession().getMap(), hex.getColor(), hex);
    }

    public static Set<Hex> neighbors(MoveContext context, Hex center, int radius, boolean includeCenter) {
        return HexGeometry.neighborsInRadius(context.getSession().getMap(), radius, center, includeCenter);
    }

    public static boolean isSameColor(Hex hex, HexColor color) {
        return hex.getColor() == color;
    }

    public static boolean isEnemy(Hex hex, HexColor selfColor) {
        return hex.getColor() != HexColor.EMPTY && hex.getColor() != selfColor;
    }

    public static boolean canCapture(Hex target, HexColor selfColor, int attackerLevel) {
        if (isSameColor(target, selfColor) || target.getColor() == HexColor.EMPTY) {
            return true;
        }
        return attackerLevel == 4 || target.getDefenseLevel() < attackerLevel;
    }

    public static boolean isFreeForUnit(Hex hex, HexColor selfColor) {
        Entity entity = hex.getEntity();
        return entity instanceof Field
                || entity instanceof Grave
                || entity instanceof Mineable
                || (entity instanceof ru.bogatov.antiyoyo.game.model.entity.Drone drone && drone.getOwnerColor() != selfColor);
    }

    public static boolean isFreeForBuilding(Hex hex) {
        return hex.getEntity() instanceof Field;
    }

    public static boolean isDraggableTarget(Hex hex, HexColor ownerColor) {
        Entity entity = hex.getEntity();
        if (hex.getColor() == ownerColor && entity instanceof Field) {
            return true;
        }
        return entity instanceof Field
                || entity instanceof Factory
                || entity instanceof ForestFarm
                || entity instanceof MineFarm
                || entity instanceof UnitStageOne
                || entity instanceof UnitStageTwo;
    }

    public static boolean hasSpawnerNeighbor(MoveContext context, Hex hex, Set<Class<? extends Entity>> spawners) {
        HexColor selfColor = context.getSelfColor();
        return neighbors(context, hex, 1, false).stream()
                .filter(n -> n.getColor() == selfColor)
                .map(n -> n.getEntity().getClass())
                .anyMatch(spawners::contains);
    }
}
