package ru.bogatov.antiyoyo.game.engine.v2.util;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.Vector3;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@UtilityClass
public class HexGeometry {

    public static int distance(Hex from, Hex to) {
        return (Math.abs(from.getVector().getX() - to.getVector().getX())
                + Math.abs(from.getVector().getY() - to.getVector().getY())
                + Math.abs(from.getVector().getZ() - to.getVector().getZ())) / 2;
    }

    public static int distance(Vector3 from, Vector3 to) {
        return (Math.abs(from.getX() - to.getX())
                + Math.abs(from.getY() - to.getY())
                + Math.abs(from.getZ() - to.getZ())) / 2;
    }

    public static Set<Hex> neighborsInRadius(Map<Vector3, Hex> map, int radius, Hex center, boolean includeCenter) {
        Set<Hex> result = new HashSet<>();
        Vector3 c = center.getVector();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = Math.max(-radius, -dx - radius); dy <= Math.min(radius, -dx + radius); dy++) {
                int dz = -dx - dy;
                Vector3 coord = Vector3.from(c.getX() + dx, c.getY() + dy, c.getZ() + dz);
                Hex neighbor = map.get(coord);
                if (neighbor != null && !neighbor.equals(center)) {
                    result.add(neighbor);
                }
            }
        }
        if (includeCenter) {
            result.add(center);
        }
        return result;
    }

    public static Set<Hex> neighborsInRadius(Set<Hex> hexes, int radius, Hex center, boolean includeCenter) {
        Map<Vector3, Hex> map = new java.util.HashMap<>();
        for (Hex hex : hexes) {
            map.put(hex.getVector(), hex);
        }
        return neighborsInRadius(map, radius, center, includeCenter);
    }

    public static Set<Hex> sameColorNeighbors(Map<Vector3, Hex> map, Hex center, boolean includeCenter) {
        Set<Hex> neighbors = neighborsInRadius(map, 1, center, includeCenter);
        neighbors.removeIf(hex -> hex.getColor() != center.getColor());
        return neighbors;
    }
}
