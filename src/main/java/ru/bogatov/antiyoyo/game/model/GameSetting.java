package ru.bogatov.antiyoyo.game.model;

import lombok.Data;

@Data
public class GameSetting {

    private Boolean undoMove;
    private Boolean grave;
    private Integer secondsToMove;
    private Integer farmsDensity;
    private Boolean demolition;
    private Boolean cut;

}
