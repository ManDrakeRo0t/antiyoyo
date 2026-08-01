package ru.bogatov.antiyoyo.game.engine.v2.service;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.FeatureFlags;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.util.EntityClassifier;
import ru.bogatov.antiyoyo.game.engine.v2.util.HexGeometry;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class EconomyService {

    public static void updateTownHallEconomy(Map<Vector3, Hex> map, Hex townHallHex) {
        Pair<TownHall, Set<Hex>> region = RegionService.findTownHallWithRegion(map, townHallHex.getColor(), townHallHex);
        updateTownHallEconomy(region);
    }

    public static void updateTownHallEconomy(Pair<TownHall, Set<Hex>> region) {
        if (region.getFirst() == null) {
            return;
        }
        Currency depositChanges = Currency.EMPTY.clone();
        Map<Vector3, Hex> regionMap = region.getSecond().stream().collect(Collectors.toMap(Hex::getVector, h -> h));
        for (Hex hex : region.getSecond()) {
            if (!(hex.getEntity() instanceof Tree)) {
                depositChanges.add(Currency.of(1, 0, 0));
            }
            if (hex.getEntity() instanceof Farmable farmable) {
                Set<Entity> neighbors = HexGeometry.neighborsInRadius(regionMap, 1, hex, false).stream()
                        .map(Hex::getEntity)
                        .collect(Collectors.toSet());
                depositChanges.add(farmable.getFarm(neighbors));
            }
            depositChanges.add(hex.getEntity().getStorageChanges());
        }
        region.getFirst().setStorageUpdate(depositChanges);
    }

    public static void updatePricesForTownHall(Pair<TownHall, Set<Hex>> region) {
        if (region.getFirst() == null) {
            return;
        }
        Map<EntityType, Currency> prices = new java.util.HashMap<>();
        int factoryCount = (int) region.getSecond().stream().filter(hex -> hex.getEntity() instanceof Factory).count();

        prices.put(EntityType.UNIT_1, new UnitStageOne().getPrice(0));
        prices.put(EntityType.UNIT_2, new UnitStageTwo().getPrice(0));
        prices.put(EntityType.UNIT_3, new UnitStageThree().getPrice(0));
        prices.put(EntityType.TANK, new Tank().getPrice(0));
        prices.put(EntityType.TOWER, new Tower().getPrice(0));
        prices.put(EntityType.BIG_TOWER, new BigTower().getPrice(0));
        prices.put(EntityType.FACTORY, new Factory().getPrice(factoryCount));
        prices.put(EntityType.FOREST_FARM, new ForestFarm().getPrice(0));
        prices.put(EntityType.MINE_FARM, new MineFarm().getPrice(0));
        prices.put(EntityType.DRONE, new Drone(HexColor.EMPTY).getPrice(0));

        region.getFirst().setPrices(prices);
    }

    public static void updatePricesForAll(Map<Vector3, Hex> map) {
        map.values().stream()
                .filter(hex -> hex.getEntity() instanceof TownHall)
                .forEach(hex -> updatePricesForTownHall(RegionService.findTownHallWithRegion(map, hex.getColor(), hex)));
    }

    public static void applyEndOfTurnEconomy(GameSession session, HexColor color) {
        Set<Pair<TownHall, Set<Hex>>> regions = RegionService.getAllRegionsByColor(session.getMap(), color);
        for (Pair<TownHall, Set<Hex>> region : regions) {
            applyRegionEconomy(session, region);
        }
    }

    public static void applyAllEndOfTurnEconomy(GameSession session) {
        for (HexColor color : HexColor.values()) {
            if (color == HexColor.EMPTY) continue;
            applyEndOfTurnEconomy(session, color);
        }
    }

    public static void applyRegionEconomy(GameSession session, Pair<TownHall, Set<Hex>> region) {
        TownHall townHall = region.getFirst();
        if (townHall == null) {
            return;
        }
        townHall.getStorage().add(townHall.getStorageUpdate());
        region.getSecond().forEach(hex -> {
            if (hex.getEntity() instanceof Grave) {
                hex.setEntity(new Tree());
                DefenseService.updateDefenseLevel(session.getMap(), hex, 0, hex.getColor());
            }
            if (hex.getEntity() instanceof Interactable && EntityClassifier.isMoveableUnit(hex.getEntity())) {
                hex.getEntity().setMovedOnThisTurn(false);
            }
        });

        if (townHall.getStorage().getGold() < 0) {
            region.getSecond().forEach(hex -> {
                if (EntityClassifier.isDieableUnit(hex.getEntity())) {
                    if (session.getSetting() != null && Boolean.TRUE.equals(session.getSetting().getGrave())) {
                        hex.setEntity(new Grave());
                    } else {
                        hex.setEntity(new Field());
                    }
                    DefenseService.updateDefenseLevel(session.getMap(), hex, 0, hex.getColor());
                }
            });
            townHall.getStorage().setGold(0);
        }
        updateTownHallEconomy(region);
    }

    public static void applyStorageUpdate(GameSession session, TownHall townHall) {
        Hex hex = RegionService.findTownHallById(session.getMap(), townHall.getUuid());
        if (hex == null) {
            return;
        }
        applyRegionEconomy(session, RegionService.findTownHallWithRegion(session.getMap(), hex.getColor(), hex));
    }

    public static void updateAllEconomies(Map<Vector3, Hex> map) {
        map.values().stream()
                .filter(hex -> hex.getEntity() instanceof TownHall)
                .forEach(hex -> updateTownHallEconomy(map, hex));
    }

    public static Currency getPrice(EntityType type, int factoryCount) {
        return switch (type) {
            case UNIT_1 -> new UnitStageOne().getPrice(0);
            case UNIT_2 -> new UnitStageTwo().getPrice(0);
            case UNIT_3 -> new UnitStageThree().getPrice(0);
            case TANK -> new Tank().getPrice(0);
            case TOWER -> new Tower().getPrice(0);
            case BIG_TOWER -> new BigTower().getPrice(0);
            case FACTORY -> new Factory().getPrice(factoryCount);
            case FOREST_FARM -> new ForestFarm().getPrice(0);
            case MINE_FARM -> new MineFarm().getPrice(0);
            case DRONE -> new Drone(HexColor.EMPTY).getPrice(0);
            default -> Currency.EMPTY.clone();
        };
    }
}
