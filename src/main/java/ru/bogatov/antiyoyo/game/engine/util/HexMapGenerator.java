package ru.bogatov.antiyoyo.game.engine.util;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.Hex;
import ru.bogatov.antiyoyo.game.model.HexColor;
import ru.bogatov.antiyoyo.game.model.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.Field;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class HexMapGenerator {

    public static Map<Vector3, Hex> generateEmptyMap(Integer size) {

        Map<Vector3, Hex> map = new HashMap<>();

        for (int x = -size; x <= size ; x++) {
            for (int y = -size; y <= size ; y++) {
                for (int z = -size; z <= size ; z++) {
                    if (z + x + y != 0) {
                        continue;
                    }
                    Vector3 vector = Vector3.from(x, y, z);
                    map.put(vector, createEmptyHex(vector));
                }
            }
        }

        return map;
    }


    private static Hex createEmptyHex(Vector3 vector) {
        return Hex.builder()
                .color(HexColor.EMPTY)
                .vector(vector)
                .isAvailable(false)
                .entity(new Field())
                .defenseLevel(0)
                .build();
    }

}
