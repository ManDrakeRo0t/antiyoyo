package ru.bogatov.antiyoyo.game.engine.v2;

import org.junit.jupiter.api.Test;
import ru.bogatov.antiyoyo.game.engine.util.HexMapGenerator;
import ru.bogatov.antiyoyo.game.engine.v2.service.EconomyService;
import ru.bogatov.antiyoyo.game.engine.v2.service.PowerService;
import ru.bogatov.antiyoyo.game.model.GameSession;
import ru.bogatov.antiyoyo.game.model.GameSetting;
import ru.bogatov.antiyoyo.game.model.Move;
import ru.bogatov.antiyoyo.game.model.Player;
import ru.bogatov.antiyoyo.game.model.common.Currency;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.HexColor;
import ru.bogatov.antiyoyo.game.model.common.Vector3;
import ru.bogatov.antiyoyo.game.model.entity.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineV2Test {

    private final GameEngineV2 engine = new GameEngineV2();

    private GameSession createTwoPlayerSession() {
        GameSession session = new GameSession();
        session.setId(UUID.randomUUID());
        session.setCurrentPlayerMove(0);
        session.setHistory(new java.util.Stack<>());
        session.setPlayers(new HashMap<>());
        session.setAliveUsersId(new HashSet<>());
        session.setSetting(defaultSettings());

        Map<Vector3, Hex> map = new HashMap<>(HexMapGenerator.generateEmptyMap(2));

        Vector3 redTown = Vector3.from(0, 0, 0);
        Vector3 redField = Vector3.from(1, -1, 0);
        Vector3 blueTown = Vector3.from(2, -2, 0);
        Vector3 blueField = Vector3.from(1, -2, 1);

        placeTownHall(map, redTown, HexColor.RED);
        placeField(map, redField, HexColor.RED);
        placeTownHall(map, blueTown, HexColor.BLUE);
        placeField(map, blueField, HexColor.BLUE);

        session.setMap(map);

        Player red = new Player(UUID.randomUUID(), HexColor.RED, null, false, null);
        Player blue = new Player(UUID.randomUUID(), HexColor.BLUE, null, false, null);
        session.getPlayers().put(0, red);
        session.getPlayers().put(1, blue);
        session.getAliveUsersId().add(red.getUserId().toString());
        session.getAliveUsersId().add(blue.getUserId().toString());

        EconomyService.updateAllEconomies(map);
        EconomyService.updatePricesForAll(map);
        PowerService.updatePowerAndDronesAvailability(session);

        return session;
    }

    private GameSetting defaultSettings() {
        GameSetting setting = new GameSetting();
        setting.setUndoMove(false);
        setting.setGrave(false);
        setting.setDemolition(false);
        setting.setCut(false);
        setting.setFarmsDensity(0);
        setting.setSecondsToMove(60);
        return setting;
    }

    private void placeTownHall(Map<Vector3, Hex> map, Vector3 vector, HexColor color) {
        Hex hex = map.get(vector);
        hex.setColor(color);
        hex.setEntity(new TownHall(Currency.of(20, 0, 0), Currency.EMPTY.clone()));
        hex.getEntity().setMovedOnThisTurn(null);
    }

    private void placeField(Map<Vector3, Hex> map, Vector3 vector, HexColor color) {
        Hex hex = map.get(vector);
        hex.setColor(color);
        hex.setEntity(new Field());
        hex.getEntity().setMovedOnThisTurn(null);
    }

    @Test
    void endTurnAppliesIncome() {
        GameSession session = createTwoPlayerSession();
        int initialGold = session.getPlayers().get(0).getColor() == HexColor.RED
                ? getTownHall(session, HexColor.RED).getStorage().getGold()
                : 0;
        assertTrue(initialGold >= 0);

        int before = getTownHall(session, HexColor.RED).getStorage().getGold();
        engine.endMove(session);
        int after = getTownHall(session, HexColor.RED).getStorage().getGold();

        assertTrue(after > before, "Gold should increase after end turn income");
    }

    @Test
    void buyUnitPlacesEntityAndChargesStorage() {
        GameSession session = createTwoPlayerSession();
        Hex townHallHex = findHexWithEntity(session, TownHall.class, HexColor.RED);
        TownHall townHall = (TownHall) townHallHex.getEntity();
        session.getPlayers().get(0).setSelectedTownHall(townHall);

        Hex target = session.getMap().get(Vector3.from(1, -1, 0));
        assertInstanceOf(Field.class, target.getEntity());

        int beforeGold = townHall.getStorage().getGold();

        Move move = new Move();
        move.setPlayer(0);
        move.setTo(target.getVector());
        move.setEntityType(EntityType.UNIT_1);
        move.setRedactorMode(false);

        engine.makeMove(session, move);

        assertInstanceOf(UnitStageOne.class, target.getEntity());
        assertEquals(HexColor.RED, target.getColor());
        assertEquals(beforeGold - 10, townHall.getStorage().getGold());
    }

    @Test
    void moveUnitRelocatesEntity() {
        GameSession session = createTwoPlayerSession();
        Hex townHallHex = findHexWithEntity(session, TownHall.class, HexColor.RED);
        TownHall townHall = (TownHall) townHallHex.getEntity();
        session.getPlayers().get(0).setSelectedTownHall(townHall);

        Hex unitHex = session.getMap().get(Vector3.from(1, -1, 0));
        unitHex.setEntity(new UnitStageOne());
        unitHex.getEntity().setMovedOnThisTurn(false);

        Hex target = session.getMap().get(Vector3.from(2, -1, -1));
        target.setColor(HexColor.RED);
        target.setEntity(new Field());

        Move move = new Move();
        move.setPlayer(0);
        move.setFrom(unitHex.getVector());
        move.setTo(target.getVector());
        move.setRedactorMode(false);

        engine.makeMove(session, move);

        assertInstanceOf(Field.class, unitHex.getEntity());
        assertInstanceOf(UnitStageOne.class, target.getEntity());
        assertTrue(target.getEntity().getMovedOnThisTurn());
    }

    @Test
    void undoMoveRestoresPreviousState() {
        GameSession session = createTwoPlayerSession();
        session.getSetting().setUndoMove(true);
        Hex townHallHex = findHexWithEntity(session, TownHall.class, HexColor.RED);
        TownHall townHall = (TownHall) townHallHex.getEntity();
        session.getPlayers().get(0).setSelectedTownHall(townHall);

        Hex target = session.getMap().get(Vector3.from(1, -1, 0));
        Move move = new Move();
        move.setPlayer(0);
        move.setTo(target.getVector());
        move.setEntityType(EntityType.UNIT_1);
        move.setRedactorMode(false);

        engine.makeMove(session, move);
        assertInstanceOf(UnitStageOne.class, target.getEntity());

        engine.undoMove(session);
        Hex restored = session.getMap().get(target.getVector());
        assertInstanceOf(Field.class, restored.getEntity());
    }

    private TownHall getTownHall(GameSession session, HexColor color) {
        return session.getMap().values().stream()
                .filter(hex -> hex.getColor() == color && hex.getEntity() instanceof TownHall)
                .map(hex -> (TownHall) hex.getEntity())
                .findFirst()
                .orElseThrow();
    }

    private Hex findHexWithEntity(GameSession session, Class<? extends Entity> type, HexColor color) {
        return session.getMap().values().stream()
                .filter(hex -> hex.getColor() == color && type.isInstance(hex.getEntity()))
                .findFirst()
                .orElseThrow();
    }
}
