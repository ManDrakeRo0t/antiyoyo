package ru.bogatov.antiyoyo.game.engine;

import org.springframework.stereotype.Component;
import ru.bogatov.antiyoyo.game.model.*;
import ru.bogatov.antiyoyo.game.model.common.Color;
import ru.bogatov.antiyoyo.game.model.entity.Entity;

@Component
public class ContextProducer {

    public MoveContext produce(GameSession gameSession, Move move) {

        Color selfColor = move.getColor();
        Entity entity = gameSession.getMap().get(move.getClickedHex()).getEntity();
        Player player = gameSession.getPlayers().get(selfColor);

        return new MoveContext()
                .setAction()
                .setFrom()



    }

    public MoveType getType(Player player, Move move) {

    }

}
