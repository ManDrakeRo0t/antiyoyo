package ru.bogatov.antiyoyo.game.engine.v2.service;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.engine.util.EntityUtils;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.FeatureFlags;
import ru.bogatov.antiyoyo.game.engine.v2.util.HexGeometry;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.Entity;
import ru.bogatov.antiyoyo.game.model.entity.Farmable;
import ru.bogatov.antiyoyo.game.model.entity.Field;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class FarmService {

    public static void processFarms(GameSession session) {
        FeatureFlags flags = FeatureFlags.from(session.getSetting());
        Map<Vector3, Hex> map = session.getMap();
        Random random = new Random();

        map.values().stream()
                .filter(hex -> hex.getEntity() instanceof Farmable)
                .forEach(farm -> {
                    if (random.nextInt(100) <= flags.farmsDensity()) {
                        int count = random.nextInt(2);
                        Set<Hex> neighbors = HexGeometry.neighborsInRadius(map, 1, farm, false).stream()
                                .filter(hex -> hex.getEntity() instanceof Field)
                                .collect(Collectors.toSet());
                        int canCreate = Math.min(neighbors.size(), count);
                        while (canCreate > 0) {
                            Hex target = neighbors.iterator().next();
                            target.setEntity(EntityUtils.fromType(((Farmable) farm.getEntity()).farmableType()));
                            neighbors.remove(target);
                            canCreate--;
                        }
                    }
                });
    }
}
