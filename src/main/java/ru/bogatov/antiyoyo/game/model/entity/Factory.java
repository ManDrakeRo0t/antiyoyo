package ru.bogatov.antiyoyo.game.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.bogatov.antiyoyo.game.model.common.Currency;

@EqualsAndHashCode(callSuper = true)
@Data
public class Factory extends Entity implements Sellable, Interactable {

    @Override
    public EntityType getType() {
        return EntityType.FACTORY;
    }

    @Override
    public Integer getGoldChanges() {
        return 4;
    }

    @Override
    public Currency getPrice(Integer unitsCount) {
        return Currency.of(0,4 + unitsCount  ,2 + unitsCount);
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
