package ru.bogatov.antiyoyo.game.engine.util;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.*;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.bogatov.antiyoyo.game.engine.util.HexCalculator.getNeighborsInRadius;
import static ru.bogatov.antiyoyo.game.engine.util.PowerCalculator.calculateTotalPower;

@UtilityClass
public class MapUtils {

    private static final Map<EntityType, Sellable> sellableEntities = Map.of(
            EntityType.TANK, new Tank(),
            EntityType.UNIT_1, new UnitStageOne(),
            EntityType.UNIT_2, new UnitStageTwo(),
            EntityType.UNIT_3, new UnitStageThree(),
            EntityType.TOWER, new Tower(),
            EntityType.BIG_TOWER, new BigTower(),
            EntityType.FACTORY, new Factory(),
            EntityType.FOREST_FARM, new ForestFarm(),
            EntityType.MINE_FARM, new MineFarm(),
            EntityType.DRONE, new Drone(HexColor.EMPTY)
    );

    // aka upgradeable dieable
    public static final Set<Class<? extends Entity>> moveableUnits = Set.of(
            UnitStageTwo.class,
            UnitStageOne.class,
            UnitStageThree.class,
            Tank.class,
            Drone.class
    );

    public static final Set<Class<? extends Entity>> dieableUnits = Set.of(
            UnitStageTwo.class,
            UnitStageOne.class,
            UnitStageThree.class,
            Tank.class
    );

    public static void showDefenceForColor(Map<Vector3, Hex> map, HexColor selfColor) {
        map.values().forEach(hex -> {
            if (selfColor == hex.getColor() && hex.getEntity() instanceof Interactable interactable) {
                if (interactable.getClass() == Tower.class || interactable.getClass() == BigTower.class || interactable.getClass() == TownHall.class) {
                    getNeighborsInRadius(map, 1, hex, false)
                            .stream()
                            .filter(neighbor -> neighbor.getColor() == selfColor)
                            .forEach(neighbor -> neighbor.setDisplayDefence(true));
                }
            }
        });
    }

    public static void updateRegionAfterMove(GameSession session, Pair<TownHall, Set<Hex>> region) {

        TownHall townHall = region.getFirst();
        Set<Hex> territory = region.getSecond();

        townHall.getStorage().add(townHall.getStorageUpdate());
        territory.forEach(hex -> {
            if (hex.getEntity() instanceof Grave) {
                hex.setEntity(new Tree());
                updateDefenseLevel(session.getMap(), hex, 0, hex.getColor());
            }
//            if (hex.getEntity() instanceof Field) { Слишком много деревьев
//                if (random.nextInt(100) <= 5) {
//                    hex.setEntity(new Tree());
//                    updateDefenseLevel(map, hex, 0, hex.getColor());
//                }
//            }
//            if (hex.getEntity() instanceof Tree) {
//                getNearestNeighborsWithSameColor(map, hex.getColor(), hex)
//                        .stream()
//                        .filter(n -> n.getEntity() instanceof Field)
//                        .forEach(n -> {
//                            if (random.nextInt(100) <= 15) {
//                                n.setEntity(new Tree());
//                            }
//                        });
//            }
            if (hex.getEntity() instanceof Interactable && moveableUnits.contains(hex.getEntity().getClass())) {
                hex.getEntity().setMovedOnThisTurn(false);
            }
        });

        if (townHall.getStorage().getGold() < 0) {
            territory.forEach(hex -> {
                if (dieableUnits.contains(hex.getEntity().getClass())) {
                    if (session.getSetting().getGrave()) {
                        hex.setEntity(new Grave());
                    } else {
                        hex.setEntity(new Field());
                    }
                    updateDefenseLevel(session.getMap(), hex, 0, hex.getColor());
                }
            });
            townHall.getStorage().setGold(0);
        }

        updateTownHallEconomy(region);
    }

    public static Set<Pair<TownHall, Set<Hex>>> findRegions(Map<Vector3, Hex> map, HexColor selfColor) {

        Set<Hex> visited = new HashSet<>();
        Set<Pair<TownHall, Set<Hex>>> regions = new HashSet<>();
        map.values().stream()
                .filter(hex -> hex.getColor() == selfColor)
                .forEach(hex -> {
                    if (!visited.contains(hex)) {
                        Pair<TownHall, Set<Hex>> region = findTownHallWithRegion(map, selfColor, hex);
                        visited.addAll(region.getSecond());
                        regions.add(region);
                    }
                });

        return regions;
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

        return Pair.of(townHall, visited.stream().filter(hex -> hex.getEntity() != null).collect(Collectors.toSet()));
    }

