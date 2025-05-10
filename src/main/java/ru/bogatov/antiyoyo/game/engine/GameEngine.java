package ru.bogatov.antiyoyo.game.engine;

import lombok.extern.slf4j.Slf4j;
import ru.bogatov.antiyoyo.game.engine.utils.MoveValidator;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Move;
import ru.bogatov.antiyoyo.game.model.entity.Entity;

@Slf4j
public class GameEngine {

    public void makeMove(GameSession session, Move move) {

        validateMove(session, move);


    }

    private void validateMove(GameSession session, Move move) {
        try {
            MoveValidator.checkPlayerOrder(session, move);
            MoveValidator.checkDeposit(session, move);
            MoveValidator.checkFromHex(session, move);

        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }
    }

    private void setEntity(GameSession session, Move move) {

    }


}
