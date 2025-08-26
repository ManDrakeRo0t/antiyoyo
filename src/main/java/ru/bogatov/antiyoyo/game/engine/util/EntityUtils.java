package ru.bogatov.antiyoyo.game.engine.util;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.common.Color;
import ru.bogatov.antiyoyo.game.model.common.Currency;
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
            case TOWN_HALL -> new TownHall(Currency.of(10,0,0), Currency.EMPTY.clone());
            case STONE -> new Stone();
            case FOREST -> new Forest();
            case MINE -> new Mine();
            case FOREST_FARM -> new ForestFarm();
            case MINE_FARM -> new MineFarm();
            case DRONE -> new Drone(Color.EMPTY);
            case FIRE -> new Fire();
        };
    }

}
