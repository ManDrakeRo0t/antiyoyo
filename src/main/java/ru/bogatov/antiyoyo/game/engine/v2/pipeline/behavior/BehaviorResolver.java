package ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior;

import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BehaviorResolver {

    private final Map<Class<? extends Entity>, EntityBehavior> behaviors = new HashMap<>();

    public BehaviorResolver() {
        registerDefaults();
    }

    private void registerDefaults() {
        Set<Class<? extends Entity>> townHallFactoryTowerBig = Set.of(TownHall.class, Factory.class, Tower.class, BigTower.class);
        Set<Class<? extends Entity>> factoryTownHallBig = Set.of(Factory.class, TownHall.class, BigTower.class);

        Set<Class<? extends Entity>> unitOneDefault = Set.of(TownHall.class, Factory.class, Tower.class, BigTower.class);
        Set<Class<? extends Entity>> unitOneCut = Set.of(TownHall.class, Factory.class, Tower.class, BigTower.class, UnitStageTwo.class, UnitStageThree.class, Tank.class);
        Set<Class<? extends Entity>> unitTwoDefault = Set.of(TownHall.class, Factory.class, Tower.class, BigTower.class);
        Set<Class<? extends Entity>> unitTwoCut = Set.of(TownHall.class, Factory.class, Tower.class, BigTower.class, UnitStageThree.class, Tank.class);
        Set<Class<? extends Entity>> unitThreeDefault = Set.of(Factory.class, TownHall.class, BigTower.class);
        Set<Class<? extends Entity>> unitThreeCut = Set.of(Factory.class, TownHall.class, BigTower.class, Tank.class);
        Set<Class<? extends Entity>> tankDefault = Set.of(Factory.class, TownHall.class, BigTower.class);
        Set<Class<? extends Entity>> tankCut = Set.of(Factory.class, TownHall.class, BigTower.class);

        behaviors.put(UnitStageOne.class, new UnitBehavior(UnitStageOne.class, EntityType.UNIT_1, 1, Currency.of(10, 0, 0), 3, unitOneDefault, unitOneCut));
        behaviors.put(UnitStageTwo.class, new UnitBehavior(UnitStageTwo.class, EntityType.UNIT_2, 2, Currency.of(20, 0, 0), 3, unitTwoDefault, unitTwoCut));
        behaviors.put(UnitStageThree.class, new UnitBehavior(UnitStageThree.class, EntityType.UNIT_3, 3, Currency.of(30, 1, 1), 3, unitThreeDefault, unitThreeCut));
        behaviors.put(Tank.class, new UnitBehavior(Tank.class, EntityType.TANK, 4, Currency.of(40, 2, 2), 3, tankDefault, tankCut));

        behaviors.put(Factory.class, new BuildingBehavior(EntityType.FACTORY, Currency.of(0, 4, 2), BuildingBehavior.PlacementType.ADJACENT_TO_FACTORY_OR_TOWN_HALL, true));
        behaviors.put(ForestFarm.class, new BuildingBehavior(EntityType.FOREST_FARM, Currency.of(10, 0, 0), BuildingBehavior.PlacementType.ANY_OWN_FIELD, true));
        behaviors.put(MineFarm.class, new BuildingBehavior(EntityType.MINE_FARM, Currency.of(10, 0, 0), BuildingBehavior.PlacementType.ANY_OWN_FIELD, true));
        behaviors.put(Tower.class, new BuildingBehavior(EntityType.TOWER, Currency.of(5, 5, 5), BuildingBehavior.PlacementType.ANY_OWN_FIELD, true));
        behaviors.put(BigTower.class, new BuildingBehavior(EntityType.BIG_TOWER, Currency.of(10, 10, 10), BuildingBehavior.PlacementType.REPLACE_TOWER, true));

        behaviors.put(Drone.class, new DroneBehavior());

        behaviors.put(Tree.class, new ResourceBehavior(Currency.of(0, 3, 0)));
        behaviors.put(Stone.class, new ResourceBehavior(Currency.of(0, 0, 3)));

        NoOpBehavior noOp = new NoOpBehavior();
        behaviors.put(Field.class, noOp);
        behaviors.put(Grave.class, noOp);
        behaviors.put(Fire.class, noOp);
        behaviors.put(Forest.class, noOp);
        behaviors.put(Mine.class, noOp);
        behaviors.put(TownHall.class, noOp);
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityBehavior> T resolve(Entity entity, Class<T> type) {
        if (entity == null) {
            return null;
        }
        EntityBehavior behavior = behaviors.get(entity.getClass());
        if (type.isInstance(behavior)) {
            return (T) behavior;
        }
        return null;
    }

    public EntityBehavior resolve(Entity entity) {
        return entity != null ? behaviors.get(entity.getClass()) : null;
    }
}
