package ru.bogatov.antiyoyo.game.engine.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.GameSetting;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ru.bogatov.antiyoyo.game.engine.util.MapUtils.buildings;
import static ru.bogatov.antiyoyo.game.engine.util.MapUtils.dieableUnits;

@Slf4j
@UtilityClass
public class HexCalculator {

    public static Integer distance(Hex from, Hex to) {
        return (Math.abs(from.getVector().getX() - to.getVector().getX()) +
                Math.abs(from.getVector().getY() - to.getVector().getY()) +
                Math.abs(from.getVector().getZ() - to.getVector().getZ())) / 2;
    }

    public Hex foundTownHallById(Map<Vector3, Hex> map, UUID id) {
        return map.values().stream()
                .filter(hex -> hex.getEntity() instanceof TownHall townHall && townHall.getUuid().equals(id))
                .findFirst()
                .orElse(null);
    }

    public static Set<Hex> getAvailableHexesForField(UUID townHallId, GameSession gameSession, HexColor selfColor) {
        Hex townHall = foundTownHallById(gameSession.getMap(), townHallId);
        if (townHall == null) {
            throw new IllegalArgumentException("No townHall");
        }
        Pair<TownHall, Set<Hex>> region = MapUtils.findTownHallWithRegion(gameSession.getMap(), selfColor, townHall);

        return region.getSecond().stream().filter(hex -> buildings.contains(hex.getEntity().getClass())).collect(Collectors.toSet());
    }

    public static Set<Hex> getAvailableHexesForNewEntity(UUID townHallId,
                                                             GameSession gameSession,
                                                             HexColor selfColor,
                                                             Interactable entity) {
        Hex townHall = foundTownHallById(gameSession.getMap(), townHallId);
        if (townHall == null) {
            throw new IllegalArgumentException("No townHall");
        }

        Set<Class<? extends Entity>> shouldHaveInNeighbors;
        if (Optional.ofNullable(gameSession.getSetting()).map(GameSetting::getCut).orElse(Boolean.FALSE)) {
            shouldHaveInNeighbors = getShouldHaveInNeighborsWithCut(entity);
        } else {
            shouldHaveInNeighbors = getShouldHaveInNeighbors(entity);
        }
        Pair<TownHall, Set<Hex>> region = MapUtils.findTownHallWithRegion(gameSession.getMap(), selfColor, townHall);

        Set<Hex> available = region.getSecond().stream()
                .flatMap(hex -> addNeiboursForAttack(hex, gameSession.getMap(), entity.getAttackRadius()))
                .filter(hex -> MapUtils.hasInNeighbors(gameSession.getMap(), hex, selfColor, shouldHaveInNeighbors))
                .filter(hex -> canMoveToEnemyHex(hex, selfColor, entity))
                .filter(hex -> !(hex.getEntity() instanceof Farmable))
                .collect(Collectors.toSet());

        Set<Hex> selfAvailable = region.getSecond().stream()
                .filter(hex -> MapUtils.hasInNeighbors(gameSession.getMap(), hex, selfColor, shouldHaveInNeighbors))
                .filter(hex -> canMoveToSelfHex(hex, selfColor, entity))
                .filter(hex -> !(hex.getEntity() instanceof Farmable))
                .collect(Collectors.toSet());

        available.addAll(selfAvailable);

       return entity.customizeAvailableHexesForNew(gameSession.getMap(), region, selfColor, available);
    }

    private static Set<Class<? extends Entity>> getShouldHaveInNeighbors(Interactable entity) {
        Set<Class<? extends Entity>> shouldHaveInNeighbors = Set.of();
        if (dieableUnits.contains(entity.getClass())) {
            if (entity.getLevel() == 1 || entity.getLevel() == 2) {
                shouldHaveInNeighbors = Set.of(Tower.class, BigTower.class, Factory.class, TownHall.class);
            }
            if (entity.getLevel() == 3 || entity.getLevel() == 4) {
                shouldHaveInNeighbors = Set.of(BigTower.class, Factory.class, TownHall.class);
            }
        }
        return shouldHaveInNeighbors;
    }

    private static Set<Class<? extends Entity>> getShouldHaveInNeighborsWithCut(Interactable entity) {
        Set<Class<? extends Entity>> shouldHaveInNeighbors = Set.of();
        if (dieableUnits.contains(entity.getClass())) {
            if (entity.getLevel() == 1) {
                shouldHaveInNeighbors = Set.of(Tower.class, BigTower.class, Factory.class, TownHall.class, UnitStageTwo.class, UnitStageThree.class, Tank.class);
            }
            if (entity.getLevel() == 2) {
                shouldHaveInNeighbors = Set.of(Tower.class, BigTower.class, Factory.class, TownHall.class, UnitStageThree.class, Tank.class);
            }
            if (entity.getLevel() == 3) {
                shouldHaveInNeighbors = Set.of(BigTower.class, Factory.class, TownHall.class, Tank.class);
            }
            if (entity.getLevel() == 4) {
                shouldHaveInNeighbors = Set.of(BigTower.class, Factory.class, TownHall.class);
            }
        }
        return shouldHaveInNeighbors;
    }


    private static Stream<Hex> addNeiboursForAttack(Hex init, Map<Vector3, Hex> map, Integer radius) {
        return getNeighborsInRadius(map, radius, init, true)
                .stream()
                .filter(hex -> !isSameColor(hex, init.getColor()));

    }

