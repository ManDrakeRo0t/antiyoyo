package ru.bogatov.antiyoyo.server.dto;

import lombok.Data;
import ru.bogatov.antiyoyo.game.model.HexColor;

import java.util.UUID;

@Data
public class SessionJoinRequest {
    private HexColor color;
    private UUID userId;
    private UUID sessionId;
}
