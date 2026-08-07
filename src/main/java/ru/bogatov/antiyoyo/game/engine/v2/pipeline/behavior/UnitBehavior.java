package ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior;

import ru.bogatov.antiyoyo.game.engine.util.EntityUtils;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.*;
import ru.bogatov.antiyoyo.game.engine.v2.util.EntityClassifier;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class UnitBehavior implements Movable, Purchasable, Upgradable {

    private final Class<? extends Entity> entityClass;
    private final EntityType entityType;
    private final Supplier<Entity> factory;
    private final int level;
    private final Currency price;
    private final int moveRadius;
    private final Set<Class<? extends Entity>> defaultSpawners;
    private final Set<Class<? extends Entity>> cutSpawners;

    public UnitBehavior(Class<? extends Entity> entityClass,
                        EntityType entityType,
                        int level,
                        Currency price,
                        int moveRadius,
                        Set<Class<? extends Entity>> defaultSpawners,
                        Set<Class<? extends Entity>> cutSpawners) {
        this.entityClass = entityClass;
        this.entityType = entityType;
        this.factory = () -> EntityUtils.fromType(entityType);
        this.level = level;
        this.price = price;
        this.moveRadius = moveRadius;
        this.defaultSpawners = defaultSpawners;
        this.cutSpawners = cutSpawners;
    }

    @Override
    public Set<Hex> availableDestinations(MoveContext context, Hex source) {
        HexColor selfColor = resolveColor(source);
        if (selfColor == null) {
            return Set.of();
        }

        Set<Hex> available = new HashSet<>();
        Set<Hex> visited = new HashSet<>();
        Queue<Pair<Integer, Hex>> queue = new ArrayDeque<>();
        queue.add(Pair.of(0, source));

        while (!queue.isEmpty()) {
            Pair<Integer, Hex> current = queue.poll();
            int dist = current.getFirst();
            Hex hex = current.getSecond();
            if (visited.contains(hex)) {
                continue;
            }
            visited.add(hex);

            for (Hex neighbor : BehaviorHelper.neighbors(context, hex, 1, false)) {
                if (BehaviorHelper.isSameColor(neighbor, selfColor)) {
                    available.add(neighbor);
                    if (dist + 1 < moveRadius) {
                        queue.add(Pair.of(dist + 1, neighbor));
                    }
                } else if ((BehaviorHelper.isEnemy(neighbor, selfColor) || neighbor.getColor() == HexColor.EMPTY )&& BehaviorHelper.canCapture(neighbor, selfColor, level)) {
                    available.add(neighbor);
                }
            }
        }

        available.remove(source);
        return available.stream()
                .filter(hex -> BehaviorHelper.isSameColor(hex, selfColor)
                        ? canMoveToSelf(context, hex)
                        : BehaviorHelper.canCapture(hex, selfColor, level))
                .collect(Collectors.toSet());
    }

    @Override
    public List<MoveEvent> onMove(MoveContext context, Hex source, Hex target) {
        HexColor selfColor = resolveColor(source);
        List<MoveEvent> events = new ArrayList<>();

        if (BehaviorHelper.isSameColor(target, selfColor) && EntityClassifier.isUnit(target.getEntity())) {
            Entity existing = target.getEntity();
            if (canUpgrade(existing, factory.get())) {
                Entity merged = merge(existing, factory.get());
                if (merged != null) {
                    events.add(new EntityRemovedEvent(source, true));
                    events.add(new EntityMergedEvent(target, merged));
                    return events;
                }
            }

        }

        Entity placed = factory.get();

        if (target.getEntity() instanceof Mineable) {
            events.add(new ResourceHarvestedEvent(target, ((Mineable) target.getEntity()).getReward()));
            events.add(new EntityPlacedEvent(target, placed, selfColor, true));
        } else if (target.getEntity() instanceof Farmable) {
            events.add(new ResourceCaptureEvent(target, placed, selfColor));
        } else {
            events.add(new EntityRemovedEvent(source, true));
            events.add(new EntityPlacedEvent(target, placed, selfColor, true));
        }

        return events;
    }

    @Override
    public Set<Hex> availablePlacement(MoveContext context, TownHall townHall) {
        Pair<TownHall, Set<Hex>> regionPair = BehaviorHelper.getTownHallRegion(context, townHall);
        Set<Hex> region = regionPair.getSecond();
        if (region.isEmpty()) {
            return Set.of();
        }
        HexColor selfColor = context.getSelfColor();
        Set<Class<? extends Entity>> spawners = context.getFeatureFlags().cut() ? cutSpawners : defaultSpawners;

        Set<Hex> candidates = new HashSet<>(region);
        for (Hex hex : region) {
            candidates.addAll(BehaviorHelper.neighbors(context, hex, 1, false).stream()
                    .filter(n -> BehaviorHelper.isEnemy(n, selfColor) || n.getColor() == HexColor.EMPTY)
                    .collect(Collectors.toSet()));
        }

        return candidates.stream()
                .filter(hex -> !isFarmableBlocked(hex))
                .filter(hex -> hasSpawnerNeighbor(context, hex, spawners))
                .filter(hex -> BehaviorHelper.isSameColor(hex, selfColor)
                        ? BehaviorHelper.isFreeForUnit(hex, selfColor) || canMoveToSelf(context, hex)
                        : BehaviorHelper.canCapture(hex, selfColor, level))
                .collect(Collectors.toSet());
    }

    @Override
    public Currency price(MoveContext context, Hex target) {
        return price;
    }

    @Override
    public List<MoveEvent> onPurchase(MoveContext context, Hex target, TownHall townHall) {
        Entity placed = factory.get();
        Currency cost = price(context, target);
        Currency debit = Currency.of(-cost.getGold(), -cost.getTree(), -cost.getStone());
        if (canUpgrade(target.getEntity(), placed)) {
            Entity merged = merge(target.getEntity(), placed);
            if (merged != null) {
                return List.of(
                        new StorageChangedEvent(townHall.getUuid(), debit),
                        new EntityMergedEvent(target, merged)
                );
            }
        }
        return List.of(
                new StorageChangedEvent(townHall.getUuid(), debit),
                new EntityPlacedEvent(target, placed, context.getSelfColor())
        );
    }

    @Override
    public boolean canUpgrade(Entity existing, Entity placed) {
        if (!(existing instanceof Interactable oldUnit) || !(placed instanceof Interactable newUnit)) {
            return false;
        }
        Set<Class<? extends Entity>> mergeable = Set.of(UnitStageOne.class, UnitStageTwo.class, UnitStageThree.class);
        if (!mergeable.contains(existing.getClass()) || !mergeable.contains(placed.getClass())) {
            return false;
        }
        return oldUnit.getLevel() + newUnit.getLevel() <= 4;
    }

    @Override
    public Entity merge(Entity existing, Entity placed) {
        if (!(existing instanceof Interactable oldUnit) || !(placed instanceof Interactable newUnit)) {
            return null;
        }
        return switch (oldUnit.getLevel() + newUnit.getLevel()) {
            case 2 -> new UnitStageTwo();
            case 3 -> new UnitStageThree();
            case 4 -> new Tank();
            default -> null;
        };
    }

    private HexColor resolveColor(Hex source) {
        Entity entity = source.getEntity();
        if (entity instanceof Drone drone) {
            return drone.getOwnerColor();
        }
        return source.getColor();
    }

    private boolean canMoveToSelf(MoveContext context, Hex target) {
        HexColor selfColor = context.getSelfColor();
        Entity targetEntity = target.getEntity();
        if (BehaviorHelper.isFreeForUnit(target, selfColor)) {
            return true;
        }
        if (EntityClassifier.isUnit(targetEntity) && canUpgrade(targetEntity, factory.get())) {
            return true;
        }
        return false;
    }

    private boolean hasSpawnerNeighbor(MoveContext context, Hex hex, Set<Class<? extends Entity>> spawners) {
        HexColor selfColor = context.getSelfColor();
        return BehaviorHelper.neighbors(context, hex, 1, false).stream()
                .filter(n -> n.getColor() == selfColor)
                .map(n -> n.getEntity().getClass())
                .anyMatch(spawners::contains);
    }

    private boolean isFarmableBlocked(Hex hex) {
        return hex.getEntity() instanceof Farmable;
    }
}
