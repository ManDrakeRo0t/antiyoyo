package ru.bogatov.antiyoyo.game.engine.v2.service;

import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.engine.v2.util.EntityClassifier;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.Entity;

import java.util.Map;

@UtilityClass
public class MapUIService {

    public static void restoreAvailability(GameSession session) {
        for (Hex hex : session.getMap().values()) {
            Entity entity = hex.getEntity();
            if (entity.getMovedOnThisTurn() != null && Boolean.TRUE.equals(entity.getMovedOnThisTurn())) {
                hex.setIsAvailable(false);
            } else {
                hex.setIsAvailable(true);
            }
            hex.setDisplayDefence(false);
            hex.setGlue(false);
        }
    }

    public static void restoreMap(GameSession session) {
        for (Player player : session.getPlayers().values()) {
            player.setSelectedTownHall(null);
        }
        restoreAvailability(session);
    }

    public static void updateGlue(GameSession session) {
        Player player = session.getPlayers().get(session.getCurrentPlayerMove());
        if (player == null || player.getSelectedTownHall() == null) {
            return;
        }
        Hex townHall = RegionService.findTownHallById(session.getMap(), player.getSelectedTownHall().getUuid());
        if (townHall == null) {
            return;
        }
        RegionService.findTownHallWithRegion(session.getMap(), player.getColor(), townHall)
                .getSecond()
                .forEach(hex -> hex.setGlue(true));
    }

    public static void resetAllAvailability(Map<Vector3, Hex> map) {
        map.values().forEach(hex -> hex.setIsAvailable(false));
    }

    public static void markUnavailableForOpponents(Map<Vector3, Hex> map, HexColor selfColor) {
        map.values().stream()
                .filter(hex -> hex.getColor() != selfColor)
                .forEach(hex -> hex.setIsAvailable(false));
    }
}
