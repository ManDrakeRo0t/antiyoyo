package ru.bogatov.antiyoyo.server.dto;

import lombok.Data;
import ru.bogatov.antiyoyo.game.model.GameSetting;

import java.util.UUID;

@Data
public class SessionResponse {

    private int connectedUsers;
    private int totalUsers;
    private UUID id;
    private String name;
    private GameSetting setting;

}
