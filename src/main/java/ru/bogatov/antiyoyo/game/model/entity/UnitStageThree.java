package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@EqualsAndHashCode(callSuper = true)
@Data
public class UnitStageThree extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.UNIT_3;
    }

    @Override
    public Currency getStorageChanges() {
        return Currency.of(-18,0,0);
    }

    @Override
    public Currency getPrice(Integer unitsCount) {
        return Currency.of(30,1,1);
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

}
