package ru.bogatov.antiyoyo.game.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import ru.bogatov.antiyoyo.game.engine.util.*;
import ru.bogatov.antiyoyo.game.model.*;
import ru.bogatov.antiyoyo.game.model.entity.*;
import ru.bogatov.antiyoyo.server.domain.GameEvent;

import java.util.*;

import static ru.bogatov.antiyoyo.game.engine.util.MapUtils.findTownHallWithRegion;
import static ru.bogatov.antiyoyo.game.engine.util.MapUtils.updateTownHallEconomy;


@Slf4j
public class GameEngine {

    @SneakyThrows
    public void makeMove(GameSession session, Move move) {

        if (!move.getRedactorMode()) {
            validateMove(session, move);
        }

        saveState(session);

        applyMove(session, move);
        MapUtils.restoreAvailability(session);
    }

    public void endMove(GameSession session) {
        //delete history
        session.setHistory(new Stack<>());
        // process map
       Player player = session.getPlayers().get(session.getCurrentPlayerMove());
       HexColor selfColor = player.getColor();
       MapUtils.getAllRegionsByColor(session.getMap(), selfColor)
               .forEach(region -> MapUtils.updateRegionAfterMove(session.getMap(), region));
       // change player
        MapUtils.checkPlayersCount(session);
        MapUtils.restoreMap(session);
        chanePlayerOrder(session);
    }

    public void chanePlayerOrder(GameSession session) {
        session.setCurrentPlayerMove(findNextPlayer(session.getPlayers(), session.getCurrentPlayerMove()));
    }

    private Integer findNextPlayer(Map<Integer, Player> players, Integer currentPlayer) {
        Integer expectedNext = currentPlayer + 1 > players.size() - 1 ? 0 : currentPlayer + 1;
        if (!players.get(expectedNext).isIlluminated()) {
            return expectedNext;
        } else {
            return findNextPlayer(players, expectedNext);
        }
    }

    public void undoMove(GameSession session) throws JsonProcessingException {

        var map = session.getHistory().pop();
        if (map != null) {
            Set<Hex> data = SnapshotUtils.restoreSnapshot(map);
            Map<Vector3, Hex> newMap = new HashMap<>();
            data.forEach(hex -> newMap.put(hex.getVector(), hex));
            session.setMap(newMap);
            MapUtils.restoreMap(session);
        }

    }


    private void saveState(GameSession session) throws JsonProcessingException {
        if (CollectionUtils.isEmpty(session.getHistory())) {
            session.setHistory(new Stack<>());
        }
        session.getHistory().push(SnapshotUtils.makeSnapshot(session.getMap().values()));
    }

    public void handleBeforeMoveClick(GameSession session, GameEvent event) {
        session.getMap().values().forEach(hex -> hex.setIsAvailable(false));

        HexColor selfColor = session.getPlayers().get(session.getCurrentPlayerMove()).getColor();

        if (event.getHex() == null || event.getHex().getColor() != selfColor) {
            if (event.getEntityType() == null) {
                MapUtils.restoreMap(session);
            }
        } else {

            Pair<TownHall, Set<Hex>> result = MapUtils.findTownHallWithRegion(session.getMap(), selfColor, event.getHex());
            session.getPlayers().get(session.getCurrentPlayerMove()).setSelectedTownHall(result.getFirst());
            MapUtils.updatePricesForTownHall(result);

            Hex hex = session.getMap().get(event.getHex().getVector());
            Entity entity = hex.getEntity();

            if (entity instanceof Interactable interactable && interactable.getClass() != TownHall.class) {
                if (interactable.getClass() == Tower.class || interactable.getClass() == BigTower.class) {
                    MapUtils.showDefenceForColor(session.getMap(), selfColor);
                } else {
                    HexCalculator.getAvailableHexesForExistingEntity(session.getMap(), hex).forEach(available -> {
                        session.getMap().get(available).setIsAvailable(true);
                    });
                }
            } else if (entity.getClass() == TownHall.class) {

            }
        }

        if (event.getHex() == null && event.getEntityType() != null) {
            if (session.getPlayers().get(session.getCurrentPlayerMove()).getSelectedTownHall() != null) {
                HexCalculator.getAvailableHexesForNewEntity(session.getPlayers().get(session.getCurrentPlayerMove()).getSelectedTownHall().getUuid(),
                        session.getMap(), selfColor, (Interactable) EntityUtils.fromType(event.getEntityType())).forEach(hex -> {
                    session.getMap().get(hex).setIsAvailable(true);
                });
            }
        }
    }

    private void validateMove(GameSession session, Move move) {

            MoveValidator.checkPlayerOrder(session, move);
            MoveValidator.checkFromHex(session, move);
            MoveValidator.checkToHex(session, move);

    }

