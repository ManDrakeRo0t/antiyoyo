package ru.bogatov.antiyoyo.game.engine.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import ru.bogatov.antiyoyo.game.model.Hex;
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
                hex.getEntity() instanceof TownHall townHall ? townHall.getBalance() : null,
                hex.getDefenseLevel()
        );
    }

    public static Hex restore(HexSnapshot snapshot) {
        var hex = new Hex(
                snapshot.getVector(),
                false,
                snapshot.getColor(),
                EntityUtils.fromType(snapshot.getEntityType()),
                snapshot.getDefenseLevel(),
                false
        );
        if (snapshot.getBalance() != null) {
            if (hex.getEntity() instanceof TownHall townHall) {
                townHall.setBalance(snapshot.getBalance());
            }
        }
        return hex;
    }

}
