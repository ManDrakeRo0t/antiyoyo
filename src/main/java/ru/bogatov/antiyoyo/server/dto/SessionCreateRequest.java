package ru.bogatov.antiyoyo.server.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SessionCreateRequest {

    private UUID mapId;
    private Integer secondToMove;
    private Boolean grave;
    private Integer farmsDensity;
    private Boolean undoMove;
    private Boolean cut;
    private Boolean demolition;

}