    private static Set<Hex> getNearestNeighborsWithSameColor(Map<Vector3, Hex> map, HexColor selfColor, Hex root) {
        return getNeighborsInRadius(map, 1, root, false)
                .stream()
                .filter(hex -> hex.getColor() == selfColor)
                .collect(Collectors.toSet());
    }

    private static Set<Entity> getNearestNeighborsWithSameColor(Set<Hex> region, HexColor selfColor, Hex root) {
        return getNeighborsInRadius(region.stream().collect(Collectors.toMap(Hex::getVector, Function.identity())), 1, root, false)
                .stream()
                .filter(hex -> hex.getColor() == selfColor)
                .map(Hex::getEntity)
                .collect(Collectors.toSet());
    }

    private static Set<Hex> getNearestNeighborsWithSameColorWithCenter(Map<Vector3, Hex> map, HexColor selfColor, Hex root) {
        return getNeighborsInRadius(map, 1, root, true)
                .stream()
                .filter(hex -> hex.getColor() == selfColor)
                .collect(Collectors.toSet());
    }

    public static Integer calculateDefenseLevel(Map<Vector3, Hex> map, Hex root) {
        return getNearestNeighborsWithSameColorWithCenter(map, root.getColor(), root)
                .stream()
                .filter(hex -> hex.getEntity() instanceof Interactable)
                .map(hex -> ((Interactable) hex.getEntity()).getLevel())
                .max(Integer::compare).orElse(0);
    }

    public static boolean hasInNeighbors(Map<Vector3, Hex> map, Hex hex, Class<? extends Entity> clazz) {
        return getNearestNeighborsWithSameColor(map, hex.getColor(), hex)
                .stream()
                .anyMatch(toCheck -> clazz.isAssignableFrom(toCheck.getEntity().getClass()));
    }

    public static boolean hasInNeighbors(Map<Vector3, Hex> map, Hex hex, HexColor color, Set<Class<? extends Entity>> classSet) {
        if (classSet.size() == 0) {
            return !getNearestNeighborsWithSameColor(map, color, hex).isEmpty();
        }
        return getNearestNeighborsWithSameColor(map, color, hex)
                .stream()
                .anyMatch(toCheck -> classSet.contains(toCheck.getEntity().getClass()));
    }

    public static boolean hasInNeighbors(Map<Vector3, Hex> map, Hex hex, Set<Class<? extends Entity>> classSet) {
        return getNearestNeighborsWithSameColor(map, hex.getColor(), hex)
                .stream()
                .anyMatch(toCheck -> classSet.contains(toCheck.getEntity().getClass()));
    }

    public static boolean hasEntityType(Set<Hex> region, Class<? extends Entity> clazz) {
        return region.stream().anyMatch(hex -> clazz.isAssignableFrom(hex.getEntity().getClass()));
    }

    public static Hex findFirstWithType(Set<Hex> region, Class<? extends Entity> clazz) {
        return region.stream().filter(hex -> clazz.isAssignableFrom(hex.getEntity().getClass())).findFirst().orElse(null);
    }

    public static void updateTownHallEconomy(Map<Vector3, Hex> map, Hex townHall) {
        Pair<TownHall, Set<Hex>> region = findTownHallWithRegion(map, townHall.getColor(), townHall);
        updateTownHallEconomy(region);

    }

    public static void updateTownHallEconomy(Pair<TownHall, Set<Hex>> region) {
        Currency depositChanges = Currency.EMPTY.clone();
        for (Hex hex : region.getSecond()) {
            if (!(hex.getEntity() instanceof Tree)) {
                depositChanges.add(Currency.of(1,0,0));
            }
            if (hex.getEntity() instanceof Farmable farmable) {
                depositChanges.add(farmable.getFarm(getNearestNeighborsWithSameColor(region.getSecond(), hex.getColor(), hex)));
            }
            depositChanges.add(hex.getEntity().getStorageChanges());
        }
        if (region.getFirst() != null) {
            region.getFirst().setStorageUpdate(depositChanges);
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
            return findFirstWithType(region, Field.class);
        }
    }

