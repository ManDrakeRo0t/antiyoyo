package ru.bogatov.antiyoyo.game.engine.util;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.*;
import ru.bogatov.antiyoyo.game.model.entity.Interactable;

import java.util.Objects;
import java.util.Set;

/*
 * Класс для проверки шагов
 * 1) Проверка очередности шаг
 * 3) Проверка клетки с
 * 4) Проверка клетка в
 *
 * */

@UtilityClass
public class MoveValidator {


    public static void checkPlayerOrder(GameSession gameSession, Move move) {
        if (!Objects.equals(move.getPlayer(), gameSession.getCurrentPlayerMove())) {
            throw new IllegalArgumentException("Wrong move order");
        }
    }


    public static void checkFromHex(GameSession session, Move move) {
        if (move.getFrom() != null) { // Передвижение
            HexColor from = session.getMap().get(move.getFrom()).getColor();
            HexColor player = session.getPlayers().get(move.getPlayer()).getColor();
            if (from != player) {
                throw new IllegalArgumentException("Can't move enemy entity");
            }
        }
    }

    public static void checkToHex(GameSession gameSession, Move move) {
        if (move.getTo() == null) {
            throw new IllegalArgumentException("Can't move to empty hex");
        }

        Hex from = gameSession.getMap().get(move.getFrom());
        Hex to = gameSession.getMap().get(move.getTo());

        Set<Vector3> availableHexes;

        if (from == null) { // Новая покупка
            availableHexes = HexCalculator.getAvailableHexesForNewEntity(
                    gameSession.getPlayers().get(gameSession.getCurrentPlayerMove()).getSelectedTownHall().getUuid(),
                    gameSession.getMap(),
                    gameSession.getPlayers().get(move.getPlayer()).getColor(),
                    (Interactable) EntityUtils.fromType(move.getEntityType()));
        } else { // Передвижение
            availableHexes = HexCalculator.getAvailableHexesForExistingEntity(
                    gameSession.getMap(), from
            );
        }

        if (!availableHexes.contains(to.getVector())) {
            throw new IllegalArgumentException("Can't move to not available hex");
        }
    }

}
