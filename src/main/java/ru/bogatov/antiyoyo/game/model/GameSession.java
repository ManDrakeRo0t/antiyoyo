package ru.bogatov.antiyoyo.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;

@Data
public class GameSession {

    private Integer forUser;
    private UUID id;
    private UUID winnerId;
    private String name;
    private Map<Integer, Player> players;
    private Map<Vector3, Hex> map;
    private GameSetting setting;
    private Map<HexColor, Integer> powerByColor;
    @JsonIgnore
    private Stack<String> history;
    private Integer currentPlayerMove;
    private OffsetDateTime startTime;

}
