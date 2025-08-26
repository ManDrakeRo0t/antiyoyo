package ru.bogatov.antiyoyo.game.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.Color;
import ru.bogatov.antiyoyo.game.model.common.Vector3;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.*;

@Data
@Document("sessions")
public class GameSession {

    @MongoId
    private UUID id;
    private UUID winnerId;
    private Integer lap;
    private String name;
    private Set<String> aliveUsersId;
    private List<Alliance> alliances;
    private Map<Color, Player> players;
    private Map<Vector3, Hex> map;
    private GameSetting setting;
    private Map<Color, Integer> powerByColor;
    @JsonIgnore
    private Stack<String> history;
    private Color currentMoveColor;
    private OffsetDateTime startTime;
    private OffsetDateTime lastInteraction;
    private OffsetDateTime endTime;
    private boolean started;
    private boolean ratingProcessed;
    @JsonIgnore
    private UUID skipMoveTaskId;
    private Instant endMoveTime;
    private Integer leftSecondsToMove;
    private Set<Color> moveOrder;
}