    public void updatePricesForTownHall(Pair<TownHall, Set<Hex>> region) {
        if (region.getFirst() != null) {
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
    }

    public void updateDronesFlag(GameSession session, HexColor color) {
        var r = findRegions(session.getMap(), color).stream().findFirst().orElse(null);
        if (r.getFirst() == null) {
            return;
        }
        if (r.getFirst().getDronesLimit() == null || getDronesCount(session.getMap(), color) >= r.getFirst().getDronesLimit()){
            getAllRegionsByColor(session.getMap(), color).forEach(region -> {
                region.getFirst().setDronesAvailable(false);
            });
        }
    }

    public static void updateDefenseLevel(Map<Vector3, Hex> map, Hex hex, Integer defenceLevel, HexColor selfColor) {
        Integer calculated = MapUtils.calculateDefenseLevel(map, hex);
        hex.setDefenseLevel(defenceLevel > calculated ? defenceLevel : calculated);
        Set<Hex> toUpdate = HexCalculator.getNeighborsInRadius(map, 1, hex, false);
        toUpdate.forEach(hexToUpdate -> {
                    if (hexToUpdate.getColor() == selfColor) {
                        hexToUpdate.setDefenseLevel(
                                MapUtils.calculateDefenseLevel(map, hexToUpdate));
                    }
                }
        );
    }



    public static void updateDefenseLevelForColor(Map<Vector3, Hex> map, Hex hex, HexColor color) {
        Set<Hex> toUpdate = HexCalculator.getNeighborsInRadius(map, 1, hex, false);
        toUpdate.forEach(hexToUpdate -> {
                    if (hexToUpdate.getColor() == color) {
                        if (hexToUpdate.getEntity() instanceof Interactable interactable) {
                            hexToUpdate.setDefenseLevel(interactable.getLevel());
                        } else {
                            hexToUpdate.setDefenseLevel(0);
                        }
                    }
                }
        );
        toUpdate.forEach(hexToUpdate -> {
                    if (hexToUpdate.getColor() == color) {
                        hexToUpdate.setDefenseLevel(MapUtils.calculateDefenseLevel(map, hexToUpdate));
                    }
                }
        );
    }

    public static void restoreMap(GameSession session) {
        session.getPlayers().values().forEach(player -> {
            player.setSelectedTownHall(null);
        });
        restoreAvailability(session);
    }

    public static void updatePowerAndDronesAvailability(GameSession session) {
        session.getMap().values().forEach(hex -> {
            if (hex.getEntity() instanceof TownHall) {
                ((TownHall) hex.getEntity()).setDronesAvailable(false);
            }
        });
        Map<HexColor, Integer> dronesLimitPerColor = new HashMap<>();
        Map<HexColor, Set<Pair<TownHall, Integer>>> power = calculateTotalPower(session);
        Map<HexColor, Integer> totalPowerPerColor = new HashMap<>();
        power.forEach((key, value) -> totalPowerPerColor.put(key, value.stream().mapToInt(Pair::getSecond).sum()));
        session.setPowerByColor(totalPowerPerColor);
        int maxPower = totalPowerPerColor.values().stream().max(Integer::compare).get();
        Set<HexColor> weakColors = new HashSet<>();
        totalPowerPerColor.forEach((key, value) -> {
            float diff = PowerCalculator.getPersent(maxPower, value);
            int limit = getDronesLimitFromDiff(diff);
            if (limit >= 1) {
                weakColors.add(key);
                dronesLimitPerColor.put(key, limit);
            }
        });
        for (HexColor weakColor : weakColors) {
            Set<Pair<TownHall, Integer>> regions = power.get(weakColor);
            regions.forEach(region -> {
                region.getFirst().setDronesAvailable(true);
                region.getFirst().setDronesLimit(dronesLimitPerColor.get(weakColor));
            });

        }
    }

    private Integer getDronesLimitFromDiff(float diff) {
        if (diff > 81) {
            return 0;
        }
        if (diff > 64) {
            return 1;
        }
        if (diff > 55) {
            return 2;
        }
        if (diff > 46) {
            return 3;
        }
        if (diff > 31) {
            return 4;
        }
        return 5;
     }

    public Integer getDronesCount(Map<Vector3, Hex> map, HexColor color) {
        return Math.toIntExact(map.values()
                .stream()
                .filter(hex -> hex.getEntity() instanceof Drone drone && drone.getOwnerColor() == color)
                .count());
    }

    public static void restoreDefence(GameSession session) {
        session.getMap().values().forEach(hex -> {
            if (hex.getEntity() instanceof Interactable interactable) {
                MapUtils.updateDefenseLevel(session.getMap(), hex, interactable.getLevel(), hex.getColor());
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

    public static void checkPlayersCount(GameSession session) {
        Pair<Integer, Set<HexColor>> playerCount = getPlayersCount(session.getMap());
        int activePlayers = Math.toIntExact(session.getPlayers().values()
                .stream().filter(player -> !player.isIlluminated()).count());
        if (playerCount.getFirst() == 1) {
            Set<HexColor> leftColors = playerCount.getSecond();
            if (leftColors.size() == 1) {
                Player winner = session.getPlayers().values().stream()
                        .filter(player -> !player.isIlluminated() && leftColors.contains(player.getColor()))
                        .findFirst().orElse(null);
                winner.setPlace(1);
                session.setWinnerId(winner == null ? null : winner.getUserId());
                session.getAliveUsersId().remove(winner.getUserId().toString());
                session.setEndTime(OffsetDateTime.now());
            }
            return;
        }
        if (!Objects.equals(playerCount.getFirst(), activePlayers)) {
            Set<HexColor> leftColors = playerCount.getSecond();
            if (leftColors.size() == 1) {
                Player winner = session.getPlayers().values().stream()
                        .filter(player -> !player.isIlluminated() && leftColors.contains(player.getColor()))
                        .findFirst().orElse(null);
                winner.setPlace(1);
                session.setWinnerId(winner == null ? null : winner.getUserId());
                session.getAliveUsersId().remove(winner.getUserId().toString());
                session.setEndTime(OffsetDateTime.now());
                Player other =  session.getPlayers().values().stream()
                        .filter(player -> player.getPlace() < 0)
                        .findFirst().orElse(null);
                if (other != null) {
                    other.setPlace(2);
                    other.setIlluminated(true);
                    session.getAliveUsersId().remove(other.getUserId().toString());
                }
            } else {
                session.getPlayers().values().forEach(player -> {
                    if (!leftColors.contains(player.getColor())) {
                        player.setIlluminated(true);
                        player.setPlace(playerCount.getFirst() + 1);
                        session.getAliveUsersId().remove(player.getUserId().toString());
                    }
                });
            }
        }
    }

    public static Pair<Integer, Set<HexColor>> getPlayersCount(Map<Vector3, Hex> map) {
        Set<HexColor> colors = new HashSet<>();
        map.values().forEach(hex -> colors.add(hex.getColor()));
        colors.remove(HexColor.EMPTY);
        Set<HexColor> playersColors = colors.stream()
                .filter(color -> {
                    var regions = getAllRegionsByColor(map, color);
                    return !regions.isEmpty() && regions.stream().anyMatch(r -> r.getFirst() != null);
                })
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

    public static void killInRegion(GameSession session, Set<Hex> region) {
        region.stream()
                .filter(hex -> dieableUnits.contains(hex.getEntity().getClass()))
                .forEach(hex -> {
                    if (session.getSetting().getGrave()) {
                        hex.setEntity(new Grave());
                    } else {
                        hex.setEntity(new Field());
                    }
                    updateDefenseLevel(session.getMap(), hex, 0, hex.getColor());
                });
    }

    public static void killInRegion(GameSession session, TownHall townHall) {
        Hex townHallHex = HexCalculator.foundTownHallById(session.getMap(), townHall.getUuid());
        killInRegion(session, findTownHallWithRegion(session.getMap(), townHallHex.getColor(), townHallHex).getSecond());
    }

    public static void processFarms(GameSession session) {

        Integer density = Optional.ofNullable(session.getSetting()).map(GameSetting::getFarmsDensity).orElse(20);

        Random random = new Random();

        session.getMap().values().stream()
                .filter(hex -> hex.getEntity() instanceof Farmable)
                .forEach(farm -> {
                    if (random.nextInt(100) <= density) {
                        int count = random.nextInt(2);
                        Set<Hex> n = getNeighborsInRadius(session.getMap(), 1, farm, false)
                                .stream().filter(hex -> hex.getEntity() instanceof Field).collect(Collectors.toSet());
                        int canCreate = Math.min(n.size(), count);
                        while (canCreate > 0) {
                            Hex r = n.stream().findAny().get();
                            r.setEntity(EntityUtils.fromType(((Farmable) farm.getEntity()).farmableType()));
                            n.remove(r);
                            canCreate--;
                        }
                    }
                });
    }

    public static void restoreDrones(GameSession session) {

        Map<HexColor, Boolean> isDronesAvailable = new HashMap<>();

        session.getMap().values()
                .stream()
                .filter(hex -> hex.getEntity() instanceof TownHall)
                .forEach(townHall -> isDronesAvailable.put(townHall.getColor() ,((TownHall) townHall.getEntity()).isDronesAvailable()));

        session.getMap().values().stream()
                .filter(hex -> hex.getEntity() instanceof Drone)
                .forEach(drore -> {
                    drore.getEntity().setMovedOnThisTurn(false);
                });


    }

    public static void processFire(GameSession session) {
        session.getMap().values().stream()
                .filter(hex -> hex.getEntity() instanceof Fire)
                .forEach(fireHex -> {
                    Fire fire = (Fire) fireHex.getEntity();
                    fire.setStage(fire.getStage() - 1);
                    if (fire.getStage() <= 0) {
                        fireHex.setEntity(new Field());
                    }
                });
    }
}
