package ru.bogatov.antiyoyo.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.bogatov.antiyoyo.game.model.common.Color;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.entity.EntityType;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Player {

    private UUID userId;
    private Color color;
    private TownHall selectedTownHall;
    private Hex selectedHex;
    private EntityType selectedEntity;
    private boolean isIlluminated;
    private OffsetDateTime illuminateTime;

    public void illuminate() {
        isIlluminated = true;
        illuminateTime = OffsetDateTime.now();
    }

}
