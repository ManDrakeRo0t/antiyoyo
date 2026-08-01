package ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior;

import ru.bogatov.antiyoyo.game.engine.util.EntityUtils;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.*;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BuildingBehavior implements Purchasable, Demolishable {

    public enum PlacementType {
        ADJACENT_TO_FACTORY_OR_TOWN_HALL,
        ANY_OWN_FIELD,
        REPLACE_TOWER
    }

    private final EntityType entityType;
    private final Supplier<Entity> factory;
    private final Currency basePrice;
    private final PlacementType placementType;
    private final boolean supportsDemolition;

    public BuildingBehavior(EntityType entityType,
                            Currency basePrice,
                            PlacementType placementType,
                            boolean supportsDemolition) {
        this.entityType = entityType;
        this.factory = () -> EntityUtils.fromType(entityType);
        this.basePrice = basePrice;
        this.placementType = placementType;
        this.supportsDemolition = supportsDemolition;
    }

    @Override
    public Set<Hex> availablePlacement(MoveContext context, TownHall townHall) {
        Pair<TownHall, Set<Hex>> regionPair = BehaviorHelper.getTownHallRegion(context, townHall);
        Set<Hex> region = regionPair.getSecond();
        if (region.isEmpty()) {
            return Set.of();
        }
        HexColor selfColor = context.getSelfColor();

        return region.stream()
                .filter(hex -> BehaviorHelper.isSameColor(hex, selfColor))
                .filter(this::canPlaceHere)
                .filter(hex -> placementType != PlacementType.ADJACENT_TO_FACTORY_OR_TOWN_HALL
                        || BehaviorHelper.hasSpawnerNeighbor(context, hex, Set.of(Factory.class, TownHall.class)))
                .collect(Collectors.toSet());
    }

    @Override
    public Currency price(MoveContext context, Hex target) {
        if (entityType == EntityType.FACTORY && context.getSelectedTownHall() != null) {
            Pair<TownHall, Set<Hex>> region = BehaviorHelper.getTownHallRegion(context, context.getSelectedTownHall());
            int count = (int) region.getSecond().stream().filter(h -> h.getEntity() instanceof Factory).count();
            return new Factory().getPrice(count);
        }
        return basePrice;
    }

    @Override
    public List<MoveEvent> onPurchase(MoveContext context, Hex target, TownHall townHall) {
        Currency cost = price(context, target);
        Currency debit = Currency.of(-cost.getGold(), -cost.getTree(), -cost.getStone());
        return List.of(
                new StorageChangedEvent(townHall.getUuid(), debit),
                new EntityPlacedEvent(target, factory.get(), context.getSelfColor())
        );
    }

    @Override
    public Currency refund(MoveContext context, Hex target) {
        return price(context, target).split(2);
    }

    private boolean canPlaceHere(Hex hex) {
        Entity entity = hex.getEntity();
        return switch (placementType) {
            case REPLACE_TOWER -> entity instanceof Field || entity instanceof Tower;
            default -> entity instanceof Field;
        };
    }
}
