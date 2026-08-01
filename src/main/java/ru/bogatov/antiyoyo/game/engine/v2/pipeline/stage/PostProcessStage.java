package ru.bogatov.antiyoyo.game.engine.v2.pipeline.stage;

import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveContext;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.PipelineStage;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.MoveType;
import ru.bogatov.antiyoyo.game.engine.v2.service.*;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.entity.Drone;
import ru.bogatov.antiyoyo.game.model.entity.Entity;
import ru.bogatov.antiyoyo.game.model.entity.Fire;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

import java.util.Map;
import java.util.Set;

public class PostProcessStage implements PipelineStage {

    @Override
    public void execute(MoveContext context) {
        switch (context.getMoveType()) {
            case BUY, MOVE, DEMOLISH -> processMove(context);
            case FINISH_TURN -> processEndTurn(context);
            case UNDO -> processUndo(context);
            case CLICK, VALIDATE -> {
                // no post-processing
            }
        }
    }

    private void processMove(MoveContext context) {
        GameSession session = context.getSession();

        boolean capturedEnemy = context.getTargetOriginalColor() != null
                && context.getTargetOriginalColor() != HexColor.EMPTY
                && context.getTargetOriginalColor() != context.getSelfColor();
        if (capturedEnemy || context.getMoveType() == MoveType.DEMOLISH) {
            // In demolition color does not change, but region resources/structures may be affected minimally.
            // For enemy capture we must revalidate regions.
            if (capturedEnemy) {
                Entity oldEntity = context.getFromHex() != null ? context.getFromHex().getEntity() : null;
                RegionService.validateRegionsAfterCapture(session, oldEntity, context.getTargetOriginalColor());
            }
            DefenseService.recalculateAllDefense(session.getMap());
        }

        if (context.getSelectedTownHall() != null) {
            Hex townHallHex = RegionService.findTownHallById(session.getMap(), context.getSelectedTownHall().getUuid());
            if (townHallHex != null) {
                EconomyService.updateTownHallEconomy(session.getMap(), townHallHex);
                EconomyService.updatePricesForTownHall(RegionService.findTownHallWithRegion(session.getMap(), townHallHex.getColor(), townHallHex));
            }
            PowerService.updateDronesFlag(session, context.getSelfColor());
        }

        MapUIService.restoreAvailability(session);
        MapUIService.updateGlue(session);
    }

    private void processEndTurn(MoveContext context) {
        GameSession session = context.getSession();
        HexColor selfColor = context.getSelfColor();

        EconomyService.applyEndOfTurnEconomy(session, selfColor);
        RegionService.checkPlayersCount(session);
        MapUIService.restoreMap(session);
        FarmService.processFarms(session);
        restoreDrones(session);
        processFire(session);
        PowerService.updatePowerAndDronesAvailability(session);

        if (!isGameFinished(session)) {
            advancePlayer(session);
        }
    }

    private void processUndo(MoveContext context) {
        GameSession session = context.getSession();
        if (session.getHistory().isEmpty()) {
            return;
        }
        String snapshot = session.getHistory().pop();
        Set<Hex> restored = ru.bogatov.antiyoyo.game.engine.util.SnapshotUtils.restoreSnapshot(snapshot);
        Map<ru.bogatov.antiyoyo.game.model.common.Vector3, Hex> newMap = new java.util.HashMap<>();
        restored.forEach(hex -> newMap.put(hex.getVector(), hex));
        session.setMap(newMap);
        MapUIService.restoreMap(session);
    }

    private void restoreDrones(GameSession session) {
        session.getMap().values().stream()
                .filter(hex -> hex.getEntity() instanceof Drone)
                .forEach(hex -> hex.getEntity().setMovedOnThisTurn(false));
    }

    private void processFire(GameSession session) {
        session.getMap().values().stream()
                .filter(hex -> hex.getEntity() instanceof Fire fire)
                .forEach(hex -> {
                    Fire fire = (Fire) hex.getEntity();
                    fire.setStage(fire.getStage() - 1);
                    if (fire.getStage() <= 0) {
                        hex.setEntity(new ru.bogatov.antiyoyo.game.model.entity.Field());
                    }
                });
    }

    private boolean isGameFinished(GameSession session) {
        return session.getWinnerId() != null;
    }

    private void advancePlayer(GameSession session) {
        Map<Integer, Player> players = session.getPlayers();
        if (players.values().stream().allMatch(Player::isIlluminated)) {
            return;
        }
        session.setCurrentPlayerMove(findNextPlayer(players, session.getCurrentPlayerMove()));
    }

    private Integer findNextPlayer(Map<Integer, Player> players, Integer current) {
        Integer expected = current + 1 > players.size() - 1 ? 0 : current + 1;
        if (!players.get(expected).isIlluminated()) {
            return expected;
        }
        return findNextPlayer(players, expected);
    }
}
