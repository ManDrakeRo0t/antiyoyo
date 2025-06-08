package ru.bogatov.antiyoyo.game.model;

import lombok.Data;

@Data
public class GameSetting {

    private Boolean undoMove;
    private Boolean grave;
    private Integer playersCount;
    private Boolean fogOfWar;
    private Float earthDensity;
    private Float treeDensity;

}
