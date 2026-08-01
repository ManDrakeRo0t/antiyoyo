package ru.bogatov.antiyoyo.game.engine.v2.service;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.engine.v2.util.HexGeometry;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.Entity;
import ru.bogatov.antiyoyo.game.model.entity.Interactable;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class DefenseService {

    public static int calculateDefenseLevel(Map<Vector3, Hex> map, Hex root) {
        return HexGeometry.neighborsInRadius(map, 1, root, true).stream()
                .filter(hex -> hex.getColor() == root.getColor())
                .filter(hex -> hex.getEntity() instanceof Interactable)
                .map(hex -> ((Interactable) hex.getEntity()).getLevel())
                .max(Integer::compare).orElse(0);
    }

    public static void updateDefenseLevel(Map<Vector3, Hex> map, Hex hex, int defenseLevel, HexColor selfColor) {
        int calculated = calculateDefenseLevel(map, hex);
        hex.setDefenseLevel(Math.max(defenseLevel, calculated));
        Set<Hex> toUpdate = HexGeometry.neighborsInRadius(map, 1, hex, false);
        toUpdate.stream()
                .filter(h -> h.getColor() == selfColor)
                .forEach(h -> h.setDefenseLevel(calculateDefenseLevel(map, h)));
    }

    public static void updateDefenseLevelForColor(Map<Vector3, Hex> map, Hex hex, HexColor color) {
        Set<Hex> toUpdate = HexGeometry.neighborsInRadius(map, 1, hex, false);
        toUpdate.stream().filter(h -> h.getColor() == color).forEach(h -> {
            if (h.getEntity() instanceof Interactable interactable) {
                h.setDefenseLevel(interactable.getLevel());
            } else {
                h.setDefenseLevel(0);
            }
        });
        toUpdate.stream().filter(h -> h.getColor() == color).forEach(h -> h.setDefenseLevel(calculateDefenseLevel(map, h)));
    }

    public static void recalculateAllDefense(Map<Vector3, Hex> map) {
        for (Hex hex : map.values()) {
            if (hex.getEntity() instanceof Interactable interactable) {
                updateDefenseLevel(map, hex, interactable.getLevel(), hex.getColor());
            } else {
                hex.setDefenseLevel(calculateDefenseLevel(map, hex));
            }
        }
    }

    public static void showDefenceForColor(Map<Vector3, Hex> map, HexColor color) {
        map.values().stream()
                .filter(hex -> hex.getColor() == color)
                .filter(hex -> hex.getEntity() instanceof Interactable)
                .forEach(hex -> {
                    Entity entity = hex.getEntity();
                    if (entity instanceof ru.bogatov.antiyoyo.game.model.entity.Tower
                            || entity instanceof ru.bogatov.antiyoyo.game.model.entity.BigTower
                            || entity instanceof ru.bogatov.antiyoyo.game.model.entity.TownHall) {
                        HexGeometry.neighborsInRadius(map, 1, hex, false).stream()
                                .filter(n -> n.getColor() == color)
                                .forEach(n -> n.setDisplayDefence(true));
                    }
                });
    }
}
