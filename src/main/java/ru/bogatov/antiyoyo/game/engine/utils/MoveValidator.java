package ru.bogatov.antiyoyo.game.engine.utils;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.*;
import ru.bogatov.antiyoyo.game.model.entity.Sellable;

import java.util.Objects;

/*
* Класс для проверки шагов
* 1) Проверка очередности шага
* 2) Проверка баланса
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

    public static void checkDeposit(GameSession gameSession, Move move) {
        if (move.getFrom() == null) { // Новая покупка
            Sellable sellable = (Sellable) move.getEntity();
            if (gameSession.getPlayers().get(move.getPlayer()).getBalance() < sellable.getPrice()) {
                throw new IllegalArgumentException("Need more money");
            }
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

        if (from == null) { // Новая покупка

        } else { // Передвижение

        }
    }

}
