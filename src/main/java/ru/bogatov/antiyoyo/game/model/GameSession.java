package ru.bogatov.antiyoyo.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

@Data
@Document("sessions")
public class GameSession {

    @MongoId
    private UUID id;
    private UUID winnerId;
    private String name;
    private Set<String> aliveUsersId;
    private Map<Integer, Player> players;
    private Map<Vector3, Hex> map;
    private GameSetting setting;
    private Map<HexColor, Integer> powerByColor;
    @JsonIgnore
    private Stack<String> history;
    private Integer currentPlayerMove;
    private OffsetDateTime startTime;
    private OffsetDateTime lastInteraction;
    private OffsetDateTime endTime;
    private boolean started;
    private boolean ratingProcessed;
    @JsonIgnore
    private UUID skipMoveTaskId;
    private Instant endMoveTime;
    private Integer leftSecondsToMove;

}
