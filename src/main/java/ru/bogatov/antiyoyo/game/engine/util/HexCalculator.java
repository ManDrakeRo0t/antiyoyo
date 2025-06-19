package ru.bogatov.antiyoyo.game.engine.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.bogatov.antiyoyo.game.model.Hex;
import ru.bogatov.antiyoyo.game.model.HexColor;
import ru.bogatov.antiyoyo.game.model.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static Set<Vector3> getAvailableHexesForNewEntity(UUID townHallId,
                                                             Map<Vector3, Hex> map,
                                                             HexColor selfColor,
                                                             Interactable entity) {
        Hex townHall = foundTownHallById(map, townHallId);
        if (townHall == null) {
            throw new IllegalArgumentException("No townHall");
        }
        Pair<TownHall, Set<Hex>> region = MapUtils.findTownHallWithRegion(map, selfColor, townHall);

        Set<Vector3> available = region.getSecond().stream()
                .flatMap(hex -> addNeiboursForAttack(hex, map, entity.getAttackRadius()))
                .filter(hex -> canMoveToEnemyHex(hex, selfColor, entity)).map(Hex::getVector)
                .collect(Collectors.toSet());

        Set<Vector3> selfAvailable = region.getSecond().stream()
                .filter(hex -> canMoveToSelfHex(hex, selfColor, entity))
                .map(Hex::getVector)
                .collect(Collectors.toSet());

        if (entity instanceof Factory) {
            selfAvailable = selfAvailable.stream()
                    .filter(hex -> MapUtils.hasInNeighbors(map, map.get(hex), Set.of(Factory.class, TownHall.class)))
                    .collect(Collectors.toSet());
        }

       available.addAll(selfAvailable);

       return available;
    }


    private static Stream<Hex> addNeiboursForAttack(Hex init, Map<Vector3, Hex> map, Integer radius) {
        return getNeighborsInRadius(map, radius, init, true)
                .stream()
                .filter(hex -> !isSameColor(hex, init.getColor()));

    }

    public static Set<Vector3> getAvailableHexesForExistingEntity(Map<Vector3, Hex> map,
                                                                  Hex initialPosition) {
        Interactable entity = (Interactable) initialPosition.getEntity();
        HexColor selfColor = initialPosition.getColor();

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

        return available.stream()
                .filter(hex -> canMoveToEnemyHex(hex, selfColor, entity) || canMoveToSelfHex(hex, selfColor, entity))
                .map(Hex::getVector).collect(Collectors.toSet());

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

    private static boolean canPlaceEntity(Interactable entity, Hex to) {
        if (MapUtils.moveableUnits.contains(entity.getClass())) {
            return to.getEntity() instanceof Tree || to.getEntity() instanceof Field || to.getEntity() instanceof Grave;
        }
        return to.getEntity() instanceof Field;
    }

    private static boolean canMoveToEnemyHex(Hex to, HexColor selfColor, Interactable entity) {
        return !isSameColor(to, selfColor) && (to.getDefenseLevel() < entity.getLevel() || entity.getLevel() == 4);
    }

}
