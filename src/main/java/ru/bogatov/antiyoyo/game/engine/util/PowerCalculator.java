package ru.bogatov.antiyoyo.game.engine.util;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.Color;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class PowerCalculator {

    public static Integer calculatePowerForRegion(Pair<TownHall, Set<Hex>> region) {
        AtomicInteger result = new AtomicInteger(region.getFirst().getStorage().getGold());
        result.addAndGet(region.getFirst().getStorage().getTree() * 2);
        result.addAndGet(region.getFirst().getStorage().getStone() * 3);
        final int[] count = {1};
        region.getSecond().forEach(hex -> {
            result.addAndGet(count[0] + hexToPower(hex));
            count[0]++;
        });
        return result.get();
    }

    public static Map<Color, Set<Pair<TownHall, Integer>>> calculateTotalPower(GameSession gameSession) {
            Set<Color> colors = new HashSet<>();
            gameSession.getMap().values().forEach(hex -> colors.add(hex.getColor()));
            Map<Color, Set<Pair<TownHall, Integer>>> powerMap = new HashMap<>();
            colors.forEach(color -> {
                Set<Pair<TownHall, Set<Hex>>> regions = MapUtils.getAllRegionsByColor(gameSession.getMap(), color);
                Set<Pair<TownHall, Integer>> powerForRegions = new HashSet<>();
                regions.forEach(region -> {
                    powerForRegions.add(Pair.of(region.getFirst(), calculatePowerForRegion(region)));
                });
                powerMap.put(color, powerForRegions);
            });
            return powerMap;
    }

    public float getPersent(int max, int target) {
        return (float) target / max * 100;
    }

    private static int hexToPower(Hex hex) {
        Entity entity = hex.getEntity();
        if (entity instanceof Tree) {
            return 6;
        }
        if (entity instanceof Stone) {
            return 9;
        }
        if (entity instanceof Forest) {
            return 6;
        }
        if (entity instanceof ForestFarm) {
            return 24;
        }
        if (entity instanceof Mine) {
            return 9;
        }
        if (entity instanceof MineFarm) {
            return 36;
        }
        if (entity instanceof UnitStageOne) {
            return 10;
        }
        if (entity instanceof UnitStageTwo) {
            return 20;
        }
        if (entity instanceof UnitStageThree) {
            return 30;
        }
        if (entity instanceof Tank) {
            return 40;
        }
        if (entity instanceof Tower) {
            return 15;
        }
        if (entity instanceof BigTower) {
            return 35;
        }
        if (entity instanceof Factory) {
            return 15;
        }
        return 1;
    }

}
