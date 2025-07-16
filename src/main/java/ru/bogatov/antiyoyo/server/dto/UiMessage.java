package ru.bogatov.antiyoyo.server.dto;

import lombok.Builder;
import lombok.Data;
import ru.bogatov.antiyoyo.game.model.common.HexColor;

@Data
@Builder
public class UiMessage {

    private String scope;
    private String type;
    private String message;

    public static UiMessage left(HexColor hexColor) {
        return UiMessage.builder()
                .type("LEFT")
                .scope("ALL")
                .message(hexColor.toString() + " left, player will illuminated in 1 minute")
                .build();
    }

    public static UiMessage illuminated(HexColor hexColor) {
        return UiMessage.builder()
                .type("ILLUMINATED")
                .scope("ALL")
                .message(hexColor.toString() + " was illuminated")
                .build();
    }

    public static UiMessage joined(HexColor hexColor) {
        return UiMessage.builder()
                .type("JOIN")
                .scope("ALL")
                .message(hexColor.toString() + " joined")
                .build();
    }

}
