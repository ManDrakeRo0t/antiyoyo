package ru.bogatov.antiyoyo.game.engine.util;

import lombok.experimental.UtilityClass;

import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Hex;
import ru.bogatov.antiyoyo.game.model.HexColor;
import ru.bogatov.antiyoyo.game.model.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.*;
import java.util.stream.Collectors;

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

    public static void showDefenceForColor(Map<Vector3, Hex> map, HexColor selfColor) {
        map.values().forEach(hex -> {
            if (selfColor == hex.getColor() && hex.getEntity() instanceof Interactable interactable) {
                if (interactable.getClass() == Tower.class || interactable.getClass() == BigTower.class) {
                    HexCalculator.getNeighborsInRadius(map, 1, hex, false)
                            .stream()
                            .filter(neighbor -> neighbor.getColor() == selfColor)
                            .forEach(neighbor -> neighbor.setDisplayDefence(true));
                }
            }
        });
    }

    public static void updateRegionAfterMove(Map<Vector3, Hex> map, Pair<TownHall, Set<Hex>> region) {
        Random random = new Random();

        Set<Class<? extends Entity>> unitsCanDie = Set.of(
                UnitStageTwo.class,
                UnitStageOne.class,
                UnitStageThree.class,
                Tank.class);

        TownHall townHall = region.getFirst();
        Set<Hex> territory = region.getSecond();

        townHall.setBalance(townHall.getBalance() + townHall.getBalanceChanges());

        territory.forEach(hex -> {
            if (hex.getEntity() instanceof Grave) {
                hex.setEntity(new Tree());
            }
            if (hex.getEntity() instanceof Field) {
                if (random.nextInt(10) <= 1) {
                    hex.setEntity(new Tree());
                }
            }
            if (hex.getEntity() instanceof Tree) {
                getNearestNeighborsWithSameColor(map, hex.getColor(), hex)
                        .stream()
                        .filter(n -> n.getEntity() instanceof Field)
                        .forEach(n -> {
                            if (random.nextInt(10) <= 3) {
                                n.setEntity(new Tree());
                            }
                        });
            }
        });

        if (townHall.getBalance() < 0) {
            territory.forEach(hex -> {
                if (unitsCanDie.contains(hex.getEntity().getClass())) {
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
        return HexCalculator.getNeighborsInRadius(map, 1, root, false)
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
        session.getMap().values().forEach(hex -> {
            hex.setIsAvailable(true);
            hex.setDisplayDefence(false);
        });
    }

    public static Set<Pair<TownHall, Set<Hex>>> getAllRegionsByColor(Map<Vector3, Hex> map, HexColor color) {
        return map.values().stream()
                .filter(hex -> hex.getEntity() instanceof TownHall && hex.getColor() == color)
                .map(townHall -> findTownHallWithRegion(map, color, townHall))
                .collect(Collectors.toSet());
    }
}