    private void applyMove(GameSession session, Move move) {

        Hex from = getHexByCord(session, move.getFrom());
        Hex to = getHexByCord(session, move.getTo());

        var selectedTownHall = session.getPlayers().get(session.getCurrentPlayerMove()).getSelectedTownHall();
        Hex townHall = null;
        if (selectedTownHall != null) {
            townHall = HexCalculator.foundTownHallById(
                    session.getMap(),
                    selectedTownHall.getUuid()
            );
        }


        if (from != null) {
            setEntity(session, from, new Field(), from.getColor());
        } else {
            if (townHall.getEntity() instanceof TownHall townHallEntity &&
                    EntityUtils.fromType(move.getEntityType()) instanceof Sellable sellable) {
                townHallEntity.setBalance(townHallEntity.getBalance() - townHallEntity.getPrices().get(move.getEntityType()));
            }
        }
        setEntity(session,
                to,
                EntityUtils.fromType(move.getEntityType()),
                session.getPlayers().get(move.getPlayer()).getColor());

        if (townHall != null && !move.getRedactorMode()) {
            updateTownHallEconomy(session.getMap(), townHall);
        }



    }

    private Entity mergeUnit(Interactable old, Interactable toPlace) {
        return switch (old.getLevel() + toPlace.getLevel()) {
            case 2 -> new UnitStageTwo();
            case 3 -> new UnitStageThree();
            case 4 -> new Tank();
            default -> null;
        };
    }

    private void setEntity(GameSession session, Hex hex, Entity entity, HexColor newColor) {

        HexColor oldColor = hex.getColor();

        if (hex.getEntity() instanceof Interactable old
                && entity instanceof Interactable toPlace
                && hex.getColor() == newColor
                && MapUtils.moveableUnits.contains(entity.getClass())
        ) {
            Boolean isMovedPrevious = hex.getEntity().getMovedOnThisTurn();
            hex.setEntity(mergeUnit(old, toPlace));
            hex.getEntity().setMovedOnThisTurn(isMovedPrevious);
        } else {
            hex.setEntity(entity);
            hex.getEntity().setMovedOnThisTurn(MapUtils.moveableUnits.contains(entity.getClass()) ? true : null);
        }
        hex.setColor(newColor);
        if (hex.getEntity() instanceof Interactable interactable) {
            MapUtils.updateDefenseLevel(session.getMap(), hex, interactable.getLevel(), newColor);
        } else {
            MapUtils.updateDefenseLevel(session.getMap(), hex, 0, newColor);
        }

        if (entity instanceof TownHall) {
            MapUtils.updateTownHallEconomy(session.getMap(), hex);
        }

        if (oldColor != newColor) {
            validateTownHallsAndRegions(session);
        }

    }


    private Hex getHexByCord(GameSession session, Vector3 vector) {
        if (vector == null) {
            return null;
        }
        return session.getMap().get(vector);
    }

    private void validateTownHallsAndRegions(GameSession session) {
        Set<Hex> validated = new HashSet<>();
        session.getMap().values().forEach(hex -> {
            if (hex.getColor() != HexColor.EMPTY && !validated.contains(hex)) {
                try {
                    Pair<TownHall, Set<Hex>> region = findTownHallWithRegion(session.getMap(), hex.getColor(), hex);
                    if (region.getFirst() != null) {
                        validated.addAll(region.getSecond());
                    } else {
                        Hex placeForTownHall = MapUtils.findPlaceForTownHall(session.getMap(), region.getSecond());
                        if (placeForTownHall != null) {
                            setEntity(session, placeForTownHall, new TownHall(0,0), placeForTownHall.getColor());
                        } else {
                            MapUtils.killInRegion(session, region.getSecond());
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    if (hex.getEntity() instanceof TownHall townHall) {
                        Integer balance = townHall.getBalance();
                        setEntity(session, hex, new Field(), hex.getColor());
                        Pair<TownHall, Set<Hex>> region = findTownHallWithRegion(session.getMap(), hex.getColor(), hex);
                        region.getFirst().setBalance(balance + region.getFirst().getBalance());
                        System.out.println("Merged");
                    }
                }
            }
        });
    }

    public Pair<Integer, Set<HexColor>> validateSessionAndGetPlayersCount(GameSession gameSession) {
        Pair<Integer, Set<HexColor>> playersCount = MapUtils.getPlayersCount(gameSession.getMap());
        if (playersCount.getFirst() <= 1) {
            throw new IllegalArgumentException("Игроков не достаточно");
        }
        if (gameSession.getMap().isEmpty()) {
            throw new IllegalArgumentException("Карта пуста");
        }
        MapUtils.validateAllHexAreAvailable(gameSession.getMap());
        return playersCount;
    }

}
