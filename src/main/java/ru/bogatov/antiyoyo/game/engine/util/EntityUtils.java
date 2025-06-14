package ru.bogatov.antiyoyo.game.engine.util;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.entity.*;

@UtilityClass
public class EntityUtils {

    public static Entity fromType(EntityType type) {
        return switch (type) {
            case TANK -> new Tank();
            case UNIT_1 -> new UnitStageOne();
            case UNIT_2 -> new UnitStageTwo();
            case UNIT_3 -> new UnitStageThree();
            case TOWER -> new Tower();
            case BIG_TOWER -> new BigTower();
            case FACTORY -> new Factory();
            case FIELD -> new Field();
            case TREE -> new Tree();
            case GRAVE -> new Grave();
            case TOWN_HALL -> new TownHall(10,0);
        };
    }

}
