package ru.bogatov.antiyoyo.game.model.entity;

public interface Interactable {

    Integer getLevel();

    Integer getAttackRadius();

    Integer getMoveRadius();

    Integer getDefenceRadius(); //todo delete and make 1 by default

}
