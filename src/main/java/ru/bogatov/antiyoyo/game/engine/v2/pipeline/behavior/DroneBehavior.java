package ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior;

import ru.bogatov.antiyoyo.game.engine.util.EntityUtils;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.*;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DroneBehavior implements Movable, Purchasable {

    private static final int DRONE_RADIUS = 4;
    private static final Currency PRICE = Currency.of(5, 0, 0);

    private static final Set<Class<? extends Entity>> STRIKE_TARGETS = Set.of(
            Field.class,
            Factory.class,
            ForestFarm.class,
            MineFarm.class,
            UnitStageOne.class,
            UnitStageTwo.class
    );

    @Override
    public Set<Hex> availableDestinations(MoveContext context, Hex source) {
        HexColor ownerColor = resolveOwnerColor(source);
        return BehaviorHelper.neighbors(context, source, DRONE_RADIUS, false).stream()
                .filter(hex -> BehaviorHelper.isDraggableTarget(hex, ownerColor))
                .collect(Collectors.toSet());
    }

    @Override
    public List<MoveEvent> onMove(MoveContext context, Hex source, Hex target) {
        HexColor ownerColor = resolveOwnerColor(source);
        List<MoveEvent> events = new ArrayList<>();
        events.add(new EntityRemovedEvent(source, true));

        if (BehaviorHelper.isDraggableTarget(target, ownerColor)) {
            int fireStage = computeFireStage(target);
            events.add(new EntityDestroyedEvent(target));
            events.add(new FireIgnitedEvent(target, fireStage));
        } else {
            Drone drone = (Drone) EntityUtils.fromType(EntityType.DRONE);
            events.add(new DronePlacedEvent(target, ownerColor, drone));
        }
        return events;
    }

    @Override
    public Set<Hex> availablePlacement(MoveContext context, TownHall townHall) {
        HexColor selfColor = context.getSelfColor();
        return BehaviorHelper.getTownHallRegion(context, townHall).getSecond().stream()
                .filter(hex -> hex.getColor() == selfColor && hex.getEntity() instanceof Field)
                .collect(Collectors.toSet());
    }

    @Override
    public Currency price(MoveContext context, Hex target) {
        return PRICE;
    }

    @Override
    public List<MoveEvent> onPurchase(MoveContext context, Hex target, TownHall townHall) {
        Currency debit = Currency.of(-PRICE.getGold(), 0, 0);
        return List.of(
                new StorageChangedEvent(townHall.getUuid(), debit),
                new EntityPlacedEvent(target, EntityUtils.fromType(EntityType.DRONE), context.getSelfColor())
        );
    }

    private HexColor resolveOwnerColor(Hex source) {
        Entity entity = source.getEntity();
        return entity instanceof Drone drone ? drone.getOwnerColor() : source.getColor();
    }

    private int computeFireStage(Hex target) {
        Entity entity = target.getEntity();
        if (entity instanceof UnitStageOne) return 1;
        if (entity instanceof UnitStageTwo) return 2;
        return 3;
    }
}
