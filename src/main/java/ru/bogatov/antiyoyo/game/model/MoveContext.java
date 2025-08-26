package ru.bogatov.antiyoyo.game.model;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.bogatov.antiyoyo.game.model.common.Hex;
import ru.bogatov.antiyoyo.game.model.common.Color;
import ru.bogatov.antiyoyo.game.model.entity.EntityType;
import ru.bogatov.antiyoyo.game.model.entity.TownHall;

import java.util.Set;

@Data
@Accessors(chain = true)
public class MoveContext {

    private Color selfColor;
    private Set<Color> teammates;
    private TownHall townHall;
    private Hex selectedHex;
    private EntityType selectedEntity;
    private Set<Hex> region;
    private MoveType action;
    private Hex from;
    private Hex to;
    private EntityType entity;

}
