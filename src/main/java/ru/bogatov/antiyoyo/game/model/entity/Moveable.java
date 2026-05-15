package ru.bogatov.antiyoyo.game.model.entity;

import java.util.UUID;

public interface Moveable extends Interactable {

    UUID getMainTownHall();

    Integer getAttackRadius();

    Integer getMoveRadius();

}
