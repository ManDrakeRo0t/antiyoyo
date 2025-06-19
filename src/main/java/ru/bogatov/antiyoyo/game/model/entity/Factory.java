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
        return 4;
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
        return -1;
    }

    @Override
    public Integer getMoveRadius() {
        return -1;
    }
}
