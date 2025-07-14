package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@EqualsAndHashCode(callSuper = true)
@Data
public class BigTower extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.BIG_TOWER;
    }

    @Override
    public Currency getStorageChanges() {
        return Currency.of(-5,0,0);
    }

    @Override
    public Currency getPrice(Integer unitsCount) {
        return Currency.of(10,10,10);
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
