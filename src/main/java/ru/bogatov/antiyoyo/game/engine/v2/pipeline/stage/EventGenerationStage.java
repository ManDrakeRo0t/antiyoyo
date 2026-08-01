package ru.bogatov.antiyoyo.game.engine.v2.pipeline.stage;

import ru.bogatov.antiyoyo.game.engine.util.EntityUtils;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.PipelineStage;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveType;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior.*;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.*;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class EventGenerationStage implements PipelineStage {

    private final BehaviorResolver behaviorResolver;

    public EventGenerationStage(BehaviorResolver behaviorResolver) {
        this.behaviorResolver = behaviorResolver;
    }

    @Override
    public void execute(MoveContext context) {
        List<MoveEvent> events = switch (context.getMoveType()) {
            case BUY -> generateBuyEvents(context);
            case MOVE -> generateMoveEvents(context);
            case DEMOLISH -> generateDemolishEvents(context);
            default -> List.of();
        };
        context.addEvents(events);
    }

    private List<MoveEvent> generateBuyEvents(MoveContext context) {
        Entity template = EntityUtils.fromType(context.getMove().getEntityType());
        Purchasable purchasable = behaviorResolver.resolve(template, Purchasable.class);
        if (purchasable == null) {
            context.stop("Entity cannot be purchased");
            return List.of();
        }
        return purchasable.onPurchase(context, context.getToHex(), context.getSelectedTownHall());
    }

    private List<MoveEvent> generateMoveEvents(MoveContext context) {
        Entity entity = context.getFromHex().getEntity();
        Movable movable = behaviorResolver.resolve(entity, Movable.class);
        if (movable == null) {
            context.stop("Entity cannot move");
            return List.of();
        }
        return movable.onMove(context, context.getFromHex(), context.getToHex());
    }

    private List<MoveEvent> generateDemolishEvents(MoveContext context) {
        Hex target = context.getToHex();
        Entity entity = target.getEntity();
        Demolishable demolishable = behaviorResolver.resolve(entity, Demolishable.class);
        if (demolishable == null) {
            context.stop("Entity cannot be demolished");
            return List.of();
        }
        List<MoveEvent> events = new ArrayList<>();
        events.add(new StorageChangedEvent(context.getSelectedTownHall().getUuid(), demolishable.refund(context, target)));
        events.add(new EntityRemovedEvent(target, true));
        return events;
    }
}
