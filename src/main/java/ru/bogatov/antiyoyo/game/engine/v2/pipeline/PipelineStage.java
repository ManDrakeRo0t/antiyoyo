package ru.bogatov.antiyoyo.game.engine.v2.pipeline;

public interface PipelineStage {

    void execute(MoveContext context);
}
