package ru.bogatov.antiyoyo.game.engine.util;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Move;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.Drone;
import ru.bogatov.antiyoyo.game.model.entity.Field;
import ru.bogatov.antiyoyo.game.model.entity.Interactable;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
            HexColor entityColor = session.getMap().get(move.getFrom()).getEntity() instanceof Drone drone
                    && drone.getOwnerColor() == player ? drone.getOwnerColor() : null;
            if (from != player && entityColor != player) {
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

        Set<Hex> availableHexes;

        if (from == null) { // Новая покупка
            if (EntityUtils.fromType(move.getEntityType()) instanceof Field) {
                availableHexes = HexCalculator.getAvailableHexesForField(
                        gameSession.getPlayers().get(gameSession.getCurrentPlayerMove()).getSelectedTownHall().getUuid(),
                        gameSession,
                        gameSession.getPlayers().get(move.getPlayer()).getColor());
            } else {
                availableHexes = HexCalculator.getAvailableHexesForNewEntity(
                        gameSession.getPlayers().get(gameSession.getCurrentPlayerMove()).getSelectedTownHall().getUuid(),
                        gameSession,
                        gameSession.getPlayers().get(move.getPlayer()).getColor(),
                        (Interactable) EntityUtils.fromType(move.getEntityType()));
            }
        } else { // Передвижение
            availableHexes = HexCalculator.getAvailableHexesForExistingEntity(
                    gameSession.getMap(), from, gameSession.getPlayers().get(move.getPlayer()).getColor()
            );
        }

        if (!availableHexes.stream().map(Hex::getVector).collect(Collectors.toSet()).contains(to.getVector())) {
            throw new IllegalArgumentException("Can't move to not available hex");
        }
        if (from == to) {
            throw new IllegalArgumentException("Can't move to not available hex");
        }
    }

}
