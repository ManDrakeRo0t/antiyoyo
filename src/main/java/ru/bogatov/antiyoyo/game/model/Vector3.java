package ru.bogatov.antiyoyo.game.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Builder
public class Vector3 {

    private Integer x;
    private Integer y;
    private Integer z;

    public static Vector3 from(int x, int y, int z) {
        return Vector3.builder().x(x).y(y).z(z).build();
    }
}
