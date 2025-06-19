package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnitStageOne extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.UNIT_1;
    }

    @Override
    public Integer getGoldChanges() {
        return -2;
    }

    @Override
    public Currency getPrice(Integer unitsCount) {
        return Currency.of(10,0,0);
    }

    @Override
    public Integer getLevel() {
        return 1;
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
