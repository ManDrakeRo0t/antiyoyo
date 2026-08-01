package ru.bogatov.antiyoyo.game.engine.v2.service;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class PowerService {

    public static int calculatePowerForRegion(Pair<TownHall, Set<Hex>> region) {
        TownHall townHall = region.getFirst();
        if (townHall == null) {
            return 0;
        }
        AtomicInteger result = new AtomicInteger(townHall.getStorage().getGold());
        result.addAndGet(townHall.getStorage().getTree() * 2);
        result.addAndGet(townHall.getStorage().getStone() * 3);
        int[] counter = {1};
        region.getSecond().forEach(hex -> {
            result.addAndGet(counter[0] + hexToPower(hex));
            counter[0]++;
        });
        return result.get();
    }

    public static Map<HexColor, Set<Pair<TownHall, Integer>>> calculateTotalPower(GameSession session) {
        Map<Vector3, Hex> map = session.getMap();
        Set<HexColor> colors = new HashSet<>();
        map.values().forEach(hex -> colors.add(hex.getColor()));
        colors.remove(HexColor.EMPTY);

        Map<HexColor, Set<Pair<TownHall, Integer>>> powerMap = new HashMap<>();
        colors.forEach(color -> {
            Set<Pair<TownHall, Set<Hex>>> regions = RegionService.getAllRegionsByColor(map, color);
            Set<Pair<TownHall, Integer>> regionPowers = new HashSet<>();
            regions.forEach(region -> regionPowers.add(Pair.of(region.getFirst(), calculatePowerForRegion(region))));
            powerMap.put(color, regionPowers);
        });
        return powerMap;
    }

    public static void updatePowerAndDronesAvailability(GameSession session) {
        Map<Vector3, Hex> map = session.getMap();
        map.values().forEach(hex -> {
            if (hex.getEntity() instanceof TownHall townHall) {
                townHall.setDronesAvailable(false);
            }
        });

        Map<HexColor, Set<Pair<TownHall, Integer>>> power = calculateTotalPower(session);
        Map<HexColor, Integer> totalPerColor = new HashMap<>();
        power.forEach((color, regions) -> totalPerColor.put(color, regions.stream().mapToInt(Pair::getSecond).sum()));
        session.setPowerByColor(totalPerColor);

        if (totalPerColor.isEmpty()) {
            return;
        }
        int maxPower = totalPerColor.values().stream().max(Integer::compare).orElse(0);
        Map<HexColor, Integer> limits = new HashMap<>();
        Set<HexColor> weakColors = new HashSet<>();
        totalPerColor.forEach((color, value) -> {
            float diff = getPercent(maxPower, value);
            int limit = getDronesLimitFromDiff(diff);
            if (limit >= 1) {
                weakColors.add(color);
                limits.put(color, limit);
            }
        });

        for (HexColor weakColor : weakColors) {
            Set<Pair<TownHall, Integer>> regions = power.get(weakColor);
            if (regions == null) continue;
            regions.forEach(region -> {
                region.getFirst().setDronesAvailable(true);
                region.getFirst().setDronesLimit(limits.get(weakColor));
            });
        }
    }

    public static int getDronesCount(Map<Vector3, Hex> map, HexColor color) {
        return (int) map.values().stream()
                .filter(hex -> hex.getEntity() instanceof Drone drone && drone.getOwnerColor() == color)
                .count();
    }

    public static void updateDronesFlag(GameSession session, HexColor color) {
        Set<Pair<TownHall, Set<Hex>>> regions = RegionService.getAllRegionsByColor(session.getMap(), color);
        if (regions.isEmpty()) {
            return;
        }
        TownHall first = regions.iterator().next().getFirst();
        if (first == null) {
            return;
        }
        int count = getDronesCount(session.getMap(), color);
        if (first.getDronesLimit() == null || count >= first.getDronesLimit()) {
            regions.forEach(region -> region.getFirst().setDronesAvailable(false));
        }
    }

    public static int getDronesLimitFromDiff(float diff) {
        if (diff > 81) return 0;
        if (diff > 64) return 1;
        if (diff > 55) return 2;
        if (diff > 46) return 3;
        if (diff > 31) return 4;
        return 5;
    }

    public static float getPercent(int max, int target) {
        return (float) target / max * 100;
    }

    public static int hexToPower(Hex hex) {
        Entity entity = hex.getEntity();
        if (entity instanceof Tree) return 6;
        if (entity instanceof Stone) return 9;
        if (entity instanceof Forest) return 6;
        if (entity instanceof ForestFarm) return 24;
        if (entity instanceof Mine) return 9;
        if (entity instanceof MineFarm) return 36;
        if (entity instanceof UnitStageOne) return 10;
        if (entity instanceof UnitStageTwo) return 20;
        if (entity instanceof UnitStageThree) return 30;
        if (entity instanceof Tank) return 40;
        if (entity instanceof Tower) return 15;
        if (entity instanceof BigTower) return 35;
        if (entity instanceof Factory) return 15;
        return 1;
    }
}
