package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnitStageThree extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.UNIT_3;
    }

    @Override
    public Integer getBalanceChange() {
        return -8;
    }

    @Override
    public Integer getPrice(Integer unitsCount) {
        return 30;
    }

    @Override
    public Integer getLevel() {
        return 3;
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
