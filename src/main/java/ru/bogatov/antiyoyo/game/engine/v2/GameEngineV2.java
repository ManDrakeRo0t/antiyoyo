package ru.bogatov.antiyoyo.game.engine.v2;

import lombok.extern.slf4j.Slf4j;
import ru.bogatov.antiyoyo.game.engine.IGameEngine;
import ru.bogatov.antiyoyo.game.engine.util.EntityUtils;
import ru.bogatov.antiyoyo.game.engine.util.Pair;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.*;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.applier.EventApplierRegistry;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.behavior.*;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.event.MoveEvent;
import ru.bogatov.antiyoyo.game.engine.v2.pipeline.stage.*;
import ru.bogatov.antiyoyo.game.engine.v2.service.*;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Move;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;
import ru.bogatov.antiyoyo.server.domain.GameEvent;

import java.util.*;

@Slf4j
public class GameEngineV2 implements IGameEngine {

    private final BehaviorResolver behaviorResolver;
    private final EventApplierRegistry applierRegistry;
    private final MovePipeline movePipeline;

    public GameEngineV2() {
        this.behaviorResolver = new BehaviorResolver();
        this.applierRegistry = EventApplierRegistry.defaults();
        this.movePipeline = MovePipeline.builder()
                .stages(List.of(
                        new ValidationStage(behaviorResolver),
                        new SnapshotStage(),
                        new EventGenerationStage(behaviorResolver),
                        new EventApplicationStage(applierRegistry),
                        new PostProcessStage()
                ))
                .build();
    }

    public void makeMove(GameSession session, Move move) {
        FeatureFlags flags = FeatureFlags.from(session.getSetting());
        MoveContext context = MoveContext.forMove(session, move, flags);
        MoveResult result = movePipeline.execute(context);
        if (!result.isSuccess()) {
            throw new IllegalArgumentException(result.getErrorMessage());
        }
    }

    public void endMove(GameSession session) {
        FeatureFlags flags = FeatureFlags.from(session.getSetting());
        MoveContext context = MoveContext.forFinishTurn(session, flags);
        MoveResult result = movePipeline.execute(context);
        if (!result.isSuccess()) {
            throw new IllegalArgumentException(result.getErrorMessage());
        }
    }

    public void undoMove(GameSession session) {
        FeatureFlags flags = FeatureFlags.from(session.getSetting());
        MoveContext context = MoveContext.forUndo(session, flags);
        movePipeline.execute(context);
    }

    public void handleBeforeMoveClick(GameSession session, GameEvent event) {
        FeatureFlags flags = FeatureFlags.from(session.getSetting());
        Player player = session.getPlayers().get(session.getCurrentPlayerMove());
        if (player == null) {
            return;
        }
        HexColor selfColor = player.getColor();

        // reset glue
        session.getMap().values().forEach(hex -> hex.setGlue(false));

        Hex hex = event.getHex() == null ? null : session.getMap().get(event.getHex().getVector());
        boolean canInteract = hex != null && canInteractWithHex(hex, selfColor);

        if (canInteract) {
            Pair<TownHall, Set<Hex>> region = RegionService.findTownHallWithRegion(session.getMap(), selfColor, hex);
            player.setSelectedTownHall(region.getFirst());

            if (region.getFirst() != null) {
                EconomyService.updatePricesForTownHall(region);
                PowerService.updateDronesFlag(session, selfColor);
                EconomyService.updateTownHallEconomy(region);
            }

            MapUIService.markUnavailableForOpponents(session.getMap(), selfColor);
            region.getSecond().forEach(h -> h.setGlue(true));

            Entity entity = hex.getEntity();
            if (entity instanceof Interactable interactable && !(entity instanceof TownHall)) {
                MapUIService.resetAllAvailability(session.getMap());
                if (entity instanceof Tower || entity instanceof BigTower) {
                    DefenseService.showDefenceForColor(session.getMap(), selfColor);
                } else {
                    Boolean alreadyMoved = entity.getMovedOnThisTurn();
                    if (!Boolean.TRUE.equals(alreadyMoved)) {
                        Movable movable = behaviorResolver.resolve(entity, Movable.class);
                        if (movable != null) {
                            movable.availableDestinations(MoveContext.forClick(session, hex, null, flags), hex)
                                    .forEach(available -> available.setIsAvailable(true));
                        }
                    }
                }
            }
        } else {
            if (event.getEntityType() == null) {
                MapUIService.restoreMap(session);
            }
        }

        if (hex == null && event.getEntityType() != null && player.getSelectedTownHall() != null) {
            MapUIService.resetAllAvailability(session.getMap());
            Entity template = EntityUtils.fromType(event.getEntityType());
            if (template instanceof Interactable) {
                Purchasable purchasable = behaviorResolver.resolve(template, Purchasable.class);
                if (purchasable != null) {
                    purchasable.availablePlacement(MoveContext.forClick(session, null, event.getEntityType(), flags), player.getSelectedTownHall())
                            .forEach(available -> available.setIsAvailable(true));
                }
            } else if (template instanceof Field && flags.demolition()) {
                TownHall townHall = player.getSelectedTownHall();
                Hex townHallHex = RegionService.findTownHallById(session.getMap(), townHall.getUuid());
                if (townHallHex != null) {
                    RegionService.findTownHallWithRegion(session.getMap(), selfColor, townHallHex).getSecond().stream()
                            .filter(h -> ru.bogatov.antiyoyo.game.engine.v2.util.EntityClassifier.isBuilding(h.getEntity()))
                            .forEach(h -> h.setIsAvailable(true));
                }
            }
        }
    }

    public Pair<Integer, Set<HexColor>> validateSessionAndGetPlayersCount(GameSession gameSession) {
        Pair<Integer, Set<HexColor>> playersCount = RegionService.getPlayersCount(gameSession.getMap());
        if (playersCount.getFirst() <= 1) {
            throw new IllegalArgumentException("Игроков не достаточно");
        }
        if (gameSession.getMap().isEmpty()) {
            throw new IllegalArgumentException("Карта пуста");
        }
        if (playersCount.getFirst() > 8) {
            throw new IllegalArgumentException("Максимально 8 игроков");
        }
        RegionService.validateAllHexAreAvailable(gameSession.getMap());
        return playersCount;
    }

    private boolean canInteractWithHex(Hex hex, HexColor selfColor) {
        return hex.getColor() == selfColor
                || (hex.getEntity() instanceof Drone drone && drone.getOwnerColor() == selfColor);
    }
}
