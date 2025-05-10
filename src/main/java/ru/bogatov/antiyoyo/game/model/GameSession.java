package ru.bogatov.antiyoyo.game.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class GameSession {

    private UUID id;
    private UUID winnerId;
    private String name;
    private Map<Integer, Player> players;
    private Map<Vector3, Hex> map;
    private GameSetting setting;
    private List<MoveBackup> moveBackups;
    private Integer currentPlayerMove;
    private OffsetDateTime startTime;

}
