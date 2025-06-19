package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Tank extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.TANK;
    }

    @Override
    public Integer getBalanceChange() {
        return -54;
    }

    @Override
    public Integer getPrice(Integer unitsCount) {
        return 40;
    }

    @Override
    public Integer getLevel() {
        return 4;
    }

    @Override
    public Integer getAttackRadius() {
        return 1;
    }

    @Override
    public Integer getMoveRadius() {
        return 4;
    }




}
