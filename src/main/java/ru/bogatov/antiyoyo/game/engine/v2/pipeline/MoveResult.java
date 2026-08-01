package ru.bogatov.antiyoyo.game.engine.v2.pipeline;

import lombok.Builder;
import lombok.Getter;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.MoveEvent;

import java.util.List;

@Getter
@Builder
public class MoveResult {

    private final boolean success;
    private final String errorMessage;
    private final List<MoveEvent> generatedEvents;

    public static MoveResult success(List<MoveEvent> events) {
        return MoveResult.builder()
                .success(true)
                .generatedEvents(events)
                .build();
    }

    public static MoveResult failure(String message) {
        return MoveResult.builder()
                .success(false)
                .errorMessage(message)
                .generatedEvents(List.of())
                .build();
    }
}
