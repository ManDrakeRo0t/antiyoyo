package ru.bogatov.antiyoyo.game.engine.utils;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.Hex;
import ru.bogatov.antiyoyo.game.model.HexColor;
import ru.bogatov.antiyoyo.game.model.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.Field;
import ru.bogatov.antiyoyo.game.model.entity.Interactable;
import ru.bogatov.antiyoyo.game.model.entity.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class HexCalculator {

    public static Integer distance(Hex from, Hex to) {
        return (Math.abs(from.getVector().getX() - to.getVector().getX()) +
                Math.abs(from.getVector().getY() - to.getVector().getY()) +
                Math.abs(from.getVector().getZ() - to.getVector().getZ())) / 2;
    }

    public static Set<Vector3> getAvailableHexesForNewEntity(Map<Vector3, Hex> map,
                                                             HexColor selfColor,
                                                             Interactable entity) {
        return map.values().stream()
                .filter(hex -> isSameColor(hex, selfColor))
                .flatMap(hex -> addNeiboursForAttack(hex, map, entity.getAttackRadius()))
                .filter(hex -> canMoveToSelfHex(hex, selfColor) || canMoveToEnemyHex(hex, selfColor, entity))
                .map(Hex::getVector)
                .collect(Collectors.toSet());
    }

    private static Stream<Hex> addNeiboursForAttack(Hex init, Map<Vector3, Hex> map, Integer radius) {

        return getNeighborsInRadius(map, radius, init)
                .stream()
                .filter(hex -> !isSameColor(hex, init.getColor()));

    }

    public static Set<Vector3> getAvailableHexesForExistingEntity(Map<Vector3, Hex> map,
                                                                  Hex initialPosition) {
        Interactable entity = (Interactable) initialPosition.getEntity();
        HexColor selfColor = initialPosition.getColor();
        return map.values().stream()
                .filter(hex -> inMoveRadius(hex, initialPosition, entity) || inAttackRadius(hex, initialPosition, entity))
                .filter(hex -> canMoveToEnemyHex(hex, selfColor, entity) || canMoveToSelfHex(hex, selfColor))
                .map(Hex::getVector)
                .collect(Collectors.toSet());
    }

    public static List<Hex> getNeighborsInRadius(Map<Vector3, Hex> map, Integer radius, Hex init) {
        List<Hex> result = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = Math.max(-radius, -x - radius); y <= Math.min(radius, -x + radius); y++) {
                int z = -x - y;
                result.add(map.get(addVector(init.getVector(), Vector3.from(x, y, z))));
            }
        }
        return result;
    }

    private static Vector3 addVector(Vector3 a, Vector3 b) {
        return Vector3.from(a.getX() + b.getX(), a.getY() + b.getY(), a.getZ() + b.getY());
    }

    private static boolean inMoveRadius(Hex to, Hex from, Interactable entity) {
        return isSameColor(to, from.getColor()) && distance(from, to) <= entity.getMoveRadius();
    }

    private static boolean isSameColor(Hex hex, HexColor color) {
        return hex.getColor() == color;
    }

    private static boolean inAttackRadius(Hex to, Hex from, Interactable entity) {
        return !isSameColor(to, from.getColor()) && distance(from, to) <= entity.getAttackRadius();
    }

    private static boolean canMoveToSelfHex(Hex to, HexColor selfColor) {
        return isSameColor(to, selfColor) && to.getEntity() instanceof Tree || to.getEntity() instanceof Field;
    }

    private static boolean canMoveToEnemyHex(Hex to, HexColor selfColor, Interactable entity) {
        return !isSameColor(to, selfColor) && to.getDefenseLevel() < entity.getLevel();
    }

}
