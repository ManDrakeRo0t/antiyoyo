package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TownHall extends Entity implements Interactable {

    @Override
    public EntityType getType() {
        return EntityType.BUILDING;
    }

    @Override
    public Integer getLevel() {
        return 2;
    }

    @Override
    public Integer getAttackRadius() {
        return -1;
    }

    @Override
    public Integer getMoveRadius() {
        return -1;
    }

    @Override
    public Integer getDefenceRadius() {
        return 1;
    }
}
