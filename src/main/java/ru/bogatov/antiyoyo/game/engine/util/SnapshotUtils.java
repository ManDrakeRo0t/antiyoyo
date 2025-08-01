package ru.bogatov.antiyoyo.game.engine.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.entity.Drone;
import ru.bogatov.antiyoyo.game.model.entity.Fire;
import ru.bogatov.antiyoyo.game.model.entity.HexSnapshot;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class SnapshotUtils {

    ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public static String makeSnapshot(Collection<Hex> hexes) {
        Set<HexSnapshot> snapshotSet = hexes.stream().map(SnapshotUtils::toSnapshot).collect(Collectors.toSet());
        return objectMapper.writeValueAsString(snapshotSet);
    }

    @SneakyThrows
    public static Set<Hex> restoreSnapshot(String snapshot) {
        List<HexSnapshot> data = objectMapper.readValue(snapshot, new TypeReference<List<HexSnapshot>>() {});
        return data.stream().map(SnapshotUtils::restore).collect(Collectors.toSet());
    }

    public static HexSnapshot toSnapshot(Hex hex) {
        return new HexSnapshot(
                hex.getVector(),
                hex.getColor(),
                hex.getEntity().getType(),
                hex.getEntity() instanceof Drone drone ? drone.getOwnerColor() : null,
                hex.getEntity() instanceof TownHall townHall ? townHall.getStorage() : null,
                hex.getEntity() instanceof TownHall townHall ? townHall.isDronesAvailable() : null,
                hex.getEntity() instanceof TownHall townHall ? townHall.getDronesLimit() : null,
                hex.getDefenseLevel(),
                hex.getEntity().getMovedOnThisTurn(),
                hex.getEntity() instanceof Fire fire ? fire.getStage() : null,
                hex.getGlue()
        );
    }

    public static Hex restore(HexSnapshot snapshot) {
        var hex = new Hex(
                snapshot.getVector(),
                false,
                snapshot.getColor(),
                EntityUtils.fromType(snapshot.getEntityType()),
                snapshot.getDefenseLevel(),
                snapshot.getGlue(),
                false
        );
        if (snapshot.getStorage() != null) {
            if (hex.getEntity() instanceof TownHall townHall) {
                townHall.setStorage(snapshot.getStorage());
                townHall.setDronesAvailable(snapshot.getIsDronesAvailable());
                townHall.setDronesLimit(snapshot.getDronesLimit());
            }
        }
        if (hex.getEntity() instanceof Drone drone) {
            drone.setOwnerColor(snapshot.getEntityOwnerColor());
        }
        if (hex.getEntity() instanceof Fire fire) {
            fire.setStage(snapshot.getFireStage());
        }
        hex.getEntity().setMovedOnThisTurn(snapshot.getIsMoved());
        return hex;
    }

}
