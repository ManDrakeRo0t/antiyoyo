package ru.bogatov.antiyoyo.game.engine.v2.pipeline.stage;

import ru.bogatov.antiyoyo.game.engine.util.EntityUtils;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.*;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior.*;
import ru.bogatov.antiyoyo.game.engine.v2.util.EntityClassifier;
import ru.bogatov.antiyoyo.game.model.Move;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.Objects;
import java.util.Set;

public class ValidationStage implements PipelineStage {

    private final BehaviorResolver behaviorResolver;

    public ValidationStage(BehaviorResolver behaviorResolver) {
        this.behaviorResolver = behaviorResolver;
    }

    @Override
    public void execute(MoveContext context) {
        if (context.isRedactorMode()) {
            return;
        }

        switch (context.getMoveType()) {
            case BUY -> validateBuy(context);
            case MOVE -> validateMove(context);
            case DEMOLISH -> validateDemolish(context);
            case UNDO -> validateUndo(context);
            case FINISH_TURN, CLICK, VALIDATE -> {
                // no extra validation
            }
        }
    }

    private void validateBuy(MoveContext context) {
        Move move = context.getMove();
        if (move == null) {
            context.stop("Move is null");
            return;
        }
        if (!Objects.equals(move.getPlayer(), context.getSession().getCurrentPlayerMove())) {
            context.stop("Wrong move order");
            return;
        }
        if (context.getSelectedTownHall() == null) {
            context.stop("No town hall selected");
            return;
        }
        if (context.getToHex() == null) {
            context.stop("Target hex is null");
            return;
        }

        Entity template = EntityUtils.fromType(move.getEntityType());
        Purchasable purchasable = behaviorResolver.resolve(template, Purchasable.class);
        if (purchasable == null) {
            context.stop("Entity cannot be purchased");
            return;
        }

        Set<Hex> available = purchasable.availablePlacement(context, context.getSelectedTownHall());
        if (!available.contains(context.getToHex())) {
            context.stop("Target hex is not available for purchase");
            return;
        }

        Currency price = purchasable.price(context, context.getToHex());
        if (!context.getSelectedTownHall().getStorage().isAffordable(price)) {
            context.stop("Not enough resources");
        }
    }

    private void validateMove(MoveContext context) {
        Move move = context.getMove();
        if (move == null) {
            context.stop("Move is null");
            return;
        }
        if (!Objects.equals(move.getPlayer(), context.getSession().getCurrentPlayerMove())) {
            context.stop("Wrong move order");
            return;
        }
        if (context.getFromHex() == null || context.getToHex() == null) {
            context.stop("Source or target hex is null");
            return;
        }
        Hex from = context.getFromHex();
        HexColor playerColor = context.getSelfColor();
        Entity entity = from.getEntity();

        boolean isOwner = from.getColor() == playerColor
                || (entity instanceof Drone drone && drone.getOwnerColor() == playerColor);
        if (!isOwner) {
            context.stop("Can't move enemy entity");
            return;
        }

        Movable movable = behaviorResolver.resolve(entity, Movable.class);
        if (movable == null) {
            context.stop("Entity cannot move");
            return;
        }

        Set<Hex> available = movable.availableDestinations(context, from);
        if (!available.contains(context.getToHex())) {
            context.stop("Can't move to not available hex");
        }
    }

    private void validateDemolish(MoveContext context) {
        if (!context.getFeatureFlags().demolition()) {
            context.stop("Demolition is disabled");
            return;
        }
        if (context.getSelectedTownHall() == null) {
            context.stop("No town hall selected");
            return;
        }
        if (context.getToHex() == null || !EntityClassifier.isBuilding(context.getToHex().getEntity())) {
            context.stop("Only buildings can be demolished");
        }
    }

    private void validateUndo(MoveContext context) {
        if (!context.getFeatureFlags().undoMove()) {
            context.stop("Undo is disabled");
        }
    }
}
