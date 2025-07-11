package ru.bogatov.antiyoyo.server.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class RatingConfig {

    public static final Integer GLOBAL_DELTA = 50;
    public static final Float FAVORITE_DELTA = 0.75F;
    public static final Float MIDDLE_DELTA = 0.5F;

    public static final Map<Integer, PlayerClass> players8 = Map.of(
            1, PlayerClass.of(35, 60, 80),
            2, PlayerClass.of(30, 50, 70),
            3, PlayerClass.of(20, 30, 40),
            4, PlayerClass.of(5, 10, 15),
            5, PlayerClass.of(-15, -10, -5),
            6, PlayerClass.of(-35, -30, -25),
            7, PlayerClass.of(-55, -50, -45),
            8, PlayerClass.of(-80, -70, -60)
    );

    public static final Map<Integer, PlayerClass> players7 = Map.of(
            1, PlayerClass.of(40, 65, 85),
            2, PlayerClass.of(30, 50, 70),
            3, PlayerClass.of(15, 25, 35),
            4, PlayerClass.of(0, 5, 10),
            5, PlayerClass.of(-20, -15, -10),
            6, PlayerClass.of(-45, -35, -25),
            7, PlayerClass.of(-70, -60, -50)
    );

    public static final Map<Integer, PlayerClass> players6 = Map.of(
            1, PlayerClass.of(45	,70,90),
            2, PlayerClass.of(25,45,65),
            3, PlayerClass.of(10,20,30),
            4, PlayerClass.of(-5,0,5),
            5, PlayerClass.of(-30,	-25,	-20),
            6, PlayerClass.of(-60,	-50,	-40)
    );

    public static final Map<Integer, PlayerClass> players5 = Map.of(
            1, PlayerClass.of(50	,80,	100),
            2, PlayerClass.of(20,	40,	60),
            3, PlayerClass.of(5,	10,	20),
            4, PlayerClass.of(-10,	-5,	0),
            5, PlayerClass.of(-50,	-40,	-30)
    );

    public static final Map<Integer, PlayerClass> players4 = Map.of(
            1, PlayerClass.of(55,	80,	115),
            2, PlayerClass.of(15,	35,	55),
            3, PlayerClass.of(-15,	-10,	0),
            4, PlayerClass.of(-55,	-45,	-35)
    );

    public static final Map<Integer, PlayerClass> players3 = Map.of(
            1, PlayerClass.of(60,	90,	125),
            2, PlayerClass.of(0,	20,	40),
            3, PlayerClass.of(-125,	-100,	-60)
    );

    public static final Map<Integer, PlayerClass> players2 = Map.of(
            1, PlayerClass.of(75,	100,	150),
            2, PlayerClass.of(-150,	-100,	-75)
    );

    public static final Map<Integer, Map<Integer, PlayerClass>> baseDeltaRating = Map.of(
            8, players8,
            7, players7,
            6, players6,
            5, players5,
            4, players4,
            3, players3,
            2, players2
    );




    @Data
    @AllArgsConstructor
    public static class PlayerClass {

        private Integer favorite;
        private Integer middle;
        private Integer low;

        static PlayerClass of(Integer favorite, Integer middle, Integer low) {
            return new PlayerClass(favorite, middle, low);
        }

    }

}
