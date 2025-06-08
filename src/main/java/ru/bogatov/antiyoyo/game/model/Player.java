package ru.bogatov.antiyoyo.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Player {

    private UUID userId;
    private HexColor color;
    private TownHall selectedTownHall;

}
