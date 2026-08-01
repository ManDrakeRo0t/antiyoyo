package ru.bogatov.antiyoyo.game.engine.v2.pipeline.stage;

import org.springframework.util.CollectionUtils;
import ru.bogatov.antiyoyo.game.engine.util.SnapshotUtils;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.PipelineStage;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveType;

import java.util.Stack;

public class SnapshotStage implements PipelineStage {

    @Override
    public void execute(MoveContext context) {
        MoveType type = context.getMoveType();
        switch (type) {
            case BUY, MOVE, DEMOLISH -> saveSnapshot(context);
            case FINISH_TURN -> clearHistory(context);
            case UNDO, CLICK, VALIDATE -> {
                // no snapshot changes
            }
        }
    }

    private void saveSnapshot(MoveContext context) {
        if (CollectionUtils.isEmpty(context.getSession().getHistory())) {
            context.getSession().setHistory(new Stack<>());
        }
        context.getSession().getHistory().push(
                SnapshotUtils.makeSnapshot(context.getSession().getMap().values())
        );
    }

    private void clearHistory(MoveContext context) {
        context.getSession().setHistory(new Stack<>());
    }
}
