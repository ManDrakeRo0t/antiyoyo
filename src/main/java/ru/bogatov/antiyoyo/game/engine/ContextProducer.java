package ru.bogatov.antiyoyo.game.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bogatov.antiyoyo.game.engine.util.HexCalculator;
import ru.bogatov.antiyoyo.game.model.*;
import ru.bogatov.antiyoyo.game.model.common.Color;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.entity.Drone;
import ru.bogatov.antiyoyo.game.model.entity.Entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class ContextProducer {

    public MoveContext produce(GameSession gameSession, Move move) {

        Color selfColor = move.getColor();
        Entity entity = gameSession.getMap().get(move.getClickedHex()).getEntity();
        Hex to = gameSession.getMap().get(move.getClickedHex());
        Player player = gameSession.getPlayers().get(selfColor);
        Hex from = gameSession.getPlayers().get(selfColor).getSelectedHex();


        return new MoveContext()
                .setSelfColor(selfColor)
                .setAction(getType(player, move, entity))
                .setFrom(player.getSelectedHex())
                .setTo(to)
                .setEntity(move.getEntityType())
                .setTeammates(getTeammates(selfColor, gameSession.getAlliances()))
                .setFrom(from)
                .setRegion()


    }

    private Set<Color> getTeammates(Color selfColor, List<Alliance> allianceList) {
        return allianceList.stream()
                .filter(s -> s.getTo() == selfColor || s.getFrom() == selfColor)
                .map(s -> Set.of(s.getFrom(), s.getTo()))
                .reduce(new HashSet<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                });
    }


    private MoveType getType(Player player, Move move, Entity clickedEntity) {
        if (player.getSelectedHex() == null && clickedEntity instanceof Drone) {
            return MoveType.SELECT_HEX;
        }
        if (player.getSelectedTownHall() == null) {
            return MoveType.SELECT_HEX;
        }
        if (move.getEntityType() != null) {
            return MoveType.SELECT_NEW_UNIT;
        }
        if (player.getSelectedHex() != null && move.getClickedHex() != null) {
            return MoveType.MOVE;
        }
        if (player.getSelectedEntity() != null && move.getClickedHex() != null) {
            return MoveType.PURCHASE;
        }
        return MoveType.UNKNOWN;
    }

}
