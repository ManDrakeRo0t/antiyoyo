package ru.bogatov.antiyoyo.game.engine.util;

import lombok.experimental.UtilityClass;

import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Hex;
import ru.bogatov.antiyoyo.game.model.HexColor;
import ru.bogatov.antiyoyo.game.model.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.*;
import java.util.stream.Collectors;

import static ru.bogatov.antiyoyo.game.engine.util.HexCalculator.getNeighborsInRadius;

@UtilityClass
public class MapUtils {

    private static final Map<EntityType, Sellable> sellableEntities = Map.of(
            EntityType.TANK, new Tank(),
            EntityType.UNIT_1, new UnitStageOne(),
            EntityType.UNIT_2, new UnitStageTwo(),
            EntityType.UNIT_3, new UnitStageThree(),
            EntityType.TOWER, new Tower(),
            EntityType.BIG_TOWER, new BigTower(),
            EntityType.FACTORY, new Factory()
    );

    public static final Set<Class<? extends Entity>> moveableUnits = Set.of(
            UnitStageTwo.class,
            UnitStageOne.class,
            UnitStageThree.class,
            Tank.class
    );

    public static void showDefenceForColor(Map<Vector3, Hex> map, HexColor selfColor) {
        map.values().forEach(hex -> {
            if (selfColor == hex.getColor() && hex.getEntity() instanceof Interactable interactable) {
                if (interactable.getClass() == Tower.class || interactable.getClass() == BigTower.class) {
                    getNeighborsInRadius(map, 1, hex, false)
                            .stream()
                            .filter(neighbor -> neighbor.getColor() == selfColor)
                            .forEach(neighbor -> neighbor.setDisplayDefence(true));
                }
            }
        });
    }

    public static void updateRegionAfterMove(Map<Vector3, Hex> map, Pair<TownHall, Set<Hex>> region) {
        Random random = new Random();

        TownHall townHall = region.getFirst();
        Set<Hex> territory = region.getSecond();

        townHall.setBalance(townHall.getBalance() + townHall.getBalanceChanges());

        territory.forEach(hex -> {
            if (hex.getEntity() instanceof Grave) {
                hex.setEntity(new Tree());
            }
            if (hex.getEntity() instanceof Field) {
                if (random.nextInt(100) <= 5) {
                    hex.setEntity(new Tree());
                }
            }
            if (hex.getEntity() instanceof Tree) {
                getNearestNeighborsWithSameColor(map, hex.getColor(), hex)
                        .stream()
                        .filter(n -> n.getEntity() instanceof Field)
                        .forEach(n -> {
                            if (random.nextInt(100) <= 15) {
                                n.setEntity(new Tree());
                            }
                        });
            }
            if (hex.getEntity() instanceof Interactable && moveableUnits.contains(hex.getEntity().getClass())) {
                hex.getEntity().setMovedOnThisTurn(false);
            }
        });

        if (townHall.getBalance() < 0) {
            territory.forEach(hex -> {
                if (moveableUnits.contains(hex.getEntity().getClass())) {
                    hex.setEntity(new Grave());
                }
            });
        }

        updateTownHallEconomy(region);
    }

    public static Pair<TownHall, Set<Hex>> findTownHallWithRegion(Map<Vector3, Hex> map, HexColor selfColor, Hex start) {
        Set<Hex> visited = new HashSet<>();
        Queue<Hex> queue = new ArrayDeque<>();
        queue.add(start);
        TownHall townHall = null;

        while (!queue.isEmpty()) {

            Hex root = queue.poll();
            visited.add(root);
            if (root.getEntity() instanceof TownHall casted) {
                if (townHall != null) {
                    throw new IllegalArgumentException("Two town hall is one region");
                }
                townHall = casted;
            }
            Set<Hex> toCheck = getNearestNeighborsWithSameColor(map, selfColor, root);
            toCheck.removeAll(visited);
            toCheck.removeAll(queue);

            queue.addAll(toCheck);
        }

        return Pair.of(townHall, visited);
    }

    private static Set<Hex> getNearestNeighborsWithSameColor(Map<Vector3, Hex> map, HexColor selfColor, Hex root) {
        return getNeighborsInRadius(map, 1, root, false)
                .stream()
                .filter(hex -> hex.getColor() == selfColor)
                .collect(Collectors.toSet());
    }

    public static Integer calculateDefenseLevel(Map<Vector3, Hex> map, Hex root) {
        return getNearestNeighborsWithSameColor(map, root.getColor(), root)
                .stream()
                .map(Hex::getDefenseLevel)
                .max(Integer::compare).orElse(0);
    }

    public static boolean hasInNeighbors(Map<Vector3, Hex> map, Hex hex, Class<? extends Entity> clazz) {
        return getNearestNeighborsWithSameColor(map, hex.getColor(), hex)
                .stream()
                .anyMatch(toCheck -> clazz.isAssignableFrom(toCheck.getEntity().getClass()));
    }

    public static boolean hasInNeighbors(Map<Vector3, Hex> map, Hex hex, Set<Class<? extends Entity>> classSet) {
        return getNearestNeighborsWithSameColor(map, hex.getColor(), hex)
                .stream()
                .anyMatch(toCheck -> classSet.contains(toCheck.getEntity().getClass()));
    }

    public static boolean hasEntityType(Set<Hex> region, Class<? extends Entity> clazz) {
        return region.stream().anyMatch(hex -> clazz.isAssignableFrom(hex.getClass()));
    }

