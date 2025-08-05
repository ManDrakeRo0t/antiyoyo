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

    public static float getDeltaMultiplier(int rank) {
        if (rank < 200) {
            return 0.45F;
        }
        if (rank < 400) {
            return 0.6F;
        }
        if (rank < 600) {
            return 0.75F;
        }
        if (rank < 800) {
            return 0.9F;
        }
        return 1f;
    }

    public static final Map<Integer, PlayerClass> players8 = Map.of(
            1, PlayerClass.of(125, 135, 145),
            2, PlayerClass.of(95, 105, 115),
            3, PlayerClass.of(65, 75, 85),
            4, PlayerClass.of(35, 45, 55),
            5, PlayerClass.of(5, 15, 25),
            6, PlayerClass.of(-25, -15, -5),
            7, PlayerClass.of(-55, -45, -35),
            8, PlayerClass.of(-85, -75, -65)
    );

    public static final Map<Integer, PlayerClass> players7 = Map.of(
            1, PlayerClass.of(105, 110, 120),
            2, PlayerClass.of(75, 80, 90),
            3, PlayerClass.of(45, 50, 60),
            4, PlayerClass.of(15, 20, 30),
            5, PlayerClass.of(-15, -5, 0),
            6, PlayerClass.of(-45, -35, -30),
            7, PlayerClass.of(-75, -70, -60)
    );

    public static final Map<Integer, PlayerClass> players6 = Map.of(
            1, PlayerClass.of(75	,85,95),
            2, PlayerClass.of(45,55,65),
            3, PlayerClass.of(15,25,35),
            4, PlayerClass.of(-15,-5,5),
            5, PlayerClass.of(-45,	-35,	-25),
            6, PlayerClass.of(-75,	-65,	-55)
    );

    public static final Map<Integer, PlayerClass> players5 = Map.of(
            1, PlayerClass.of(55	,65,	75),
            2, PlayerClass.of(25,	35,	45),
            3, PlayerClass.of(-5,	5,	15),
            4, PlayerClass.of(-35,	-25,	-15),
            5, PlayerClass.of(-65,	-55,	-45)
    );

    public static final Map<Integer, PlayerClass> players4 = Map.of(
            1, PlayerClass.of(40,	45,	55),
            2, PlayerClass.of(10,	15,	25),
            3, PlayerClass.of(-20,	-15,	-5),
            4, PlayerClass.of(-50,	-45,	-35)
    );

    public static final Map<Integer, PlayerClass> players3 = Map.of(
            1, PlayerClass.of(30,	35,	45),
            2, PlayerClass.of(-5,	0,	10),
            3, PlayerClass.of(-40,	-35,	-25)
    );

    public static final Map<Integer, PlayerClass> players2 = Map.of(
            1, PlayerClass.of(15,	20,	30),
            2, PlayerClass.of(-35,	-30,	-20)
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
