package ru.bogatov.antiyoyo.game.engine.v2.service;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.FeatureFlags;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.util.HexGeometry;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;
import ru.bogatov.antiyoyo.game.engine.v2.util.EntityClassifier;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class RegionService {

    public static Pair<TownHall, Set<Hex>> findTownHallWithRegion(Map<Vector3, Hex> map, HexColor color, Hex start) {
        Set<Hex> visited = new HashSet<>();
        Queue<Hex> queue = new ArrayDeque<>();
        queue.add(start);
        Map<Hex, TownHall> townHalls = new HashMap<>();

        while (!queue.isEmpty()) {
            Hex root = queue.poll();
            if (visited.contains(root)) {
                continue;
            }
            visited.add(root);
            if (root.getEntity() instanceof TownHall townhall) {
                townHalls.put(root, townhall);
            }
            Set<Hex> toCheck = HexGeometry.sameColorNeighbors(map, root, false);
            toCheck.removeAll(visited);
            toCheck.removeAll(queue);
            queue.addAll(toCheck);
        }

        TownHall mainTownHall = townHalls.values().stream().findFirst().orElse(null);
        if (townHalls.size() > 1 && mainTownHall != null) {
            for (Map.Entry<Hex, TownHall> entry : townHalls.entrySet()) {
                if (mainTownHall.getStorage().compareTo(entry.getValue().getStorage()) < 0) {
                    mainTownHall = entry.getValue();
                }
            }
            TownHall finalMain = mainTownHall;
            townHalls.forEach((hex, th) -> {
                if (!finalMain.getUuid().equals(th.getUuid())) {
                    finalMain.getStorage().add(th.getStorage());
                    hex.setEntity(new Field());
                    DefenseService.updateDefenseLevel(map, hex, 0, color);
                }
            });
        }

        return Pair.of(mainTownHall, visited.stream().filter(hex -> hex.getEntity() != null).collect(Collectors.toSet()));
    }

    public static Set<Pair<TownHall, Set<Hex>>> findRegions(Map<Vector3, Hex> map, HexColor color) {
        Set<Hex> visited = new HashSet<>();
        Set<Pair<TownHall, Set<Hex>>> regions = new HashSet<>();
        for (Hex hex : map.values()) {
            if (hex.getColor() != color || visited.contains(hex)) {
                continue;
            }
            Pair<TownHall, Set<Hex>> region = findTownHallWithRegion(map, color, hex);
            visited.addAll(region.getSecond());
            regions.add(region);
        }
        return regions;
    }

    public static Set<Pair<TownHall, Set<Hex>>> getAllRegionsByColor(Map<Vector3, Hex> map, HexColor color) {
        return map.values().stream()
                .filter(hex -> hex.getEntity() instanceof TownHall && hex.getColor() == color)
                .map(townHall -> findTownHallWithRegion(map, color, townHall))
                .collect(Collectors.toSet());
    }

    public static Pair<Integer, Set<HexColor>> getPlayersCount(Map<Vector3, Hex> map) {
        Set<HexColor> colors = map.values().stream().map(Hex::getColor).collect(Collectors.toSet());
        colors.remove(HexColor.EMPTY);
        Set<HexColor> active = colors.stream()
                .filter(color -> {
                    Set<Pair<TownHall, Set<Hex>>> regions = getAllRegionsByColor(map, color);
                    return !regions.isEmpty() && regions.stream().anyMatch(r -> r.getFirst() != null);
                })
                .collect(Collectors.toSet());
        return Pair.of(active.size(), active);
    }

    public static void checkPlayersCount(GameSession session) {
        Pair<Integer, Set<HexColor>> active = getPlayersCount(session.getMap());
        Set<HexColor> leftColors = active.getSecond();
        session.getPlayers().values().forEach(player -> {
            if (!leftColors.contains(player.getColor()) && !player.isIlluminated()) {
                player.illuminate();
                session.getAliveUsersId().remove(player.getUserId().toString());
            }
        });
        long alive = session.getPlayers().values().stream().filter(p -> !p.isIlluminated()).count();
        if (active.getFirst() == 1 || alive == 1) {
            Player winner = session.getPlayers().values().stream()
                    .filter(p -> !p.isIlluminated() && leftColors.contains(p.getColor()))
                    .findFirst().orElse(null);
            if (winner != null) {
                session.getAliveUsersId().remove(winner.getUserId().toString());
                session.setWinnerId(winner.getUserId());
                session.setEndTime(OffsetDateTime.now());
            }
        }
    }

    public static void validateAllHexAreAvailable(Map<Vector3, Hex> map) {
        if (map.isEmpty()) {
            return;
        }
        Vector3 start = map.keySet().iterator().next();
        Set<Vector3> visited = new HashSet<>();
        Queue<Vector3> toVisit = new ArrayDeque<>();
        toVisit.add(start);

        while (!toVisit.isEmpty()) {
            Vector3 root = toVisit.poll();
            if (visited.contains(root)) {
                continue;
            }
            Set<Vector3> toCheck = HexGeometry.neighborsInRadius(map, 1, Hex.builder().vector(root).build(), false)
                    .stream().map(Hex::getVector).collect(Collectors.toSet());
            toCheck.removeAll(visited);
            toCheck.removeAll(toVisit);
            toVisit.addAll(toCheck);
            visited.add(root);
        }

        Set<Vector3> all = map.values().stream().map(Hex::getVector).collect(Collectors.toSet());
        if (!visited.containsAll(all)) {
            throw new IllegalArgumentException("Не все клетки можно посетить");
        }
    }

    public static Hex findPlaceForTownHall(Map<Vector3, Hex> map, Set<Hex> region) {
        boolean hasFactory = region.stream().anyMatch(hex -> hex.getEntity() instanceof Factory);
        if (hasFactory) {
            Hex nearFactory = region.stream()
                    .filter(hex -> hex.getEntity() instanceof Field)
                    .filter(hex -> hasNeighborType(map, hex, Factory.class))
                    .findFirst().orElse(null);
            if (nearFactory != null) {
                return nearFactory;
            }
            return region.stream().filter(hex -> hex.getEntity() instanceof Factory).findFirst().orElse(null);
        }
        return region.stream().filter(hex -> hex.getEntity() instanceof Field).findFirst().orElse(null);
    }

    public static boolean hasNeighborType(Map<Vector3, Hex> map, Hex hex, Class<? extends Entity> type) {
        return HexGeometry.sameColorNeighbors(map, hex, false).stream()
                .anyMatch(n -> type.isAssignableFrom(n.getEntity().getClass()));
    }

    public static boolean hasNeighborType(Set<Hex> region, Hex hex, Class<? extends Entity> type) {
        Map<Vector3, Hex> map = region.stream().collect(Collectors.toMap(Hex::getVector, Function.identity()));
        return hasNeighborType(map, hex, type);
    }

    public static void killInRegion(GameSession session, Set<Hex> region) {
        region.stream()
                .filter(hex -> EntityClassifier.isDieableUnit(hex.getEntity()))
                .forEach(hex -> {
                    if (session.getSetting() != null && Boolean.TRUE.equals(session.getSetting().getGrave())) {
                        hex.setEntity(new Grave());
                    } else {
                        hex.setEntity(new Field());
                    }
                    DefenseService.updateDefenseLevel(session.getMap(), hex, 0, hex.getColor());
                });
    }

    public static void killInRegion(GameSession session, TownHall townHall) {
        Hex townHallHex = findTownHallById(session.getMap(), townHall.getUuid());
        if (townHallHex != null) {
            killInRegion(session, findTownHallWithRegion(session.getMap(), townHallHex.getColor(), townHallHex).getSecond());
        }
    }

    public static Hex findTownHallById(Map<Vector3, Hex> map, UUID id) {
        return map.values().stream()
                .filter(hex -> hex.getEntity() instanceof TownHall th && th.getUuid().equals(id))
                .findFirst().orElse(null);
    }

    public static void validateRegionsAfterCapture(GameSession session, Entity oldEntity, HexColor oldColor) {
        Currency oldBalance = Currency.EMPTY.clone();
        Set<TownHall> created = new HashSet<>();
        if (oldEntity instanceof TownHall townHall) {
            oldBalance = townHall.getStorage();
        }

        Set<Hex> validated = new HashSet<>();
        for (Hex hex : session.getMap().values()) {
            if (hex.getColor() == HexColor.EMPTY || validated.contains(hex)) {
                continue;
            }
            Pair<TownHall, Set<Hex>> region = findTownHallWithRegion(session.getMap(), hex.getColor(), hex);
            if (region.getFirst() != null) {
                validated.addAll(region.getSecond());
            } else {
                Hex place = findPlaceForTownHall(session.getMap(), region.getSecond());
                if (place != null) {
                    TownHall townHall = new TownHall(Currency.EMPTY.clone(), Currency.EMPTY.clone());
                    if (place.getColor() == oldColor) {
                        created.add(townHall);
                    }
                    place.setEntity(townHall);
                    place.getEntity().setMovedOnThisTurn(null);
                    DefenseService.updateDefenseLevel(session.getMap(), place, 0, place.getColor());
                    validated.addAll(findTownHallWithRegion(session.getMap(), place.getColor(), place).getSecond());
                } else {
                    killInRegion(session, region.getSecond());
                    validated.addAll(region.getSecond());
                }
            }
        }

        if (!created.isEmpty()) {
            Currency perTownHall = oldBalance.split(created.size());
            for (TownHall townHall : created) {
                townHall.setStorage(perTownHall.clone());
                EconomyService.updateTownHallEconomy(session.getMap(), findTownHallById(session.getMap(), townHall.getUuid()));
                EconomyService.applyStorageUpdate(session, townHall);
                if (townHall.getStorage().getGold() + townHall.getStorageUpdate().getGold() < 0) {
                    killInRegion(session, townHall);
                }
            }
        }
    }
}