    public static Hex findFirstWithType(Set<Hex> region, Class<? extends Entity> clazz) {
        return region.stream().filter(hex -> clazz.isAssignableFrom(hex.getClass())).findFirst().orElse(null);
    }

    public static void updateTownHallEconomy(Map<Vector3, Hex> map, Hex townHall) {
         Pair<TownHall, Set<Hex>> region = findTownHallWithRegion(map, townHall.getColor(), townHall);
        updateTownHallEconomy(region);
    }

    public static void updateTownHallEconomy(Pair<TownHall, Set<Hex>> region) {
        int depositChanges = 0;
        for (Hex hex : region.getSecond()) {
            if (!(hex.getEntity() instanceof Tree)) {
                depositChanges += 1;
            }
            depositChanges += hex.getEntity().getBalanceChange();
        }
        if (region.getFirst() != null) {
            region.getFirst().setBalanceChanges(depositChanges);
        }
    }

    public static Hex findPlaceForTownHall(Map<Vector3, Hex> map, Set<Hex> region) {
        boolean isFactoryExists = hasEntityType(region, Factory.class);
        if (isFactoryExists) {
            Hex placeNearFactory = region.stream().filter(hex -> hex.getEntity() instanceof Field)
                    .filter(hex -> hasInNeighbors(map, hex, Factory.class))
                    .findFirst().orElse(null);
            if (placeNearFactory == null) {
                return findFirstWithType(region, Factory.class);
            }
            return placeNearFactory;
        } else {
            Hex hexToReplace = findFirstWithType(region, Field.class);
            if (hexToReplace == null) {
                hexToReplace = findFirstWithType(region, Grave.class);
            }
            if (hexToReplace == null) {
                hexToReplace = findFirstWithType(region, UnitStageOne.class);
            }
            if (hexToReplace == null) {
                hexToReplace = findFirstWithType(region, UnitStageTwo.class);
            }
            return hexToReplace;
        }
    }

    public void updatePricesForTownHall(Pair<TownHall, Set<Hex>> region) {
        region.getFirst().setPrices(
                sellableEntities.entrySet().stream().map(entry -> {
                    int count = 0;
                    if (EntityType.FACTORY == entry.getKey()) {
                        count = (int) region.getSecond()
                                .stream().filter(hex -> hex.getEntity() instanceof Factory)
                                .count();
                    }
                    return Map.entry(entry.getKey(), entry.getValue().getPrice(count));
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
    }

    public static void restoreMap(GameSession session) {
        session.getPlayers().values().forEach(player -> {
            player.setSelectedTownHall(null);
        });
        restoreAvailability(session);
    }

    public static void restoreDefence(GameSession session) {
        session.getMap().values().forEach(hex -> {
            if (hex.getEntity() instanceof Interactable interactable) {
                Set<Hex> toUpdate = HexCalculator.getNeighborsInRadius(session.getMap(), interactable.getDefenceRadius(), hex, false);
                toUpdate.forEach(hexToUpdate ->
                        hexToUpdate.setDefenseLevel(
                                MapUtils.calculateDefenseLevel(session.getMap(), hexToUpdate))
                );
            }
        });

    }

    public static void restoreAvailability(GameSession session) {
        session.getMap().values().forEach(hex -> {
            if (hex.getEntity().getMovedOnThisTurn() != null && Boolean.TRUE.equals(hex.getEntity().getMovedOnThisTurn())) {
                hex.setIsAvailable(false);
            } else {
                hex.setIsAvailable(true);
            }
            hex.setDisplayDefence(false);
        });
    }

    public static Set<Pair<TownHall, Set<Hex>>> getAllRegionsByColor(Map<Vector3, Hex> map, HexColor color) {
        return map.values().stream()
                .filter(hex -> hex.getEntity() instanceof TownHall && hex.getColor() == color)
                .map(townHall -> findTownHallWithRegion(map, color, townHall))
                .collect(Collectors.toSet());
    }

    public static Pair<Integer, Set<HexColor>>  getPlayersCount(Map<Vector3, Hex> map) {
        Set<HexColor> colors = new HashSet<>();
        map.values().forEach(hex -> colors.add(hex.getColor()));
        colors.remove(HexColor.EMPTY);
        Set<HexColor> playersColors =  colors.stream().filter(color -> !getAllRegionsByColor(map, color).isEmpty())
                        .collect(Collectors.toSet());
        return Pair.of(playersColors.size(), playersColors);
    }

    public static void validateAllHexAreAvailable(Map<Vector3, Hex> map) {
        Vector3 start = map.keySet().stream().toList().getFirst();

        Set<Vector3> visited = new HashSet<>();

        Queue<Vector3> toVisit = new ArrayDeque<>();
        toVisit.add(start);

        while (!toVisit.isEmpty()) {
            Vector3 root = toVisit.poll();
            if (!visited.contains(root)) {
                var toCheck = getNeighborsInRadius(map, 1, Hex.builder().vector(root).build(), false).stream().map(Hex::getVector).collect(Collectors.toSet());
                toCheck.removeAll(visited);
                toCheck.removeAll(toVisit);
                toVisit.addAll(toCheck);
                visited.add(root);
            }
        }

        if (!visited.containsAll(map.values().stream().map(Hex::getVector).collect(Collectors.toSet()))) {
            throw new IllegalArgumentException("Не все клетки можно посетить");
        }

    }
}
