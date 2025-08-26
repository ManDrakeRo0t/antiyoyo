package ru.bogatov.antiyoyo.server.dto;

import lombok.Data;
import ru.bogatov.antiyoyo.game.model.common.Color;

import java.util.UUID;

@Data
public class SessionJoinRequest {
    private Color color;
    private UUID userId;
    private UUID sessionId;
}
