package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@EqualsAndHashCode(callSuper = true)
@Data
public class Tower extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.TOWER;
    }

    @Override
    public Currency getStorageChanges() {
        return Currency.of(-2,0,0);
    }

    @Override
    public Currency getPrice(Integer unitsCount) {
        return Currency.of(5,5,5);
    }

    @Override
    public Integer getLevel() {
        return 2;
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
