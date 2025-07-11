package ru.bogatov.antiyoyo.game.engine.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.aggregation.DateOperators;

import java.time.OffsetDateTime;

class HexMapGeneratorTest {


    @Test
    public void generatorTest() {
        OffsetDateTime now = OffsetDateTime.now();
        int t = 4;
        OffsetDateTime old = now.minusMinutes(3);

        System.out.println(now.minusMinutes(t).isBefore(old));
    }


}