    public static Set<Hex> getAvailableHexesForExistingEntity(Map<Vector3, Hex> map,
                                                                  Hex initialPosition,
                                                                    HexColor playerColor) {
        Interactable entity = (Interactable) initialPosition.getEntity();
        HexColor selfColor = initialPosition.getColor();
        HexColor entityColor = entity instanceof Drone drone ? drone.getOwnerColor() : initialPosition.getColor();

        if (entityColor != playerColor) {
            return Set.of();
        }

        Set<Hex> available = new HashSet<>();
        Set<Hex> visited = new HashSet<>();

        Queue<Pair<Integer, Hex>> toVisit = new ArrayDeque<>();
        toVisit.add(Pair.of(0, initialPosition));

        while (!toVisit.isEmpty()) {

            Pair<Integer, Hex> root = toVisit.poll();

            if (!visited.contains(root.getSecond())) {

                var rootPath = root.getFirst();

                var toCheck = getNeighborsInRadius(map, 1, root.getSecond(), false);

                toCheck.forEach(hex -> {
                    if (root.getSecond().getColor() == hex.getColor() &&
                            hex.getColor() == selfColor &&
                            rootPath + 1 <= entity.getMoveRadius()) {
                            available.add(hex);
                    }

                    if (root.getSecond().getColor() != hex.getColor() && root.getSecond().getColor() == selfColor &&
                            hex.getColor() != selfColor &&
                            rootPath + 1 <= entity.getMoveRadius()) {
                            available.add(hex);
                    }
                });

            }

            if (distance(root.getSecond(), initialPosition) < entity.getMoveRadius() && root.getSecond().getColor() == selfColor) {
                var toCheck = getNeighborsInRadius(map, 1, root.getSecond(), false);
                toCheck.removeAll(visited);
                toCheck.removeAll(toVisit.stream().map(Pair::getSecond).collect(Collectors.toSet()));
                toVisit.addAll(toCheck.stream().map(h -> Pair.of(root.getFirst() + 1, h)).collect(Collectors.toSet()));
            }

            visited.add(root.getSecond());
        }

        visited.remove(initialPosition);

        Set<Hex> result = available.stream()
                .filter(hex -> canMoveToEnemyHex(hex, selfColor, entity) || canMoveToSelfHex(hex, selfColor, entity))
                .collect(Collectors.toSet());
        return entity.customizeAvailableHexesForExisting(map, initialPosition, result);
    }

    public static Set<Hex> getNeighborsInRadius(Map<Vector3, Hex> map, Integer radius, Hex init, boolean addCenter) {
        Set<Hex> result = new HashSet<>();
        Vector3 center = init.getVector();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = Math.max(-radius, -dx - radius); dy <= Math.min(radius, -dx + radius); dy++) {
                int dz = -dx - dy;
                Vector3 neighborCoord = Vector3.from(
                        center.getX() + dx,
                        center.getY() + dy,
                        center.getZ() + dz
                );

                Hex neighbor = map.get(neighborCoord);
                if (neighbor != null && !neighbor.equals(init)) {
                    result.add(neighbor);
                }
            }
        }
        if (addCenter) {
            result.add(init);
        }
        return result;
    }




    private static boolean isSameColor(Hex hex, HexColor color) {
        return hex.getColor() == color;
    }

    private static boolean canUpgradeUnit(Entity old, Interactable toPlace) {
        Set<Class> entitiesToMerge = Set.of(UnitStageOne.class, UnitStageTwo.class, UnitStageThree.class);
        Integer maxLevel = 4;
        if (entitiesToMerge.contains(old.getClass()) && entitiesToMerge.contains(toPlace.getClass())) {
            if (((Interactable) old).getLevel() + toPlace.getLevel() <= maxLevel) {
                return true;
            }
        }
        return false;
    }

    private static boolean canMoveToSelfHex(Hex to, HexColor selfColor, Interactable entity) {
        return isSameColor(to, selfColor) &&
                (canUpgradeUnit(to.getEntity(), entity) || canPlaceEntity(entity, to) || canReplaceEntity(entity, to));
    }

    private static boolean canReplaceEntity(Interactable entity, Hex to) {
        return to.getEntity() instanceof Tower && entity instanceof BigTower;
    }

    public boolean canInteractWithHex(Hex hex, HexColor selfColor) {
        return isSameColor(hex, selfColor) || (hex.getEntity() instanceof Drone drone && drone.getOwnerColor() == selfColor);
    }

    private static boolean canPlaceEntity(Interactable entity, Hex to) {
        if (MapUtils.moveableUnits.contains(entity.getClass())) {
            return to.getEntity() instanceof Mineable ||
                    to.getEntity() instanceof Field ||
                    to.getEntity() instanceof Grave ||
                    to.getEntity() instanceof Farmable ||
                    (to.getEntity() instanceof Drone drone && drone.getOwnerColor() != to.getColor());
        }
        return to.getEntity() instanceof Field;
    }

    private static boolean canMoveToEnemyHex(Hex to, HexColor selfColor, Interactable entity) {
        return !isSameColor(to, selfColor) && (to.getDefenseLevel() < entity.getLevel() || entity.getLevel() == 4);
    }

}
