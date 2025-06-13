package ru.bogatov.antiyoyo.server.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class SessionCreateRequest {

    private UUID mapId;
}
