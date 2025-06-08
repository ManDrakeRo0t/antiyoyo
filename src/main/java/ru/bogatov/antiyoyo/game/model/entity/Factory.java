package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Factory extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.FACTORY;
    }

    @Override
    public Integer getBalanceChange() {
        return 5;
    }

    @Override
    public Integer getPrice(Integer unitsCount) {
        return 12 + (unitsCount * 2);
    }

    @Override
    public Integer getLevel() {
        return 0;
    }

    @Override
    public Integer getAttackRadius() {
        return 0;
    }

    @Override
    public Integer getMoveRadius() {
        return 0;
    }

    @Override
    public Integer getDefenceRadius() {
        return 0;
    }
}
