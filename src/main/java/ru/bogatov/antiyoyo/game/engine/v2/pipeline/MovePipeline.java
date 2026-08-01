package ru.bogatov.antiyoyo.game.engine.v2.pipeline;

import lombok.Builder;
import lombok.SneakyThrows;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.MoveEvent;

import java.util.List;

@Builder
public class MovePipeline {

    private final List<PipelineStage> stages;

    @SneakyThrows
    public MoveResult execute(MoveContext context) {
        for (PipelineStage stage : stages) {
            stage.execute(context);
            if (context.isStopped()) {
                return MoveResult.failure(context.getStopReason());
            }
        }
        return MoveResult.success(context.getEvents());
    }
}
