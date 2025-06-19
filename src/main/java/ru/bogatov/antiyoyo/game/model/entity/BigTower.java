package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BigTower extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.BIG_TOWER;
    }

    @Override
    public Integer getBalanceChange() {
        return -5;
    }

    @Override
    public Integer getPrice(Integer unitsCount) {
        return 35;
    }

    @Override
    public Integer getLevel() {
        return 3;
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
