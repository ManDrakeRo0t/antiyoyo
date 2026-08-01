package ru.bogatov.antiyoyo.game.engine.v2.util;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.Set;

@UtilityClass
public class EntityClassifier {

    public static final Set<Class<? extends Entity>> MOVEABLE_UNITS = Set.of(
            UnitStageTwo.class,
            UnitStageOne.class,
            UnitStageThree.class,
            Tank.class,
            Drone.class
    );

    public static final Set<Class<? extends Entity>> DIEABLE_UNITS = Set.of(
            UnitStageTwo.class,
            UnitStageOne.class,
            UnitStageThree.class,
            Tank.class
    );

    public static final Set<Class<? extends Entity>> BUILDINGS = Set.of(
            Factory.class,
            ForestFarm.class,
            MineFarm.class
    );

    public static boolean isMoveableUnit(Entity entity) {
        return entity != null && MOVEABLE_UNITS.contains(entity.getClass());
    }

    public static boolean isDieableUnit(Entity entity) {
        return entity != null && DIEABLE_UNITS.contains(entity.getClass());
    }

    public static boolean isBuilding(Entity entity) {
        return entity != null && BUILDINGS.contains(entity.getClass());
    }

    public static boolean isUnit(Entity entity) {
        return entity instanceof UnitStageOne
                || entity instanceof UnitStageTwo
                || entity instanceof UnitStageThree
                || entity instanceof Tank;
    }
}
