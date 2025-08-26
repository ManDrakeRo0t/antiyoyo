package ru.bogatov.antiyoyo.server.dto;

import lombok.Builder;
import lombok.Data;
import ru.bogatov.antiyoyo.game.model.common.Color;

@Data
@Builder
public class UiMessage {

    private String scope;
    private String type;
    private String message;

    public static UiMessage left(Color color) {
        return UiMessage.builder()
                .type("LEFT")
                .scope("ALL")
                .message(color.toString() + " left, player will illuminated in 1 minute")
                .build();
    }

    public static UiMessage illuminated(Color color) {
        return UiMessage.builder()
                .type("ILLUMINATED")
                .scope("ALL")
                .message(color.toString() + " was illuminated")
                .build();
    }

    public static UiMessage joined(Color color) {
        return UiMessage.builder()
                .type("JOIN")
                .scope("ALL")
                .message(color.toString() + " joined")
                .build();
    }

}
