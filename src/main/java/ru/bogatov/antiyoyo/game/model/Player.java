package ru.bogatov.antiyoyo.game.model;

import lombok.Data;

import java.util.UUID;

@Data
public class Player {

    private UUID userId;
    private HexColor color;
    private Integer balance;

}
