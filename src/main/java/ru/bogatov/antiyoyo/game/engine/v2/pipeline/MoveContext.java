package ru.bogatov.antiyoyo.game.engine.v2.pipeline;

import lombok.Getter;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.MoveEvent;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Move;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.EntityType;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class MoveContext {

    private final GameSession session;
    private final Move move;
    private final MoveType moveType;
    private final int currentPlayerIndex;
    private final Player selfPlayer;
    private final HexColor selfColor;
    private final TownHall selectedTownHall;
    private final Hex fromHex;
    private final Hex toHex;
    private final HexColor targetOriginalColor;
    private final FeatureFlags featureFlags;

    private final List<MoveEvent> events = new ArrayList<>();
    private final Map<HexColor, Set<Pair<TownHall, Set<Hex>>>> cachedRegions = new HashMap<>();
    private boolean stopped;
    private String stopReason;

    private MoveContext(GameSession session,
                        Move move,
                        MoveType moveType,
                        Player selfPlayer,
                        HexColor selfColor,
                        TownHall selectedTownHall,
                        Hex fromHex,
                        Hex toHex,
                        HexColor targetOriginalColor,
                        FeatureFlags featureFlags) {
        this.session = session;
        this.move = move;
        this.moveType = moveType;
        this.currentPlayerIndex = session.getCurrentPlayerMove() != null ? session.getCurrentPlayerMove() : 0;
        this.selfPlayer = selfPlayer;
        this.selfColor = selfColor;
        this.selectedTownHall = selectedTownHall;
        this.fromHex = fromHex;
        this.toHex = toHex;
        this.targetOriginalColor = targetOriginalColor;
        this.featureFlags = featureFlags;
    }

    public static MoveContext forMove(GameSession session, Move move, FeatureFlags flags) {
        int playerIndex = move.getPlayer() != null ? move.getPlayer() : session.getCurrentPlayerMove();
        Player player = session.getPlayers().get(playerIndex);
        HexColor color = player != null ? player.getColor() : null;
        Hex from = move.getFrom() != null ? session.getMap().get(move.getFrom()) : null;
        Hex to = move.getTo() != null ? session.getMap().get(move.getTo()) : null;
        TownHall selected = player != null ? player.getSelectedTownHall() : null;
        MoveType type = MoveTypeResolver.resolve(move, flags);
        HexColor targetOriginal = to != null ? to.getColor() : null;
        return new MoveContext(session, move, type, player, color, selected, from, to, targetOriginal, flags);
    }

    public static MoveContext forFinishTurn(GameSession session, FeatureFlags flags) {
        int playerIndex = session.getCurrentPlayerMove() != null ? session.getCurrentPlayerMove() : 0;
        Player player = session.getPlayers().get(playerIndex);
        HexColor color = player != null ? player.getColor() : null;
        return new MoveContext(session, null, MoveType.FINISH_TURN, player, color, null, null, null, null, flags);
    }

    public static MoveContext forUndo(GameSession session, FeatureFlags flags) {
        int playerIndex = session.getCurrentPlayerMove() != null ? session.getCurrentPlayerMove() : 0;
        Player player = session.getPlayers().get(playerIndex);
        HexColor color = player != null ? player.getColor() : null;
        return new MoveContext(session, null, MoveType.UNDO, player, color, null, null, null, null, flags);
    }

    public static MoveContext forClick(GameSession session, Hex hex, EntityType entityType, FeatureFlags flags) {
        int playerIndex = session.getCurrentPlayerMove() != null ? session.getCurrentPlayerMove() : 0;
        Player player = session.getPlayers().get(playerIndex);
        HexColor color = player != null ? player.getColor() : null;
        TownHall selected = player != null ? player.getSelectedTownHall() : null;
        return new MoveContext(session, null, MoveType.CLICK, player, color, selected, null, hex, null, flags);
    }

    public static MoveContext forValidate(GameSession session, FeatureFlags flags) {
        return new MoveContext(session, null, MoveType.VALIDATE, null, null, null, null, null, null, flags);
    }

    public void addEvent(MoveEvent event) {
        this.events.add(event);
    }

    public void addEvents(List<MoveEvent> events) {
        this.events.addAll(events);
    }

    public List<MoveEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public void cacheRegions(HexColor color, Set<Pair<TownHall, Set<Hex>>> regions) {
        this.cachedRegions.put(color, regions);
    }

    public Set<Pair<TownHall, Set<Hex>>> getCachedRegions(HexColor color) {
        return cachedRegions.get(color);
    }

    public boolean hasCachedRegions(HexColor color) {
        return cachedRegions.containsKey(color);
    }

    public void stop(String reason) {
        this.stopped = true;
        this.stopReason = reason;
    }

    public boolean isStopped() {
        return stopped;
    }

    public String getStopReason() {
        return stopReason;
    }

    public boolean isRedactorMode() {
        return move != null && Boolean.TRUE.equals(move.getRedactorMode());
    }
}
