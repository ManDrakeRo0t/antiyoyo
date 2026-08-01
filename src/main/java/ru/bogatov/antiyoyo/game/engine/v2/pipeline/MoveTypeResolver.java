package ru.bogatov.antiyoyo.game.engine.v2.pipeline;

import ru.bogatov.antiyoyo.game.model.Move;
import ru.bogatov.antiyoyo.game.model.entity.EntityType;

public final class MoveTypeResolver {

    private MoveTypeResolver() {
    }

    public static MoveType resolve(Move move, FeatureFlags flags) {
        if (move == null) {
            return MoveType.CLICK;
        }
        if (move.getFrom() == null) {
            if (move.getEntityType() == EntityType.FIELD) {
                return flags.demolition() ? MoveType.DEMOLISH : MoveType.BUY;
            }
            return MoveType.BUY;
        }
        return MoveType.MOVE;
    }
}
