package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnitStageTwo extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.UNIT;
    }

    @Override
    public Integer getPrice() {
        return 20;
    }

    @Override
    public Integer getLevel() {
        return 2;
    }

    @Override
    public Integer getAttackRadius() {
        return 1;
    }

    @Override
    public Integer getMoveRadius() {
        return 4;
    }

    @Override
    public Integer getDefenceRadius() {
        return 1;
    }
}
