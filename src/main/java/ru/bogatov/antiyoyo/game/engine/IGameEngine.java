package ru.bogatov.antiyoyo.game.engine;

import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Move;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.server.domain.GameEvent;

import java.util.Set;

public interface IGameEngine {
    void makeMove(GameSession session, Move move);

    void endMove(GameSession session);

    void undoMove(GameSession session);

    void handleBeforeMoveClick(GameSession session, GameEvent event);

    Pair<Integer, Set<HexColor>> validateSessionAndGetPlayersCount(GameSession gameSession);
}
