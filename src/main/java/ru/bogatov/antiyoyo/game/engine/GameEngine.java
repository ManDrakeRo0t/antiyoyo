package ru.bogatov.antiyoyo.game.engine;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import ru.bogatov.antiyoyo.game.engine.util.*;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.Move;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;
import ru.bogatov.antiyoyo.server.domain.GameEvent;

import java.util.*;

import static ru.bogatov.antiyoyo.game.engine.util.MapUtils.*;


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
                .forEach(region -> MapUtils.updateRegionAfterMove(session, region));
        // change player
        MapUtils.checkPlayersCount(session);
        MapUtils.restoreMap(session);
        MapUtils.processFarms(session);
        MapUtils.restoreDrones(session);
        MapUtils.processFire(session);
        MapUtils.updatePowerAndDronesAvailability(session);
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

    public void undoMove(GameSession session) {

        if (!session.getSetting().getUndoMove()) {
            return;
        }

        var map = session.getHistory().pop();
        if (map != null) {
            Set<Hex> data = SnapshotUtils.restoreSnapshot(map);
            Map<Vector3, Hex> newMap = new HashMap<>();
            data.forEach(hex -> newMap.put(hex.getVector(), hex));
            session.setMap(newMap);
            MapUtils.restoreMap(session);
        }

    }


    private void saveState(GameSession session) {
        if (CollectionUtils.isEmpty(session.getHistory())) {
            session.setHistory(new Stack<>());
        }
        session.getHistory().push(SnapshotUtils.makeSnapshot(session.getMap().values()));
    }

    public void handleBeforeMoveClick(GameSession session, GameEvent event) {
        session.getMap().values().forEach(hex -> hex.setIsAvailable(false));

        HexColor selfColor = session.getPlayers().get(session.getCurrentPlayerMove()).getColor();


        if (event.getHex() == null || !HexCalculator.canInteractWithHex(session.getMap().get(event.getHex().getVector()), selfColor)) {
            if (event.getEntityType() == null) {
                MapUtils.restoreMap(session);
            }
        } else {
            Pair<TownHall, Set<Hex>> result = MapUtils.findTownHallWithRegion(session.getMap(), selfColor, event.getHex());
            session.getPlayers().get(session.getCurrentPlayerMove()).setSelectedTownHall(result.getFirst());
            MapUtils.updatePricesForTownHall(result);
            MapUtils.updateDronesFlag(session, selfColor);
            updateTownHallEconomy(result);
            Hex hex = session.getMap().get(event.getHex().getVector());
            Entity entity = hex.getEntity();

            if (entity instanceof Interactable interactable && interactable.getClass() != TownHall.class) {
                if (interactable.getClass() == Tower.class || interactable.getClass() == BigTower.class) {
                    MapUtils.showDefenceForColor(session.getMap(), selfColor);
                } else {
                    HexCalculator.getAvailableHexesForExistingEntity(session.getMap(), hex, selfColor).forEach(available -> {
                        session.getMap().get(available.getVector()).setIsAvailable(true);
                    });
                }
            }
        }

        if (event.getHex() == null && event.getEntityType() != null) {
            if (session.getPlayers().get(session.getCurrentPlayerMove()).getSelectedTownHall() != null) {
                HexCalculator.getAvailableHexesForNewEntity(session.getPlayers().get(session.getCurrentPlayerMove()).getSelectedTownHall().getUuid(),
                        session, selfColor, (Interactable) EntityUtils.fromType(event.getEntityType())).forEach(hex -> {
                    session.getMap().get(hex.getVector()).setIsAvailable(true);
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
        HexColor selfColor = session.getPlayers().get(move.getPlayer()).getColor();
        boolean skipMove = false;


        var selectedTownHall = session.getPlayers().get(session.getCurrentPlayerMove()).getSelectedTownHall();
        Hex townHall = null;
        if (selectedTownHall != null) {
            townHall = HexCalculator.foundTownHallById(
                    session.getMap(),
                    selectedTownHall.getUuid()
            );
        }

        if (from != null) {
            if (to.getEntity() instanceof Farmable) {
                skipMove = true;
            } else {
                setEntity(session, from, new Field(), from.getColor());
            }
        } else {
            if (townHall != null && townHall.getEntity() instanceof TownHall townHallEntity &&
                    EntityUtils.fromType(move.getEntityType()) instanceof Sellable) {
                townHallEntity.getStorage().remove(townHallEntity.getPrices().get(move.getEntityType()));
            }
        }
        if (townHall != null && townHall.getEntity() instanceof TownHall townHallEntity &&
                to.getEntity() != null && to.getEntity() instanceof Mineable mineable) {
            townHallEntity.getStorage().add(mineable.getReward());
        }
        if (!skipMove) {
            setEntity(session, to, EntityUtils.fromType(move.getEntityType()), session.getPlayers().get(move.getPlayer()).getColor());
        } else {
            to.setColor(session.getPlayers().get(move.getPlayer()).getColor());
            from.getEntity().setMovedOnThisTurn(true);
        }

        if (townHall != null && !move.getRedactorMode()) {
            updateTownHallEconomy(session.getMap(), townHall);
            MapUtils.updatePricesForTownHall(findTownHallWithRegion(session.getMap(), selfColor, townHall));
            MapUtils.updateDronesFlag(session, selfColor);
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

    private void setEntity(GameSession session, Hex hex, Entity newEntity, HexColor newColor) {

        HexColor oldColor = hex.getColor();
        Entity oldEntity = hex.getEntity();

        if (oldEntity instanceof Interactable old
                && newEntity instanceof Interactable toPlace
                && hex.getColor() == newColor
                && MapUtils.moveableUnits.contains(newEntity.getClass())
                && !(toPlace instanceof Drone) && !(old instanceof Drone)
        ) {
            Boolean isMovedPrevious = hex.getEntity().getMovedOnThisTurn();
            hex.setEntity(mergeUnit(old, toPlace));
            hex.getEntity().setMovedOnThisTurn(isMovedPrevious);
        } else {
            hex.setEntity(newEntity);
            hex.getEntity().setMovedOnThisTurn(MapUtils.moveableUnits.contains(newEntity.getClass()) ? true : null);
        }
        if (newEntity instanceof Drone) {
            if (oldEntity instanceof Field) {
                ((Drone) newEntity).setOwnerColor(newColor);
            } else {
                if (dieableUnits.contains(oldEntity.getClass())) {
                    hex.setEntity(new Fire(1));
                } else {
                    hex.setEntity(new Fire());
                }
            }
        } else {
            hex.setColor(newColor);
        }
        if (hex.getEntity() instanceof Interactable interactable) {
            MapUtils.updateDefenseLevel(session.getMap(), hex, interactable.getLevel(), newColor);
        } else {
            MapUtils.updateDefenseLevel(session.getMap(), hex, 0, newColor);
        }

        if (newEntity instanceof TownHall) {
            MapUtils.updateTownHallEconomy(session.getMap(), hex);
        }

        if (oldColor != newColor) {
            if (oldEntity instanceof Interactable) {
                MapUtils.updateDefenseLevelForColor(session.getMap(), hex, oldColor);
            }
            validateTownHallsAndRegions(session, oldEntity, oldColor);
        }

    }


    private Hex getHexByCord(GameSession session, Vector3 vector) {
        if (vector == null) {
            return null;
        }
        return session.getMap().get(vector);
    }

    private void validateTownHallsAndRegions(GameSession session, Entity oldEntity, HexColor oldColor) {
        Currency oldBalance = Currency.EMPTY.clone();
        Set<TownHall> createdTownHall = new HashSet<>();
        if (oldEntity instanceof TownHall townHall) {
            oldBalance = townHall.getStorage();
        }
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
                            TownHall townHall = new TownHall(Currency.EMPTY.clone(), Currency.EMPTY.clone());
                            if (placeForTownHall.getColor() == oldColor) {
                                createdTownHall.add(townHall);
                            }
                            setEntity(session, placeForTownHall, townHall, placeForTownHall.getColor());
                        } else {
                            MapUtils.killInRegion(session, region.getSecond());
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    if (hex.getEntity() instanceof TownHall townHall) {
                        Currency balance = townHall.getStorage();
                        setEntity(session, hex, new Field(), hex.getColor());
                        Pair<TownHall, Set<Hex>> region = findTownHallWithRegion(session.getMap(), hex.getColor(), hex);
                        region.getFirst().getStorage().add(balance);
                        System.out.println("Merged");
                    }
                }
            }
        });
        if (!createdTownHall.isEmpty()) {
            Currency storagePerTownHall = oldBalance.split(createdTownHall.size());
            createdTownHall.forEach(townHall -> {
                townHall.setStorage(storagePerTownHall.clone());
                if (townHall.getStorage().getGold() + townHall.getStorageUpdate().getGold() < 0) {
                    killInRegion(session, townHall);
                }
            });
        }
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
