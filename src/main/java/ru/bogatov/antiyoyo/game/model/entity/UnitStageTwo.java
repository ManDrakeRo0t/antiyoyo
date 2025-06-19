package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnitStageTwo extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.UNIT_2;
    }

    @Override
    public Integer getGoldChanges() {
        return -6;
    }

    @Override
    public Currency getPrice(Integer unitsCount) {
        return Currency.of(20,0,0);
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



}
